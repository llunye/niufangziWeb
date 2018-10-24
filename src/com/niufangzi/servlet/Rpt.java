package com.niufangzi.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.sf.json.JSONObject;
import net.sf.json.JSONArray;


import com.mysql.jdbc.StringUtils;
import com.niufangzi.dao.CommonDao.PeriodType;
import com.niufangzi.dao.DivisionDao;
import com.niufangzi.dao.HouseChangeDao;
import com.niufangzi.dao.HouseSellingDao;
import com.niufangzi.dao.HouseSoldDao;
import com.niufangzi.dao.PhotoSellingDao;
import com.niufangzi.dao.PhotoSoldDao;
import com.niufangzi.dao.RptChangeDayDao;
import com.niufangzi.dao.RptSellingDayDao;
import com.niufangzi.dao.RptSoldDayDao;
import com.niufangzi.dao.WangqianDao;
import com.niufangzi.model.HouseChange;
import com.niufangzi.model.HouseSelling;
import com.niufangzi.model.HouseSold;
import com.niufangzi.model.OperationState;
import com.niufangzi.model.PageInfo;
import com.niufangzi.util.CacheUtil;
import com.niufangzi.util.ConfigUtil;
import com.niufangzi.util.UtilHelper;

public class Rpt extends HttpServlet {
	
	private static Logger logger = LogManager.getLogger();
	static String SUCCESSCODE = "000000";
	private static String API_VERSION = "1.0";
	/**
	 * Destruction of the servlet. <br>
	 */
	public void destroy() {
		super.destroy(); // Just puts "destroy" string in log
		// Put your code here
	}

