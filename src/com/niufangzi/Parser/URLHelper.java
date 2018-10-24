package com.niufangzi.Parser;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.niufangzi.model.OperationState;

public class URLHelper {
	private static Logger logger = LogManager.getLogger();
	
	// 访问地址格式如： https://bj.lianjia.com/ershoufang/xicheng/pg3/
	
	// 城市列表（名称+编码）；  编码即为URL中的前缀：
	private static String[][] LIST_CITY = { {"北京", "bj"}, {"武汉", "wh"}, {"廊坊", "lf"}
											 };
	private static int CITY_COUNT = 3; //城市数量
	
	//根据城市的索引获取城市的名称
	public static String GetCityName(int cityIdx, OperationState state)
	{
		state.result = true; //默认操作成功
		if(cityIdx<0 || cityIdx>= CITY_COUNT)
		{
			state.SetState(false, "GetCityName", "cityIdx: " + cityIdx + " is error");
			return "";
		}	
		return LIST_CITY[cityIdx][0];
	}
	
	//根据城市的索引获取城市的编码
	public static String GetCityCode(int cityIdx, OperationState state)
	{
		state.result = true; //默认操作成功
		if(cityIdx<0 || cityIdx>= CITY_COUNT)
		{
			state.SetState(false, "GetCityCode", "cityIdx: " + cityIdx + " is error");
			return "";
		}
		return LIST_CITY[cityIdx][1];
	}
	
	public static int GetCityCount()
	{
		return CITY_COUNT;
	}
	
	
	// 获取‘在售’房屋的 访问URL地址
	// cityIdx:城市索引；   areaCode:区域编码；(区域可能为区或者街道一级)     pageNo:页号
	public static String GenerateSellingPageURL(int cityIdx, String areaCode, int pageNo, OperationState state)
	{		
		state.result = true; //默认操作成功
		String cityCode = GetCityCode(cityIdx, state);
		if (! state.result)
		{
			return "";
		}
		String areaTmp = "";
		if (areaCode.length() > 0)
		{
			areaTmp = areaCode + "/";
		}
		// co32:按上架时间逆序;  ng1:不看车位   
		String pageURL = "https://" + cityCode  + ".lianjia.com/ershoufang/" + areaTmp + "pg" + pageNo + "co32ng1/";  //
		
		return pageURL;
	}
	
	// 获取‘在售’房屋列表的总页数
	public static int GetSellingPageCount(int cityIdx, String areaCode, OperationState state)
	{
		state.result = true; //默认操作成功
		int pageCount = 0;
		String pageURL = GenerateSellingPageURL(cityIdx, areaCode, 1, state);
		if (! state.result)
		{
			return 0;
		}
		Document doc;
		state.result = false;
		try {
			doc = Jsoup.connect(pageURL).get();
			Element eleTmp = doc.getElementsByClass("page-box fr").first().getElementsByClass("page-box house-lst-page-box").first();
			//page-data="{&quot;totalPage&quot;:58,&quot;curPage&quot;:3}"
			// {"totalPage":100,"curPage":1}
			String strTmp = eleTmp.attr("page-data").replace("&quot;", "");
			int idx1 = strTmp.indexOf(":");
			int idx2 = strTmp.indexOf(",");
			String strCount = strTmp.substring(idx1+1, idx2);
			pageCount = Integer.valueOf(strCount);
			state.result = true;
		}
		catch (org.jsoup.UncheckedIOException e) {
			logger.error(e.getMessage());
		}
		catch (java.net.SocketTimeoutException e1) {
			logger.error(e1.getMessage());
		}
		catch (IOException e2) {
			logger.error(e2.getMessage());
		}
		catch (Exception e3) {
			logger.error(e3.getMessage());
		}
		return pageCount;
	}	
	
