package com.niufangzi.task;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.niufangzi.Parser.SellingPageParser;
import com.niufangzi.Parser.SellingParser;
import com.niufangzi.Parser.URLHelper;
import com.niufangzi.dao.BreakPointDao;
import com.niufangzi.dao.HouseChangeDao;
import com.niufangzi.dao.HouseSellingDao;
import com.niufangzi.dao.PhotoSellingDao;
import com.niufangzi.model.BreakPoint;
import com.niufangzi.model.HouseChange;
import com.niufangzi.model.HouseSelling;
import com.niufangzi.model.OperationState;
import com.niufangzi.model.PhotoSelling;
import com.niufangzi.model.BreakPoint.TaskLevel;
import com.niufangzi.model.BreakPoint.TaskType;
import com.niufangzi.util.UtilHelper;

public class HouseSellingTask {
	private static Logger logger = LogManager.getLogger();
	private static int SleepIntervalHouse = 500;   //下载间隔0.5秒
	
	/*
	 * 庞大的工作(对于一个城市来说，只有在刚建空库时，需要运行一次)
	 * 以最深的层级(街道一级)来下载所有的‘在售’房屋信息
	 * 以北京为例，约3万套， 按1秒/套(执行0.5秒+停顿0.5秒)的速度计算， 整个工作需耗时约： 9小时
	 */
	public static boolean DoHugeTask(int cityIdx)
	{
		boolean res = true;
		logger.info(" ++++++++++++++    HouseSelling - DoHugeTask  begin  +++++++++++++");
		if ((cityIdx<0) || (cityIdx>=URLHelper.GetCityCount()))
		{
			logger.error("cityIdx: " + cityIdx + " is error !");
			res = false;
		}
		else {
			OperationState state = new OperationState();
			res = DownloadACity(cityIdx, TaskLevel.JIEDAO, state);  //按街道一级
		}
		logger.info(" ++++++++++++++    HouseSelling - DoHugeTask  end  +++++++++++++");
		return res;
	}
	
	/*
	 * 中等的工作(约30天运行一次即可)
	 * 以中等的层级(区一级)来下载所有的‘在售’房屋信息（最多100页 * 30条/页 ）
	 * 以北京为例， 正常情况下，每天新增500套， 约 15000套
	 * 按1秒/套(执行0.5秒+停顿0.5秒)的速度计算， 整个工作最大耗时约： 4小时
	 * 对于一个城市来说，只有在刚建空库时，需要运行一次。
	 */
	public static boolean DoMidTask(int cityIdx)
	{
		boolean res = true;
		logger.info(" ++++++++++++++    HouseSelling - DoMidTask  begin  +++++++++++++");
		if ((cityIdx<0) || (cityIdx>=URLHelper.GetCityCount()))
		{
			logger.error("cityIdx: " + cityIdx + " is error !");
			res = false;
		}
		else {
			OperationState state = new OperationState();
			res = DownloadACity(cityIdx, TaskLevel.QU, state);  //按区一级
		}
		logger.info(" ++++++++++++++    HouseSelling - DoMidTask  end  +++++++++++++");
		return res;
	}
	