	private static String GetIPAddress(HttpServletRequest request) { 
		String ip = request.getHeader("x-forwarded-for"); 
		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) { 
			ip = request.getHeader("Proxy-Client-IP"); 
		} 
		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) { 
			ip = request.getHeader("WL-Proxy-Client-IP"); 
		} 
		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) { 
			ip = request.getHeader("HTTP_CLIENT_IP"); 
		} 
		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) { 
			ip = request.getHeader("HTTP_X_FORWARDED_FOR"); 
		} 
		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) { 
			ip = request.getRemoteAddr(); 
		} 
		if (ip.equalsIgnoreCase("0:0:0:0:0:0:0:1")) {
			ip = "127.0.0.1";
		}
		return ip; 
	} 
	
	//获取统计周期参数
	private PeriodType GetPeriodTypePara(HttpServletRequest request) {
		PeriodType pType = PeriodType.DAY;
		String periodType = request.getParameter("PeriodType");
		if (! StringUtils.isNullOrEmpty(periodType)) {
			if (periodType.equalsIgnoreCase("0"))
				pType = PeriodType.DAY;
			else if (periodType.equalsIgnoreCase("1"))
				pType = PeriodType.WEEK;
			else if (periodType.equalsIgnoreCase("2"))
				pType = PeriodType.MONTH;
			else if (periodType.equalsIgnoreCase("3"))
				pType = PeriodType.SEASON;
			else if (periodType.equalsIgnoreCase("4"))
				pType = PeriodType.YEAR;
			else
				pType = PeriodType.DAY;
		}
		return pType;
	}
	
	
	//API_1 DealSoldCount - 成交套数
	private JSONObject DealSoldCount(HttpServletRequest request, String city, int maxCount)
	{
		JSONObject respJson = new JSONObject();
		List<Integer> list_count = new ArrayList<Integer>();
		List<String> list_date =  new ArrayList<String>();
		PeriodType pType = GetPeriodTypePara(request); //统计周期参数
		
		if (city.equalsIgnoreCase("北京")) //北京市有建委的完整网签数据， 其他城市只能统计链家的成交数据
			WangqianDao.QueryRecords(city,  maxCount, pType, list_count, list_date);
		else
			RptSoldDayDao.QueryCountRecords(city, maxCount, pType, list_count, list_date);
		
		JSONArray records = new JSONArray();
		for (int i=0; i<list_count.size(); i++)
		{
			JSONObject rec = new JSONObject();
			rec.put("Count", list_count.get(i));
			rec.put("Date", list_date.get(i));
			records.add(rec);
		}
		respJson.put("Records", records);
		respJson.put("ReturnCode", SUCCESSCODE);
		respJson.put("ReturnMsg", "");
		return respJson;
	}
	
	//API_2 DealSoldPrice - 成交均价
	private JSONObject DealSoldPrice(HttpServletRequest request, String city, int maxCount)
	{
		JSONObject respJson = new JSONObject();
		List<Integer> list_price = new ArrayList<Integer>();
		List<String> list_date =  new ArrayList<String>();
		PeriodType pType = GetPeriodTypePara(request); //统计周期参数
		boolean res = RptSoldDayDao.QueryPriceRecords(city, maxCount, pType, list_price, list_date);
		
		JSONArray records = new JSONArray();
		for (int i=0; i<list_price.size(); i++)
		{
			JSONObject rec = new JSONObject();
			rec.put("Price", list_price.get(i));
			rec.put("Date", list_date.get(i));
			records.add(rec);
		}
		respJson.put("Records", records);
		respJson.put("ReturnCode", SUCCESSCODE);
		respJson.put("ReturnMsg", "");
		return respJson;
	}
	
	//API_3 DealSoldList - 成交记录列表
	private JSONObject DealSoldList(HttpServletRequest request, String city)
	{
		JSONObject respJson = new JSONObject();
		OperationState state = new OperationState();
		
		PageInfo pi = new PageInfo();
		//1. 每页记录条数 
		pi.setPageSize(ConfigUtil.getSoldListPageSize());
		//2. 页号
		String strPageNo = request.getParameter("PageNo");
		if (! StringUtils.isNullOrEmpty(strPageNo)) {
			int nTmp = UtilHelper.tryStrToInt(strPageNo, state);
			if (state.result)
			{
				if (nTmp > ConfigUtil.getMaxPageNo())
					nTmp = ConfigUtil.getMaxPageNo();
				if (nTmp > 0)
					pi.setPageNo(nTmp);
			}
		}
		//3. 过滤条件
		String queryFilterStr = "";
		String filterStr = request.getParameter("FilterStr");
		if (! StringUtils.isNullOrEmpty(filterStr)) {
			queryFilterStr = ConvertSellingFilterString(filterStr);  //selling 和 sold 数据库结构基本相同， 可共用此函数。
		}
		//4. 排序字段
		int nOrderField = 0;
		int nOrderType = 0;
		boolean fieldOK = false;
		String strOrderField = request.getParameter("OrderField");
		if (! StringUtils.isNullOrEmpty(strOrderField)) {
			int nTmp = UtilHelper.tryStrToInt(strOrderField, state);
			if (state.result)
			{
				if (nTmp>=0 && nTmp<=4) {
					nOrderField = nTmp;
					fieldOK = true;
				}
			}
		}
		//5. 排序方式
		if (fieldOK) {
			String strOrderType = request.getParameter("OrderType");
			if (! StringUtils.isNullOrEmpty(strOrderType)) {
				int nTmp = UtilHelper.tryStrToInt(strOrderType, state);
				if (state.result)
				{
					if (nTmp>=0 && nTmp<=4) {
						nOrderType = nTmp;
					}
				}
			}
		}
		
		List<HouseSold> list_sold = new ArrayList<HouseSold>();
		
		//设置总记录条数、总页数信息
		int nTotalRecord = HouseSoldDao.QueryTotalCount(city, queryFilterStr);
		pi.setTotalRecord(nTotalRecord);
		int nTotalPage = (nTotalRecord-1)/pi.getPageSize() + 1;
		if (nTotalPage > ConfigUtil.getMaxPageNo())
			nTotalPage = ConfigUtil.getMaxPageNo();
		pi.setTotalPage(nTotalPage);
		
		boolean res = HouseSoldDao.QueryRecords(city, queryFilterStr, nOrderField, nOrderType, pi, list_sold);
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
		
		JSONArray records = new JSONArray();
		for (int i=0; i<list_sold.size(); i++)
		{
			HouseSold house = list_sold.get(i);
			if(house.getFangwuhuxing() == null)
			{
				house.setFangwuhuxing("");
			}
			JSONObject rec = new JSONObject();
			rec.put("chengshi", house.getChengshi());
			rec.put("quyu1", house.getQuyu1());
			rec.put("quyu2", house.getQuyu2());
			rec.put("xiaoqu", house.getXiaoqu());
			rec.put("houseId", house.getHouseId());
			rec.put("fangwuhuxing", house.getFangwuhuxing());
			rec.put("jianzhumianji", house.getJianzhumianji());
			rec.put("fangwuchaoxiang", house.getFangwuchaoxiang());
			rec.put("zhuangxiuqingkuang", house.getZhuangxiuqingkuang());
			rec.put("suozailouceng", house.getSuozailouceng());
			rec.put("zonglouceng", house.getZonglouceng());
			rec.put("jianzhuleixing", house.getJianzhuleixing());
			rec.put("jianchengniandai", house.getJianchengniandai());
			rec.put("guapaishijian", formatter.format(house.getGuapaishijian()));
			rec.put("fangwunianxian", house.getFangwunianxian());
			rec.put("guapaijiage", house.getGuapaijiage());
			rec.put("chengjiaojiage", house.getChengjiaojiage());
			rec.put("chengjiaoriqi", formatter.format(house.getChengjiaoriqi()));
			rec.put("chengjiaotujing", house.getChengjiaotujing());
			rec.put("guanzhu", house.getGuanzhu());
			rec.put("daikan", house.getDaikan());
			rec.put("tiaojia", house.getTiaojia());
			rec.put("zhouqi", house.getZhouqi());
			records.add(rec);
		}
		respJson.put("Records", records);
		respJson.put("TotalPage", pi.getTotalPage());
		respJson.put("TotalRecord", pi.getTotalRecord());
		respJson.put("ReturnCode", SUCCESSCODE);
		respJson.put("ReturnMsg", "");
		return respJson;
	}
	
	//API_4 DealSellingCount - 挂牌在售套数
	private JSONObject DealSellingCount(HttpServletRequest request, String city, int maxCount)
	{
		JSONObject respJson = new JSONObject();
		List<Integer> list_count = new ArrayList<Integer>();
		List<String> list_date =  new ArrayList<String>();
		PeriodType pType = GetPeriodTypePara(request); //统计周期参数
		
		RptSellingDayDao.QueryCountRecords(city, maxCount, pType, list_count, list_date);
		
		JSONArray records = new JSONArray();
		for (int i=0; i<list_count.size(); i++)
		{
			JSONObject rec = new JSONObject();
			rec.put("Count", list_count.get(i));
			rec.put("Date", list_date.get(i));
			records.add(rec);
		}
		respJson.put("Records", records);
		respJson.put("ReturnCode", SUCCESSCODE);
		respJson.put("ReturnMsg", "");
		return respJson;
	}
	
	//API_5 DealSellingPrice - 挂牌在售均价
	private JSONObject DealSellingPrice(HttpServletRequest request, String city, int maxCount)
	{
		JSONObject respJson = new JSONObject();
		List<Integer> list_price = new ArrayList<Integer>();
		List<String> list_date =  new ArrayList<String>();
		PeriodType pType = GetPeriodTypePara(request); //统计周期参数
		
		boolean res = RptSellingDayDao.QueryPriceRecords(city, maxCount, pType, list_price, list_date);
		
		JSONArray records = new JSONArray();
		for (int i=0; i<list_price.size(); i++)
		{
			JSONObject rec = new JSONObject();
			rec.put("Price", list_price.get(i));
			rec.put("Date", list_date.get(i));
			records.add(rec);
		}
		respJson.put("Records", records);
		respJson.put("ReturnCode", SUCCESSCODE);
		respJson.put("ReturnMsg", "");
		return respJson;
	}
	
	//将json字符串格式的查询条件转换成 sql查询where里的条件字符串
	private String ConvertSellingFilterString(String filterStr)
	{
		String queryFilter = "";
		JSONObject json = JSONObject.fromObject(filterStr);
		if (json.containsKey("area"))
			queryFilter = queryFilter + " and quyu1='" + json.getString("area").replace(" ", "") + "'";
		
		OperationState state = new OperationState();
		if (json.containsKey("priceMin"))
		{
			int nTmp = UtilHelper.tryStrToInt(json.getString("priceMin").replace(" ", ""), state);
			if (state.result)
			{
				queryFilter = queryFilter + " and guapaijiage>=" + nTmp;
			}
		}
		if (json.containsKey("priceMax"))
		{
			int nTmp = UtilHelper.tryStrToInt(json.getString("priceMax").replace(" ", ""), state);
			if (state.result)
			{
				queryFilter = queryFilter + " and guapaijiage<=" + nTmp;
			}
		}	
		if (json.containsKey("squareMin"))
		{
			int nTmp = UtilHelper.tryStrToInt(json.getString("squareMin").replace(" ", ""), state);
			if (state.result)
			{
				queryFilter = queryFilter + " and jianzhumianji>=" + nTmp;
			}
		}
		if (json.containsKey("squareMax"))
		{
			int nTmp = UtilHelper.tryStrToInt(json.getString("squareMax").replace(" ", ""), state);
			if (state.result)
			{
				queryFilter = queryFilter + " and jianzhumianji<=" + nTmp;
			}
		}
		if (json.containsKey("room"))
			queryFilter = queryFilter + " and fangwuhuxing like '" + json.getString("room").replace(" ", "") + "%'";
		
		return queryFilter;
	}
	
	//API_6 DealSellingList - 在售记录列表
	private JSONObject DealSellingList(HttpServletRequest request, String city)
	{
		JSONObject respJson = new JSONObject();
		OperationState state = new OperationState();
		
		PageInfo pi = new PageInfo();
		//1. 每页记录条数
		pi.setPageSize(ConfigUtil.getSellingListPageSize());
		logger.info("pageSize:" + pi.getPageSize());
		//2. 页号
		String strPageNo = request.getParameter("PageNo");
		if (! StringUtils.isNullOrEmpty(strPageNo)) {
			int nTmp = UtilHelper.tryStrToInt(strPageNo, state);
			if (state.result)
			{
				if (nTmp > ConfigUtil.getMaxPageNo())
					nTmp = ConfigUtil.getMaxPageNo();
				if (nTmp > 0)
					pi.setPageNo(nTmp);
			}
		}
		//3. 过滤条件
		String queryFilterStr = "";
		String filterStr = request.getParameter("FilterStr");
		if (! StringUtils.isNullOrEmpty(filterStr)) {
			queryFilterStr = ConvertSellingFilterString(filterStr);
		}
		//4. 排序字段
		int nOrderField = 0;
		int nOrderType = 0;
		boolean fieldOK = false;
		String strOrderField = request.getParameter("OrderField");
		if (! StringUtils.isNullOrEmpty(strOrderField)) {
			int nTmp = UtilHelper.tryStrToInt(strOrderField, state);
			if (state.result)
			{
				if (nTmp>=0 && nTmp<=4) {
					nOrderField = nTmp;
					fieldOK = true;
				}
			}
		}
		//5. 排序方式
		if (fieldOK) {
			String strOrderType = request.getParameter("OrderType");
			if (! StringUtils.isNullOrEmpty(strOrderType)) {
				int nTmp = UtilHelper.tryStrToInt(strOrderType, state);
				if (state.result)
				{
					if (nTmp>=0 && nTmp<=4) {
						nOrderType = nTmp;
					}
				}
			}
		}
		
		List<HouseSelling> list_selling = new ArrayList<HouseSelling>();
		
		//设置总记录条数、总页数信息
		int nTotalRecord = HouseSellingDao.QueryTotalCount(city, queryFilterStr);
		pi.setTotalRecord(nTotalRecord);
		int nTotalPage = (nTotalRecord-1)/pi.getPageSize() + 1;
		if (nTotalPage > ConfigUtil.getMaxPageNo())
			nTotalPage = ConfigUtil.getMaxPageNo();
		pi.setTotalPage(nTotalPage);
		
		boolean res = HouseSellingDao.QueryRecords(city, queryFilterStr, nOrderField, nOrderType, pi, list_selling);
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
		
		JSONArray records = new JSONArray();
		for (int i=0; i<list_selling.size(); i++)
		{
			HouseSelling house = list_selling.get(i);
			if(house.getFangwuhuxing() == null)
			{
				house.setFangwuhuxing("");
			}
			JSONObject rec = new JSONObject();
			rec.put("chengshi", house.getChengshi());
			rec.put("quyu1", house.getQuyu1());
			rec.put("quyu2", house.getQuyu2());
			rec.put("xiaoqu", house.getXiaoqu());
			rec.put("houseId", house.getHouseId());
			rec.put("fangwuhuxing", house.getFangwuhuxing());
			rec.put("jianzhumianji", house.getJianzhumianji());
			rec.put("fangwuchaoxiang", house.getFangwuchaoxiang());
			rec.put("zhuangxiuqingkuang", house.getZhuangxiuqingkuang());
			rec.put("suozailouceng", house.getSuozailouceng());
			rec.put("zonglouceng", house.getZonglouceng());
			rec.put("jianzhuleixing", house.getJianzhuleixing());
			rec.put("jianchengniandai", house.getJianchengniandai());
			rec.put("guapaishijian", formatter.format(house.getGuapaishijian()));
			rec.put("fangwunianxian", house.getFangwunianxian());
			rec.put("guapaijiage", house.getGuapaijiage());
			rec.put("guanzhu", house.getGuanzhu());
			rec.put("daikan", house.getDaikan());
			records.add(rec);
		}
		respJson.put("Records", records);
		respJson.put("TotalPage", pi.getTotalPage());
		respJson.put("TotalRecord", pi.getTotalRecord());
		respJson.put("ReturnCode", SUCCESSCODE);
		respJson.put("ReturnMsg", "");
		return respJson;
	}
	
	//API_7 DealChangeCount - 调价次数
	private JSONObject DealChangeCount(HttpServletRequest request, String city, int maxCount)
	{
		JSONObject respJson = new JSONObject();
		List<String> list_date =  new ArrayList<String>();
		List<Integer> list_count = new ArrayList<Integer>();
		List<Float> list_rate = new ArrayList<Float>();
		PeriodType pType = GetPeriodTypePara(request); //统计周期参数
		
		RptChangeDayDao.QueryRecords(city, maxCount, pType, list_date, list_count, list_rate);
		
		JSONArray records = new JSONArray();
		for (int i=0; i<list_count.size(); i++)
		{
			JSONObject rec = new JSONObject();
			rec.put("Count", list_count.get(i));
			rec.put("Date", list_date.get(i));
			records.add(rec);
		}
		respJson.put("Records", records);
		respJson.put("ReturnCode", SUCCESSCODE);
		respJson.put("ReturnMsg", "");
		return respJson;
	}
	
	//API_8 DealChangeRate - 调价幅度
	private JSONObject DealChangeRate(HttpServletRequest request, String city, int maxCount)
	{
		JSONObject respJson = new JSONObject();
		List<String> list_date =  new ArrayList<String>();
		List<Integer> list_count = new ArrayList<Integer>();
		List<Float> list_rate = new ArrayList<Float>();
		PeriodType pType = GetPeriodTypePara(request); //统计周期参数
		
		RptChangeDayDao.QueryRecords(city, maxCount, pType, list_date, list_count, list_rate);
		
		JSONArray records = new JSONArray();
		for (int i=0; i<list_rate.size(); i++)
		{
			JSONObject rec = new JSONObject();
			rec.put("Rate", list_rate.get(i));
			rec.put("Date", list_date.get(i));
			records.add(rec);
		}
		respJson.put("Records", records);
		respJson.put("ReturnCode", SUCCESSCODE);
		respJson.put("ReturnMsg", "");
		return respJson;
	}
	
	//计算调价幅度 %
	private float CalcChangeRate(float oldPrice, float newPrice)
	{
		if (oldPrice == 0)
			return 0;
		else {
			float a = (newPrice - oldPrice)*100 / oldPrice;
			BigDecimal bd = new BigDecimal(a);  
			float d = bd.setScale(2,  BigDecimal.ROUND_HALF_UP).floatValue(); //四舍五入
			return d;	
		}
	}
	
	//API_9 DealChangeList - 挂牌价格变化记录列表
	private JSONObject DealChangeList(HttpServletRequest request, String city)
	{
		JSONObject respJson = new JSONObject();
		
		PageInfo pi = new PageInfo();
		String strPageSize = request.getParameter("PageSize");
		String strPageNo = request.getParameter("PageNo");
		//1. 每页的记录条数
		if (! StringUtils.isNullOrEmpty(strPageSize)) {
			OperationState state = new OperationState();
			int nTmp = UtilHelper.tryStrToInt(strPageSize, state);
			if (state.result)
			{
				if (nTmp > ConfigUtil.getMaxPageSize())
					nTmp = ConfigUtil.getMaxPageSize();
				if (nTmp>0)
					pi.setPageSize(nTmp);
			}
		}
		//2. 页号
		if (! StringUtils.isNullOrEmpty(strPageNo)) {
			OperationState state = new OperationState();
			int nTmp = UtilHelper.tryStrToInt(strPageNo, state);
			if (state.result)
			{
				if (nTmp > ConfigUtil.getMaxPageNo())
					nTmp = ConfigUtil.getMaxPageNo();
				if (nTmp > 0)
					pi.setPageNo(nTmp);
			}
		}

		//3. 过滤条件
		String queryFilterStr = "";
		String qu = request.getParameter("Qu");
		if (! StringUtils.isNullOrEmpty(qu)) {
			qu = qu.replace(" ", "");
			queryFilterStr = " and quyu1='" + qu + "' ";
		}
		
		List<HouseChange> list_change = new ArrayList<HouseChange>();
		
		//设置总记录条数、总页数信息
		int nTotalRecord = HouseChangeDao.QueryTotalCount(city, queryFilterStr);
		pi.setTotalRecord(nTotalRecord);
		int nTotalPage = (nTotalRecord-1)/pi.getPageSize() + 1;
		if (nTotalPage > ConfigUtil.getMaxPageNo())
			nTotalPage = ConfigUtil.getMaxPageNo();
		pi.setTotalPage(nTotalPage);
		
		boolean res = HouseChangeDao.QueryRecords(city, queryFilterStr, pi, list_change);
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
		
		JSONArray records = new JSONArray();
		for (int i=0; i<list_change.size(); i++)
		{
			HouseChange hc = list_change.get(i);
			if(hc.getFangwuhuxing() == null)
			{
				hc.setFangwuhuxing("");
			}
			
			JSONObject rec = new JSONObject();
			rec.put("houseId", hc.getHouseId());
			rec.put("quyu1", hc.getQuyu1());
			rec.put("quyu2", hc.getQuyu2());
			rec.put("xiaoqu", hc.getXiaoqu());
			rec.put("fangwuhuxing", hc.getFangwuhuxing());
			rec.put("jianzhumianji", hc.getJianzhumianji());
			rec.put("oldPrice", hc.getOldPrice());
			rec.put("newPrice", hc.getNewPrice());
			//rec.put("changeRate", alcChangeRate(hc.getOldPrice(), hc.getNewPrice()));
			rec.put("oldDate", formatter.format(hc.getOldDate()));
			rec.put("createTime", formatter.format(hc.getCreateTime()));	
			int changeTimes = 1;
			if (CacheUtil.getChangeTimesMap().containsKey(hc.getHouseId())) {  //从缓存中查询
				changeTimes = CacheUtil.getChangeTimesMap().get(hc.getHouseId()); 
			}
			rec.put("changeTimes", changeTimes);
			records.add(rec);
		}
		respJson.put("Records", records);
		respJson.put("TotalPage", pi.getTotalPage());
		respJson.put("ReturnCode", SUCCESSCODE);
		respJson.put("ReturnMsg", "");
		return respJson;
	}
	
	//API_10 DealChangeHistory - 某套房子调价历史记录
	private JSONObject DealChangeHistory(HttpServletRequest request)
	{
		JSONObject respJson = new JSONObject();
		String houseId = request.getParameter("HouseId");
		if (StringUtils.isNullOrEmpty(houseId)) {
			respJson.put("ReturnCode", "100001");
			respJson.put("ReturnMsg", "HouseId is null");
			return respJson;
		}
		
		List<HouseChange> list_change = new ArrayList<HouseChange>();
		boolean res = HouseChangeDao.QueryRecordsById(houseId, list_change);
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
		JSONArray records = new JSONArray();
		for (int i=0; i<list_change.size(); i++)
		{
			HouseChange hc = list_change.get(i);
			JSONObject rec = new JSONObject();
			rec.put("oldPrice", hc.getOldPrice());
			rec.put("newPrice", hc.getNewPrice());
			//rec.put("changeRate", alcChangeRate(hc.getOldPrice(), hc.getNewPrice()));
			rec.put("createTime", formatter.format(hc.getCreateTime()));			
			records.add(rec);
		}
		respJson.put("Records", records);
		respJson.put("ReturnCode", SUCCESSCODE);
		respJson.put("ReturnMsg", "");
		return respJson;
	}
	
	//API_11 DealPhotoContent - 根据图片URL获取图片的base64编码(涉及到跨域，不能直接引用此图片)
	private JSONObject DealPhotoContent(HttpServletRequest request)
	{
		JSONObject respJson = new JSONObject();
		String photoURL = request.getParameter("PhotoURL");
		if (StringUtils.isNullOrEmpty(photoURL)) {
			respJson.put("ReturnCode", "110001");
			respJson.put("ReturnMsg", "PhotoURL is null");
			return respJson;
		}
		String photoContent = UtilHelper.encodeImageToBase64(photoURL);
		if (StringUtils.isNullOrEmpty(photoContent)) {
			respJson.put("ReturnCode", "110002");
			respJson.put("ReturnMsg", "Photo is not exist");
			return respJson;
		}
		photoContent = "data:image/jpg;base64, " + photoContent;
		respJson.put("PhotoContent", photoContent);
		respJson.put("ReturnCode", SUCCESSCODE);
		respJson.put("ReturnMsg", "");
		return respJson;
	}
	
	//API_12 DealHuxingPhotoContent - 根据houseId, 获取其对应的户型图的base64编码
	private JSONObject DealHuxingPhotoContent(HttpServletRequest request, String city)
	{
		JSONObject respJson = new JSONObject();
		String houseId = request.getParameter("HouseId");
		if (StringUtils.isNullOrEmpty(houseId)) {
			respJson.put("ReturnCode", "120001");
			respJson.put("ReturnMsg", "HouseId is null");
			return respJson;
		}
		
		String photoURL = "";
		String strType = request.getParameter("HouseType");
		if (!StringUtils.isNullOrEmpty(strType) && strType.equalsIgnoreCase("1")) {  //0：selling; 1:sold;
			
			photoURL = PhotoSoldDao.GetPhotoURL(city, houseId, "户型图");
		}
		else
			photoURL = PhotoSellingDao.GetPhotoURL(city, houseId, "户型图");
		
		if (StringUtils.isNullOrEmpty(photoURL)) {
			respJson.put("ReturnCode", "120002");
			respJson.put("ReturnMsg", "Photo is not exist");
			return respJson;
		}
		
		String photoContent = UtilHelper.encodeImageToBase64(photoURL);
		if (StringUtils.isNullOrEmpty(photoContent)) {
			respJson.put("ReturnCode", "120003");
			respJson.put("ReturnMsg", "Failed to get photo content");
			return respJson;
		}
		photoContent = "data:image/jpg;base64, " + photoContent;
		respJson.put("PhotoContent", photoContent);
		respJson.put("ReturnCode", SUCCESSCODE);
		respJson.put("ReturnMsg", "");
		return respJson;
	}
	
	//API_13 DealQuInCity - 查询某个市下所有区 列表
	private JSONObject DealQuInCity(HttpServletRequest request, String city)
	{
		JSONObject respJson = new JSONObject();
		List<String> list_qu = new ArrayList<String>();
		//DivisionDao.GetQuInCity(city, list_qu);
		CacheUtil.GetQuInCity(city, list_qu);  //从缓存中取
		
		JSONArray records = new JSONArray();
		for (int i=0; i<list_qu.size(); i++)
		{
			JSONObject rec = new JSONObject();
			rec.put("qu", list_qu.get(i));
			records.add(rec);
		}
		respJson.put("Records", records);
		respJson.put("ReturnCode", SUCCESSCODE);
		respJson.put("ReturnMsg", "");
		return respJson;
	}
	
	
	private void doRequest(HttpServletRequest request, int reqType, HttpServletResponse response)
			throws ServletException, IOException {		
		
		response.setContentType("text/html;charset=utf-8");
		//yef_20180329 允许跨域访问此接口
		if (ConfigUtil.getAllowedCrossDomain()) {
			response.setHeader("Access-Control-Allow-Origin","*");
		}
		PrintWriter out = response.getWriter();
		
		try {
			request.setCharacterEncoding("utf-8");
		} catch (UnsupportedEncodingException e) {
			logger.error(e.toString());
		}
		
		
		//记录request信息日志
		String ip = GetIPAddress(request);
		String strReq = " req - from " + ip + " : " + request.getRequestURL().toString();
		if (request.getQueryString() != null)
		{
			strReq = strReq +  "?" + request.getQueryString();
		}
		logger.debug(strReq);
		if (reqType == 1) //Post请求
		{
			String strParas = "";
			Map<String,String[]> map=request.getParameterMap();    
			for(Iterator iter=map.entrySet().iterator();iter.hasNext();)
			{  
				Map.Entry element=(Map.Entry)iter.next();  
				Object strKey = element.getKey();  //key值  
		        String[] value=(String[])element.getValue(); //value,数组形式    
		        String strTmp = "";
		        for(int i=0;i<value.length;i++){
		        	if (i==0)
		        		strTmp = value[i];
		        	else
		        		strTmp = strTmp + "," + value[i];
		        }
		        strParas = strParas + "&" + strKey.toString() + "=" + strTmp;
			}
			logger.debug("     - post: " + strParas);
		}
		
		
		JSONObject respJson = new JSONObject();
		String method = request.getParameter("Method");
		String version = request.getParameter("Version");
		do {
			if (StringUtils.isNullOrEmpty(method)){
				respJson.put("ReturnCode", "000001");
				respJson.put("ReturnMsg", "Method not exist");
				break;
			}
			/* 暂不检查参数：version
			if (StringUtils.isNullOrEmpty(version)){
				respJson.put("ReturnCode", "000003");
				respJson.put("ReturnMsg", "Version not exist");
				break;
			}
			if (! version.equalsIgnoreCase(API_VERSION)){  //检查接口版本号
				respJson.put("ReturnCode", "000004");
				respJson.put("ReturnMsg", "Version error");
				break;
			}
			*/

			//处理公共参数
			String city = "北京";
			int maxCount = 1000;
			String pCity = request.getParameter("City");
			String pMaxCount = request.getParameter("MaxCount");
			if (! StringUtils.isNullOrEmpty(pCity)) {
				if (pCity.equalsIgnoreCase("bj"))
					city = "北京";
				else if (pCity.equalsIgnoreCase("wh"))
					city = "武汉";
				else if (pCity.equalsIgnoreCase("lf"))
					city = "廊坊";
				else
					city = pCity;
			}
			if (! StringUtils.isNullOrEmpty(pMaxCount)) {
				OperationState state = new OperationState();
				int nTmp = UtilHelper.tryStrToInt(pMaxCount, state);
				if (state.result)
				{
					if (nTmp <= 1000 && nTmp>=10)
						maxCount = nTmp;
				}
			}
			
			//根据接口名称，调用对应函数处理
			if (method.equalsIgnoreCase("SoldCount")) {
				respJson = DealSoldCount(request, city, maxCount);
				break;
			}
			else if (method.equalsIgnoreCase("SoldPrice")) {
				respJson = DealSoldPrice(request, city, maxCount);
				break;
			}
			else if (method.equalsIgnoreCase("SoldList")) {
				respJson = DealSoldList(request, city);
				break;
			}
			if (method.equalsIgnoreCase("SellingCount")) {
				respJson = DealSellingCount(request, city, maxCount);
				break;
			}
			else if (method.equalsIgnoreCase("SellingList")) {
				respJson = DealSellingList(request, city);
				break;
			}
			else if (method.equalsIgnoreCase("SellingPrice")) {
				respJson = DealSellingPrice(request, city, maxCount);
				break;
			}
			else if (method.equalsIgnoreCase("ChangeCount")) {
				respJson = DealChangeCount(request, city, maxCount);
				break;
			}
			else if (method.equalsIgnoreCase("ChangeRate")) {
				respJson = DealChangeRate(request, city, maxCount);
				break;
			}
			else if (method.equalsIgnoreCase("ChangeList")) {
				respJson = DealChangeList(request, city);
				break;
			}
			else if (method.equalsIgnoreCase("ChangeHistory")) {
				respJson = DealChangeHistory(request);
				break;
			}
			else if (method.equalsIgnoreCase("PhotoContent")) {
				respJson = DealPhotoContent(request);
				break;
			}
			else if (method.equalsIgnoreCase("HuxingPhotoContent")) {
				respJson = DealHuxingPhotoContent(request, city);
				break;
			}
			else if (method.equalsIgnoreCase("QuInCity")) {
				respJson = DealQuInCity(request, city);
				break;
			}
			else {
				respJson.put("ReturnCode", "000002");
				respJson.put("ReturnMsg", "Method error");
				break;
			}
		} while(false);
		out.print(respJson.toString());
		out.flush();
		out.close();
		
		logger.debug("resp - " + respJson.toString());
	}
	
	public void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		doRequest(request, 0, response);
	}

	public void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		logger.debug("referer : " + request.getHeader("referer"));
		doRequest(request, 1, response);
	}

	/**
	 * Initialization of the servlet. <br>
	 *
	 * @throws ServletException if an error occurs
	 */
	public void init() throws ServletException {
		// Put your code here
	}

}
