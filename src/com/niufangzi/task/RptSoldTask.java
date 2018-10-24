package com.niufangzi.task;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.niufangzi.Parser.URLHelper;
import com.niufangzi.dao.HouseSoldDao;
import com.niufangzi.dao.RptSoldDayDao;
import com.niufangzi.dao.RptSoldMonthDao;
import com.niufangzi.model.OperationState;
import com.niufangzi.util.UtilHelper;

public class RptSoldTask {

	private static Logger logger = LogManager.getLogger();
	//最早记录： 北京(2010-1-1),  廊坊(2012-7-3),  武汉(2016-1-22)
	
	
	public static Boolean DoTask()
	{
		logger.info("Sold Report CityDayTask Begin ");
		DoCityDayTask();
		logger.info("Sold Report CityDayTask Completed ");
		
		logger.info("Sold Report QuMonthTask Begin ");
		DoQuMonthTask();
		logger.info("Sold Report QuMonthTask Completed ");
		
		return true;
	}
	
	//'成交'报表(区-月) 
	private static Boolean DoQuMonthTask()
	{
		for(int cityIdx=0; cityIdx<URLHelper.GetCityCount(); cityIdx++)
		{
			OperationState cityState = new OperationState();
			String chengshi = URLHelper.GetCityName(cityIdx, cityState);
			if (!cityState.result)
			{
				logger.error("GetCityName " + cityIdx + " error");
				continue;
			}

			List<Integer> list_year = new ArrayList<Integer>();
			List<Integer> list_month = new ArrayList<Integer>();
			GetRemainMonth(chengshi, list_year, list_month); //查询报表数据库中已有记录，接着后面继续执行
			for(int i=0; i<list_year.size(); i++)
			{
				SumAQuMonth(chengshi, list_year.get(i), list_month.get(i));
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}	
			
		}
		return true;
	}
	
	//'成交'报表(市-日 )
	private static Boolean DoCityDayTask()
	{
		for(int cityIdx=0; cityIdx<URLHelper.GetCityCount(); cityIdx++)
		{
			OperationState cityState = new OperationState();
			String chengshi = URLHelper.GetCityName(cityIdx, cityState);
			if (! cityState.result)
			{				
				logger.error("GetCityName " + cityIdx + " error");
				continue;
			}
			
			List<Date> list_from = new ArrayList<Date>(); 
			List<Date> list_to = new ArrayList<Date>();
			GetRemainDay(chengshi, list_from, list_to);
			for(int i=0; i<list_from.size(); i++)
			{
				Date startDate = list_from.get(i);
				Date endDate = list_to.get(i);
				logger.info("    ");
				logger.info(chengshi + "  " + UtilHelper.DateToStr(startDate, "yyyy-MM-dd") + " <==> " + UtilHelper.DateToStr(endDate, "yyyy-MM-dd"));
				Date dStart = new Date();
				RptSoldDayDao.SumDays(chengshi, startDate, endDate);
				Date dEnd = new Date();
			    long secs = dEnd.getTime() - dStart.getTime();
			    logger.info("完成， 耗时：" + secs + " 毫秒");
			    try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			
		}

		return true;
	}
	
	private static Boolean SumAQuYear(String chengshi, int year)
	{
		for(int month=1; month<=12; month++)
		{
			SumAQuMonth(chengshi, year, month);
		    try {
				Thread.sleep(2000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		return true;
	}
	
	private static Boolean SumAQuMonth(String chengshi, int year, int month)
	{
		logger.info("    ");
		logger.info(chengshi + " : " + year + "-" + month);
		Date dStart = new Date();
		RptSoldMonthDao.SumAMonth(chengshi, year, month);
		Date dEnd = new Date();
	    long secs = dEnd.getTime() - dStart.getTime();
	    logger.info("完成， 耗时：" + secs + " 毫秒");
		return true;
	}

	//获取日报表尚未完成的日列表(进行拆分，每次任务最多100日)
	private static Boolean GetRemainDay(String city, List<Date> list_from, List<Date> list_to) {
		list_from.clear();
		list_to.clear();
		OperationState state = new OperationState();
		Date dBegin = RptSoldDayDao.QueryMaxDate(city, state);
		
		if (! state.result)
		{
			dBegin = HouseSoldDao.QueryMinDate(city, state);
			if (! state.result)
			{
				return false;
			}
		}
		Date dEnd = HouseSoldDao.QueryMaxDate(city, state);
		if (! state.result)
		{
			return false;
		}
		
		Calendar c_begin = Calendar.getInstance();
		c_begin.setTime(dBegin);
    	c_begin.add(Calendar.DATE, -14);  //重新生成最近15天的报表。（之前数据可能不完整，后续有补充的数据）
    	Date dFrom = c_begin.getTime();
    	
	    Calendar c_curr = Calendar.getInstance();
	    while(dFrom.compareTo(dEnd) <= 0)
	    {
	    	c_curr.setTime(dFrom);
	    	c_curr.add(Calendar.DATE, 99);
	    	Date dTo = c_curr.getTime();
	    	if (dTo.compareTo(dEnd) > 0)
	    	{
	    		dTo = dEnd;
	    	}
	    	list_from.add(dFrom);
	    	list_to.add(dTo);
	    	c_curr.setTime(dTo);
	    	c_curr.add(Calendar.DATE, 1);
	    	dFrom = c_curr.getTime();
	    }
	    return true;  
	}
	
	//获取月报表尚未生成的月份列表
	private static Boolean GetRemainMonth(String city, List<Integer> list_year, List<Integer> list_month) {
		list_year.clear();
		list_month.clear();
		OperationState state = new OperationState();
		Date dBegin = RptSoldMonthDao.QueryMaxDate(city, state);
		
		if (! state.result)
		{
			dBegin = HouseSoldDao.QueryMinDate(city, state);
			if (! state.result)
			{
				return false;
			}
		}
		Date dEnd = HouseSoldDao.QueryMaxDate(city, state);
		if (! state.result)
		{
			return false;
		}
		
		Calendar c_begin = Calendar.getInstance();
		c_begin.setTime(dBegin);
    	c_begin.add(Calendar.MONTH, -1);  //重新生成最近2个月的月报表。（之前数据可能不完整，后续有补充的数据）
    	dBegin = c_begin.getTime();
		
	    Calendar c_min = Calendar.getInstance();  
	    Calendar c_max = Calendar.getInstance(); 
	    c_min.setTime(dBegin);
    	c_max.setTime(dEnd); 
	    Calendar c_curr = c_min;  
	    while (c_curr.before(c_max)) {  
	    	list_year.add(c_curr.get(Calendar.YEAR));
	    	list_month.add(c_curr.get(Calendar.MONTH)+1);
	    	c_curr.add(Calendar.MONTH, 1);  
	    }
	    return true;  
	}
	
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		DoTask();
		
	}

}
