package com.niufangzi.task;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.niufangzi.Parser.SoldPageParser;
import com.niufangzi.Parser.SoldParser;
import com.niufangzi.Parser.URLHelper;
import com.niufangzi.dao.BreakPointDao;
import com.niufangzi.dao.HouseSellingDao;
import com.niufangzi.dao.HouseSoldDao;
import com.niufangzi.dao.PhotoSellingDao;
import com.niufangzi.dao.PhotoSoldDao;
import com.niufangzi.model.BreakPoint;
import com.niufangzi.model.HouseSelling;
import com.niufangzi.model.HouseSold;
import com.niufangzi.model.OperationState;
import com.niufangzi.model.PhotoSold;
import com.niufangzi.model.BreakPoint.TaskLevel;
import com.niufangzi.model.BreakPoint.TaskType;
import com.niufangzi.util.UtilHelper;

public class HouseSoldTask {
	private static Logger logger = LogManager.getLogger();
	private static int SleepIntervalHouse = 500;   //下载间隔0.5秒
	
	/*
	 * 庞大的工作(对于一个城市来说，只有在刚建空库时，需要运行一次)
	 * 以最深的层级(街道一级)来下载所有的‘成交’房屋信息
	 * 以北京为例，约65万套(截至2018-01-16)， 按1秒/套(执行0.5秒+停顿0.5秒)的速度计算， 整个工作需耗时约： 180小时
	 */
	public static boolean DoHugeTask(int cityIdx)
	{
		boolean res = true;
		logger.info(" ++++++++++++++   HouseSold - DoHugeTask  begin  +++++++++++++");
		if ((cityIdx<0) || (cityIdx>=URLHelper.GetCityCount()))
		{
			logger.error("cityIdx: " + cityIdx + " is error !");
			return false;
		}
		else {
			OperationState state = new OperationState();
			res = DownloadACity(cityIdx, TaskLevel.JIEDAO, state);  //按街道一级
		}
		logger.info(" ++++++++++++++    HouseSold - DoHugeTask  end  +++++++++++++");
		return res;
	}
	
	/*
	 * 中等的工作 (约30天运行一次即可)
	 * 以最中等的层级(区一级)来下载所有的‘成交’房屋信息 （最多100页 * 30条/页 ）
	 * 以北京为例， 17个区，每个区最多3000条记录，最多5万条记录，
	 * 按1秒/套(执行0.5秒+停顿0.5秒)的速度计算， 整个工作最大耗时约： 14小时
	 * 对于一个城市来说，只有在刚建空库时，需要运行一次。
	 */
	public static boolean DoMidTask(int cityIdx)
	{
		boolean res = true;
		logger.info(" ++++++++++++++   HouseSold - DoMidTask  begin  +++++++++++++");
		if ((cityIdx<0) || (cityIdx>=URLHelper.GetCityCount()))
		{
			logger.error("cityIdx: " + cityIdx + " is error !");
			return false;
		}
		else {
			OperationState state = new OperationState();
			res = DownloadACity(cityIdx, TaskLevel.QU, state);  //按区一级
		}
		logger.info(" ++++++++++++++    HouseSold - DoMidTask  end  +++++++++++++");
		return res;
	}
	