	// 获取‘成交’房屋的 访问URL地址
	// cityIdx:城市索引；   areaCode:区域编码；(区域可能为区或者街道一级)     pageNo:页号
	public static String GenerateSoldPageURL(int cityIdx, String areaCode, int pageNo, OperationState state)
	{
		state.result = true; //默认操作成功
		String cityCode = GetCityCode(cityIdx, state);
		if (! state.result)
		{
			return "";
		}
		String areaTmp = "";
		if (areaCode.length() > 0)
		{
			areaTmp = areaCode + "/";
		}	
		// ng1:不看车位
		String pageURL = "https://" + cityCode + ".lianjia.com/chengjiao/" + areaTmp + "pg" + pageNo + "ng1/";
		return pageURL;
	}
	
	//获取‘在售’房屋列表的总页数
	public static int GetSoldPageCount(int cityIdx, String areaCode, OperationState state)
	{
		state.result = true; //默认操作成功
		int pageCount = 0;
		String pageURL = GenerateSoldPageURL(cityIdx, areaCode, 1, state);
		if (! state.result)
		{
			return 0;
		}
		Document doc;
		state.result = false;
		try {
			doc = Jsoup.connect(pageURL).get();
			Element eleTmp = doc.getElementsByClass("page-box fr").first().getElementsByClass("page-box house-lst-page-box").first();
			//page-data="{&quot;totalPage&quot;:58,&quot;curPage&quot;:3}"
			// {"totalPage":100,"curPage":1}
			String strTmp = eleTmp.attr("page-data").replace("&quot;", "");
			int idx1 = strTmp.indexOf(":");
			int idx2 = strTmp.indexOf(",");
			String strCount = strTmp.substring(idx1+1, idx2);
			pageCount = Integer.valueOf(strCount);
			state.result = true;
		}
		catch (org.jsoup.UncheckedIOException e) {
			logger.error(e.getMessage());
		}
		catch (java.net.SocketTimeoutException e1) {
			logger.error(e1.getMessage());
		}
		catch (IOException e2) {
			logger.error(e2.getMessage());
		}
		catch (Exception e3) {
			logger.error(e3.getMessage());
		}
		return pageCount;
	}
		
	// 获取一个市下面所有区的列表：‘在售’和‘成交’ 页面中 市-区-街道 的划分是一致的
	public static int GetQuList(int cityIdx, List<String>list_quName, List<String>list_quCode, OperationState state)
	{
		state.result = true; //默认操作成功
		String pageURL = GenerateSoldPageURL(cityIdx, "", 1, state);  //此处areaCode参数为""
		if (! state.result)
		{
			return 0;
		}
		Document doc;
		state.result = false;
		try {
			doc = Jsoup.connect(pageURL).get();
			Elements eles_a = doc.getElementsByClass("position").first().getElementsByAttributeValue("data-role", "ershoufang").first()
								.getElementsByTag("a");

			// <a href="/chengjiao/dongcheng/ng1/" title="北京东城成交二手房 ">东城</a> 
			// <a href="/chengjiao/xicheng/ng1/" title="北京西城成交二手房 ">西城</a>
			// <a href="https://lf.lianjia.com/chengjiao/yanjiao/ng1/" target="_blank" title="北京燕郊成交二手房 ">燕郊</a> 
			// <a href="https://lf.lianjia.com/chengjiao/xianghe/ng1/" target="_blank" title="北京香河成交二手房 ">香河</a> 			
			for (Element ele : eles_a) {
				String strName = ele.text().trim();
				String strTmp = ele.attr("href").trim();
				
				// 由于北京链家的页面中 含有 “燕郊”和“廊坊”的链接， 此处特殊处理，过滤掉。
				if (cityIdx==0 && (strName.equals("燕郊") || strName.equals("香河")))
				{
					continue;
				}
				String list_tmp[] = strTmp.split("/");
				if (list_tmp.length >= 2)
				{
					String strCode = list_tmp[list_tmp.length-2];
					list_quName.add(strName);
					list_quCode.add(strCode);
				}
			}
			state.result = true;
		}
		catch (org.jsoup.UncheckedIOException e) {
			logger.error(e.getMessage());
			state.SetState(false, "GetQuList", e.getMessage());
		}
		catch (java.net.SocketTimeoutException e1) {
			logger.error(e1.getMessage());
			state.SetState(false, "GetQuList", e1.getMessage());
		}
		catch (IOException e2) {
			logger.error(e2.getMessage());
			state.SetState(false, "GetQuList", e2.getMessage());
		}
		
		return list_quName.size();
	}
	