	/*
	 * 日常的工作(约3天运行一次即可)
	 * 以最浅的层级(市一级)来下载所有的‘在售’房屋信息（最多100页 * 30条/页）
	 * 3000条， 按1秒/套(执行0.5秒+停顿0.5秒)的速度计算， 整个工作需耗时约： 1小时
	 * 对于一个城市来说，只有在刚建空库时，需要运行一次。
	 */
	public static boolean DoSmallTask(int cityIdx)
	{
		boolean res = true;
		logger.info(" ++++++++++++++    HouseSelling - DoSmallTask  begin  +++++++++++++");
		if ((cityIdx<0) || (cityIdx>=URLHelper.GetCityCount()))
		{
			logger.error("cityIdx: " + cityIdx + " is error !");
			res = false;
		}
		else {
			OperationState state = new OperationState();
			res = DownloadACity(cityIdx, TaskLevel.CITY, state);  //按市一级
		}
		logger.info(" ++++++++++++++    HouseSelling - DoSmallTask  end  +++++++++++++");
		return res;
	}

	
	// 下载一个城市的所有‘在售’房屋信息
	// level   0:按市一级下载；  1:按区一级下载；  2：按街道一级下载
	// 由于网站上不管哪一级，都是最多显示100页。  所以级别越往下，能抓取到的记录也就越多，所需要的时间也越长
	private static boolean DownloadACity(int cityIdx, TaskLevel level, OperationState state) 
	{
		state.result = true; //默认操作成功
		String cityName = URLHelper.GetCityName(cityIdx, state);
		if (! state.result)
		{
			return false;
		}
		
		int lastQuIdx = 0;
		int lastJiedaoIdx = 0;
		int lastPageNo = 1;
		BreakPoint bp_curr = new BreakPoint(cityIdx, TaskType.SELLING.ordinal(), level.ordinal(), 0, 0, 1);
		BreakPoint bp_last = BreakPointDao.GetBreakPoint(cityIdx, TaskType.SELLING.ordinal(), level.ordinal());
		if (bp_last != null) //从上次保存的断点开始执行
		{
			lastQuIdx = bp_last.getQuIdx();
			lastJiedaoIdx = bp_last.getJiedaoIdx();
			lastPageNo = bp_last.getPageNo() + 1;
			
			String strObj = UtilHelper.ObjectToString(bp_last);
			System.out.println(" task will start from last brekpoint: " );
			System.out.println(strObj);
		}
		
		if (level == TaskLevel.CITY) {  //按市一级下载
			String cityCode = "";
			String hintMsg = cityName;
			int startPageNo = lastPageNo; //从断点页开始下载 
			DownloadAArea(cityIdx, cityCode, hintMsg, startPageNo, bp_curr, state);
		}
		else if (level == TaskLevel.QU) {  //按区一级下载
			List<String> list_quName = new ArrayList<String>();
			List<String> list_quCode = new ArrayList<String>();
			int quCount = URLHelper.GetQuListWithRetry(cityIdx, list_quName, list_quCode, state);
			if (! state.result)
			{
				return false;
			}
			for(int quIdx=lastQuIdx; quIdx<quCount; quIdx++)
			{
				String quName = list_quName.get(quIdx);
				String quCode = list_quCode.get(quIdx);
				String hintMsg = cityName + "-" + quName + "(" + (quIdx+1) + "/" + quCount + ")";
				int startPageNo = 1;
				if (quIdx == lastQuIdx)  //从断点页处开始下载
				{
					startPageNo = lastPageNo;
				}
				bp_curr.setQuIdx(quIdx);
				DownloadAArea(cityIdx, quCode, hintMsg, startPageNo, bp_curr, state);
			}
		}
		else if (level == TaskLevel.JIEDAO) {  //按街道一级下载
			List<String> list_quName = new ArrayList<String>();
			List<String> list_quCode = new ArrayList<String>();
			int quCount = URLHelper.GetQuListWithRetry(cityIdx, list_quName, list_quCode, state);
			if (! state.result)
			{
				return false;
			}
			for(int quIdx=lastQuIdx; quIdx<quCount; quIdx++)
			{
				String quName = list_quName.get(quIdx);
				String quCode = list_quCode.get(quIdx);
				List<String> list_jiedaoName = new ArrayList<String>();
				List<String> list_jiedaoCode = new ArrayList<String>();
				int jiedaoCount = URLHelper.GetJiedaoListWithRetry(cityIdx, quCode, list_jiedaoName, list_jiedaoCode, state);
				if (state.result)
				{
					int startJiedaoIdx = 0;
					if (quIdx == lastQuIdx) //从断点的街道索引处开始
					{
						startJiedaoIdx = lastJiedaoIdx;
					}
					bp_curr.setQuIdx(quIdx);
					for(int jiedaoIdx=startJiedaoIdx; jiedaoIdx<jiedaoCount; jiedaoIdx++)
					{
						String jiedaoName = list_jiedaoName.get(jiedaoIdx);
						String jiedaoCode = list_jiedaoCode.get(jiedaoIdx);
						String hintMsg = cityName + "-" + quName + "(" + (quIdx+1) + "/" + quCount + ")" 
										+ "-" + jiedaoName + "(" + (jiedaoIdx+1) + "/" + jiedaoCount + ")";
						int startPageNo = 1;
						if((quIdx == lastQuIdx) && (jiedaoIdx == lastJiedaoIdx))   //从断点页处开始下载
						{
							startPageNo = lastPageNo;
						}
						bp_curr.setJiedaoIdx(jiedaoIdx);
						DownloadAArea(cityIdx, jiedaoCode, hintMsg, startPageNo, bp_curr, state);
					}
				}
				
			}
		}
		
		//所有任务执行完成后，删除数据库中的断点信息
		BreakPointDao.DeleteBreakPoint(bp_curr.getCityIdx(), bp_curr.getTaskType(), bp_curr.getLevel());
		
		return true;
	}

	
	// 下载一个区域（可能是 市 / 区 / 街道一级）
	private static boolean DownloadAArea(int cityIdx, String areaCode, String hintMsg, int startPageNo, BreakPoint bp, OperationState state) 
	{
		state.result = true; //默认操作成功
		int endPageNo = URLHelper.GetSellingPageCountWithRetry(cityIdx, areaCode, state);
		if (! state.result)
		{
			return false;
		}
		for (int pageNo=startPageNo; pageNo<=endPageNo; pageNo++)
		{
			logger.info("======= downloading selling " + hintMsg + ", page " + pageNo + "/" + endPageNo + "=======");
			String pageURL = URLHelper.GenerateSellingPageURL(cityIdx, areaCode, pageNo, state);
			if (! state.result)
			{
				logger.error("DownloadAArea  failed:" + state.functionName + " - " + state.errorMsg);
				continue;
			}
			boolean res = DownloadAPage(pageURL);
			
			//下载一页后(无论成功失败)，更新数据库中的断点信息
			bp.setPageNo(pageNo);
			BreakPointDao.RefreshBreakPoint(bp);
			
			if (! res)
			{
				logger.error("DownloadAArea failed !");
				break;
			}
		}
		return true;
		
	}
	
	
	//下载一页"在售"房屋信息
	private static boolean DownloadAPage(String pageURL)
	{
		List<String> list_id = new ArrayList<String>();
		List<String> list_url = new ArrayList<String>();
		boolean res = false;
		try{
			res = SellingPageParser.ParseAPage(pageURL, list_id, list_url);
		}
		catch(Exception e) {
			logger.error(e.getMessage());
		}
		
		if (! res)
		{
			logger.info("failed !");
			return false;
		}
		int totalCount = list_id.size();
		
		for(int i=0; i<totalCount; i++)
		{
			String houseId = list_id.get(i);
			logger.info("downloading " + String.valueOf(i+1) + "/" + totalCount + " : " + houseId);
			try {
				DownloadAHouse(houseId, list_url.get(i));
				Thread.sleep(SleepIntervalHouse);
			} catch (InterruptedException e) {
				logger.error(e.getMessage());
			}
		}
		
		return res;
	}
	