	/*
	 * 日常的工作 (约3天运行一次即可)
	 * 以最浅的层级(市一级)来下载所有的‘成交’房屋信息 （最多100页 * 30条/页）
	 * 3000条记录， 按1秒/套(执行0.5秒+停顿0.5秒)的速度计算， 整个工作需耗时约： 1小时
	 * 对于一个城市来说，只有在刚建空库时，需要运行一次。
	 */
	public static boolean DoSmallTask(int cityIdx)
	{
		boolean res = true;
		logger.info(" ++++++++++++++   HouseSold - DoSmallTask  begin  +++++++++++++");
		if ((cityIdx<0) || (cityIdx>=URLHelper.GetCityCount()))
		{
			logger.error("cityIdx: " + cityIdx + " is error !");
			return false;
		}
		else {
			OperationState state = new OperationState();
			res = DownloadACity(cityIdx, TaskLevel.CITY, state);  //按市一级
		}
		logger.info(" ++++++++++++++    HouseSold - DoSmallTask  end  +++++++++++++");
		return res;
	}
	
	
	
	
	// 下载一个城市的所有‘成交’房屋信息
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
		BreakPoint bp_curr = new BreakPoint(cityIdx, TaskType.SOLD.ordinal(), level.ordinal(), 0, 0, 1);
		BreakPoint bp_last = BreakPointDao.GetBreakPoint(cityIdx, TaskType.SOLD.ordinal(), level.ordinal());
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
			int startPageNo = lastPageNo; //接断点页开始下载
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
		int endPageNo = URLHelper.GetSoldPageCountWithRetry(cityIdx, areaCode, state);
		if (! state.result)
		{
			return false;
		}
		for (int pageNo=startPageNo; pageNo<=endPageNo; pageNo++)
		{
			logger.info("======= downloading sold " + hintMsg + ", page " + pageNo + "/" + endPageNo + "=======");
			String pageURL = URLHelper.GenerateSoldPageURL(cityIdx, areaCode, pageNo, state);
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
	
	
	//下载一页"成交"房屋信息
	private static boolean DownloadAPage(String pageURL)
	{
		List<String> list_id = new ArrayList<String>();
		List<String> list_url = new ArrayList<String>();
		boolean res = false;
		try{
			res = SoldPageParser.ParseAPage(pageURL, list_id, list_url);
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
			if (HouseSoldDao.houseExist(houseId)) //已存在
			{
				logger.info("    house exist !");
			}
			else {
				try {
					DownloadAHouse(houseId, list_url.get(i));
					Thread.sleep(SleepIntervalHouse);
				} catch (InterruptedException e) {
					logger.error(e.getMessage());
				}
			}
		}
		
		return res;
	}
	
	//下载一个"成交"房屋信息
	private static boolean DownloadAHouse(String houseId, String houseURL) {
		HouseSold house = new HouseSold();
		List<PhotoSold> list_photo = new ArrayList<PhotoSold>();
		
		boolean res = SoldParser.ParseAHouse(houseURL, houseId, house, list_photo);	
		if (res)
		{
			//处理‘在售’表中对应记录
			HouseSelling hs = HouseSellingDao.GetHouse(house.getHouseId());
			if (hs != null) //‘在售’表中有对应记录。
			{
				house.setShangcijiaoyi(hs.getShangcijiaoyi());  //'上次交易时间'字段信息只在 ‘在售’表中有
				HouseSellingDao.DeleteHouse(houseId);           //将记录从‘在售’表中删除
				PhotoSellingDao.DeletePhotos(houseId);          //删除其图片信息
				logger.info("delete from t_selling and photo");
			}
			
			if (house.getChengjiaojiage() > 0) //只记录链家成交(有成交价格信息的)-20180130
			{
				//打印房屋信息
				String strHuxing = house.getFangwuhuxing();
				if (strHuxing.length() >= 4)
				{
					strHuxing = strHuxing.substring(0, 4);
				}
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
				String str = house.getQuyu2() + " " + house.getXiaoqu() + " " + strHuxing + " " + house.getJianzhumianji() 
						 + "平米   " + house.getChengjiaojiage() + "万 " + sdf.format(house.getChengjiaoriqi());
				logger.info(str);

				if( HouseSoldDao.AddHouse(house) )
				{
					for(int i=0; i<list_photo.size(); i++)  //给图片信息的"城市"字段赋值
					{
						PhotoSold photo = list_photo.get(i);
						photo.setChengshi(house.getChengshi());
						if (photo.getPhotoName().equals("户型图"))  //由于磁盘空间关系，暂时只保存户型图。 2018-01-29
						{
							PhotoSoldDao.AddAPhoto(photo);
						}
					}
				}
				else
				{
					logger.info("    saving house failed");
				}
	
			}

			logger.info("    completed");
		}
		else
		{
			logger.info("    failed");
		}
		
		return res;
	}
	
	
	private static boolean doTest()
	{
		String houseURL = "https://bj.lianjia.com/chengjiao/101101367614.html";
		String houseId = "101101367614";
		HouseSold house = new HouseSold();
		List<PhotoSold> list_photo = new ArrayList<PhotoSold>();
		
		boolean res = SoldParser.ParseAHouse(houseURL, houseId, house, list_photo);	
		String str =UtilHelper.ObjectToString(house);
		System.out.println(str);
		
		return res;
	}
	
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		//
	}

}