	// 获取一个区下面所有街道的列表：‘在售’和‘成交’ 页面中 市-区-街道 的划分是一致的
	public static int GetJiedaoList(int cityIdx, String quCode, List<String>list_jiedaoName, List<String>list_jiedaoCode, OperationState state)
	{
		state.result = true; //默认操作成功
		String pageURL = GenerateSellingPageURL(cityIdx, quCode, 1, state);
		Document doc;
		state.result = false;
		try {
			doc = Jsoup.connect(pageURL).get();
			
			//此处各个城市的html内容不一致，需要分别处理-2018-01-30
			if (cityIdx == 0) { //北京
				Elements eles = doc.getElementsByClass("position").first()//.getElementsByAttributeValue("data-role", "ershoufang").first()
									.getElementsByClass("sub_sub_nav section_sub_sub_nav").first().getElementsByTag("a");
				//Elements eles_a = eleTmp.getElementsByTag("div").get(2).getElementsByTag("a");  //取第数组中的第 3 个 div
	
				//<a href="/chengjiao/andingmen/ng1/">安定门</a>  
		        //<a href="/chengjiao/baizhifang1/ng1/">白纸坊</a> 		
				for (Element ele : eles) {
					String strName = ele.text().trim();
					String strTmp = ele.attr("href").trim();
					String list_tmp[] = strTmp.split("/");
					if (list_tmp.length >= 2)
					{
						String strCode = list_tmp[list_tmp.length-2];
						list_jiedaoName.add(strName);
						list_jiedaoCode.add(strCode);
					}
				}
			}
			else if ((cityIdx == 1) || (cityIdx == 2)) { //武汉 || 廊坊 
				Elements eleDivs = doc.getElementsByAttributeValue("data-role", "ershoufang").first().getElementsByTag("div");
				if (eleDivs.size() > 2) //有些区/县下可能还没有数据。
				{
					Elements eles = eleDivs.get(2).getElementsByTag("a");
			        // <a href="/ershoufang/baishazhou/ng1/">白沙洲</a> 
			        // <a href="/ershoufang/chuhehanjie/ng1/">楚河汉街</a> 
					
					for (Element ele : eles) {
						String strName = ele.text().trim();
						String strTmp = ele.attr("href").trim();
						String list_tmp[] = strTmp.split("/");
						if (list_tmp.length >= 2)
						{
							String strCode = list_tmp[list_tmp.length-2];
							list_jiedaoName.add(strName);
							list_jiedaoCode.add(strCode);
						}
					}
				}
			}			
			state.result = true;
		}
		catch (org.jsoup.UncheckedIOException e) {
			logger.error(e.getMessage());
			state.SetState(false, "GetJiedaoList", e.getMessage());
		}
		catch (java.net.SocketTimeoutException e1) {
			logger.error(e1.getMessage());
			state.SetState(false, "GetJiedaoList", e1.getMessage());
		}
		catch (IOException e2) {
			logger.error(e2.getMessage());
			state.SetState(false, "GetJiedaoList", e2.getMessage());
		}
		
		return list_jiedaoName.size();
	}
	
	
	
	
	//================================== 以下函数加上了失败后重试功能 ==================================
	