	//下载一个"在售"房屋信息
	private static boolean DownloadAHouse(String houseId, String houseURL) {
		HouseSelling house = new HouseSelling();
		List<PhotoSelling> list_photo = new ArrayList<PhotoSelling>();
		
		boolean res = SellingParser.ParseAHouse(houseURL, houseId, house, list_photo);	
		if (res)
		{
			String strHuxing = house.getFangwuhuxing();
			if (strHuxing != null)
			{
				if (strHuxing.length() >= 4)
				{
					strHuxing = strHuxing.substring(0, 4);
				}
			}
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
			String str = house.getQuyu2() + " " + house.getXiaoqu() + " " + strHuxing + " " + house.getJianzhumianji() 
					 + "平米   " + house.getGuapaijiage() + "万 " + sdf.format(house.getGuapaishijian());
			logger.info(str);
			
			HouseSelling houseOld = HouseSellingDao.GetHouse(houseId);
			if (houseOld != null) //已存在，检查报价是否有变动
			{
				if(houseOld.getGuapaijiage()!=house.getGuapaijiage() && house.getGuapaijiage()>0)
				{
					if (HouseSellingDao.UpdateHouse(house))
					{
						HouseChange hc = new HouseChange(house.getHouseId(), house.getChengshi(), house.getQuyu1(), house.getQuyu2(),
												house.getXiaoqu(), house.getFangwuhuxing(), house.getJianzhumianji(), 
												houseOld.getGuapaijiage(), house.getGuapaijiage(), 
												houseOld.getGuapaishijian(), house.getGuapaishijian());
						HouseChangeDao.AddHouseChange(hc);
					}
					str = "**** 价格变动: " + houseOld.getGuapaijiage() + " ==> " + house.getGuapaijiage();
					logger.info(str);
				}
				else {
					logger.info("已存在！");
				}
			}
			else {  //不存在，则新增
				if( HouseSellingDao.AddHouse(house) )
				{
					for(int i=0; i<list_photo.size(); i++)  //给图片信息的"城市"字段赋值
					{
						PhotoSelling photo = list_photo.get(i);
						photo.setChengshi(house.getChengshi());
						if (photo.getPhotoName().equals("户型图"))  //由于磁盘空间关系，暂时只保存户型图。 2018-01-29
						{
							PhotoSellingDao.AddAPhoto(photo);
						}
						//list_photo.get(i).setChengshi(house.getChengshi());
					}
					//PhotoSellingDao.AddPhotos(list_photo);
					logger.info("新增成功");
				}
				else
				{
					logger.info("    saving house failed");
				}
			}
			
		}
		else
		{
			logger.info("    Parse failed");
		}
		return res;
	}

	
	private static void doTest1()
	{
		int cityIdx = 0;
		String areaCode = "andingmen";
		OperationState state = new OperationState();
		int pageCount = URLHelper.GetSellingPageCountWithRetry(cityIdx, areaCode, state);
		System.out.println("pageCount : " + pageCount);
	}
	

	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		//DoSmallTask(0);
	}

}
