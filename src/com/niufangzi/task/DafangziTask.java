package com.niufangzi.task;


import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.niufangzi.Parser.DafangziParser;
import com.niufangzi.dao.WangqianDao;
import com.niufangzi.model.OperationState;
import com.niufangzi.model.Wangqian;
import com.niufangzi.util.ConfigUtil;
import com.niufangzi.util.UtilHelper;


public class DafangziTask {
	private static Logger logger = LogManager.getLogger();
	
	public static boolean DoTask()
	{
		List<Date> list_date = new ArrayList<Date>();
		List<Integer> list_count = new ArrayList<Integer>();
		boolean res = DafangziParser.ParseWangqian(list_date, list_count);
		if (! res)
		{
			logger.error("ParseDafangzi error");
			return false;
		}
		
		Wangqian wq = new Wangqian();
		for(int i=0; i<list_date.size(); i++)
		{
			if ((i % 100) == 0)
			{
				System.out.println("now is :" + i);
			}
			Wangqian wqTmp = WangqianDao.GetWangqian(list_date.get(i));
			if (wqTmp == null) //数据库中不存在
			{
				wq.setWangqianDate(list_date.get(i));
				wq.setCunCount(list_count.get(i));  //存量房网签数量(含住宅和非住宅)
				WangqianDao.AddWangqian(wq);
			}
		}
		
		logger.info("ParseDafangzi, records: " + list_date.size());
		return res;
	}
	
	private static boolean DoTest()
	{
		OperationState state = new OperationState();
		Date dt = UtilHelper.tryStrToDate("2018-01-25", "yyyy-MM-dd", state);
		Wangqian wq = WangqianDao.GetWangqian(dt);
		if (wq == null)
		{
			System.out.println("null");
		}
		else
		{
			String str = UtilHelper.ObjectToString(wq);
			System.out.println(str);
		}
		
		return true;
	}
	
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		//DoTask();
		
	}

}