	public static int GetSellingPageCountWithRetry(int cityIdx, String areaCode, OperationState state)
	{
		int pageCount = 0;
		int nRetry = 0;
		do {
			pageCount = GetSellingPageCount(cityIdx, areaCode, state);
			nRetry++;
			if (!state.result)
			{
				try {
					logger.warn("failed,  I will retry " + nRetry);
					Thread.sleep(3000);  //休眠3秒
				} catch (InterruptedException e) {
					e.printStackTrace();
				}  
			}
		}while (! (state.result || nRetry>5));
		return pageCount;
	}
	
	public static int GetSoldPageCountWithRetry(int cityIdx, String areaCode, OperationState state)
	{
		int pageCount = 0;
		int nRetry = 0;
		do {
			pageCount = GetSoldPageCount(cityIdx, areaCode, state);
			nRetry++;
			if (!state.result)
			{
				try {
					logger.warn("failed,  I will retry " + nRetry);
					Thread.sleep(3000);  //休眠3秒
				} catch (InterruptedException e) {
					e.printStackTrace();
				}  
			}
		}while (! (state.result || nRetry>5));
		return pageCount;
	}
	
	public static int GetQuListWithRetry(int cityIdx, List<String>list_quName, List<String>list_quCode, OperationState state)
	{
		int quSize = 0;
		int nRetry = 0;
		do {
			quSize = GetQuList(cityIdx, list_quName, list_quCode, state);
			nRetry++;
			if (!state.result)
			{
				try {
					logger.warn("failed,  I will retry " + nRetry);
					Thread.sleep(3000);  //休眠3秒
				} catch (InterruptedException e) {
					e.printStackTrace();
				}  
			}
		}while (! (state.result || nRetry>5));
		return quSize;
	}
	

	public static int GetJiedaoListWithRetry(int cityIdx, String quCode, List<String>list_jiedaoName, List<String>list_jiedaoCode, OperationState state)
	{
		int jiedaoSize = 0;
		int nRetry = 0;
		do {
			jiedaoSize = GetJiedaoList(cityIdx, quCode, list_jiedaoName, list_jiedaoCode, state);
			nRetry++;
			if (!state.result)
			{
				try {
					logger.warn("failed,  I will retry " + nRetry);
					Thread.sleep(3000);  //休眠3秒
				} catch (InterruptedException e) {
					e.printStackTrace();
				}  
			}
		}while (! (state.result || nRetry>5));
		return jiedaoSize;
	}
	
	
	private static void doTest()
	{
		OperationState state = new OperationState();
		/*
		List<String> list_quName = new ArrayList<String>();
		List<String> list_quCode = new ArrayList<String>();
		GetQuList(0, list_quName, list_quCode, state);
		if (state.result)
		{
			for (int i=0; i<list_quName.size(); i++)
			{
				System.out.println(list_quName.get(i) + ":" + list_quCode.get(i));
			}
		}
		else
		{
			System.out.println("error:" + state.errorMsg);
		}
		*/
		
		List<String> list_jiedaoName = new ArrayList<String>();
		List<String> list_jiedaoCode = new ArrayList<String>();
		//GetJiedaoList(0,"xicheng", list_jiedaoName, list_jiedaoCode, state); //北京-西城
		//GetJiedaoList(1,"wuchang", list_jiedaoName, list_jiedaoCode, state); //武汉-武昌
		//GetJiedaoList(2,"yanjiao", list_jiedaoName, list_jiedaoCode, state); //燕郊-廊坊
		GetJiedaoList(2,"wenanxian", list_jiedaoName, list_jiedaoCode, state); //燕郊-文安县
		if (state.result)
		{
			System.out.println("size:" + list_jiedaoName.size());
			for (int i=0; i<list_jiedaoName.size(); i++)
			{
				System.out.println(list_jiedaoName.get(i) + ":" + list_jiedaoCode.get(i));
			}
		}
		else
		{
			System.out.println("error:" + state.errorMsg);
		}
	}
	
	
	
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		//doTest();
	}

}
