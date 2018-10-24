package com.niufangzi.task;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import com.niufangzi.Parser.WangqianParser;
import com.niufangzi.dao.WangqianDao;
import com.niufangzi.model.Wangqian;
import com.niufangzi.util.UtilHelper;

public class WangqianTask {
	private static Logger logger = LogManager.getLogger();
	
	public static boolean DoTask()
	{
		Wangqian wq = new Wangqian();
		boolean res = WangqianParser.ParseWangqian(wq);
		if (! res)
		{
			logger.error("ParseWangqian error");
			return false;
		}
		WangqianDao.DeleteWangqian(wq.getWangqianDate());
		res = WangqianDao.AddWangqian(wq);
		logger.info("ParseWangqian: " + res);
		
		String str = UtilHelper.ObjectToString(wq);
		logger.info("wq: \n" + str);
		return res;
	}
	
	private static void DoTest()
	{
		//
		/*
		java.util.Date dd = new java.util.Date();
		System.out.println(dd.toString());
		System.out.println(dd.toLocaleString());
		System.out.println(dd.toGMTString());
		System.out.println("===========================");
		System.out.println(dd.getDay());
		System.out.println("===========================");
		System.out.println(dd.getHours());
		System.out.println(dd.getMinutes());
		System.out.println(dd.getSeconds());
		System.out.println("===========================");
		System.out.println(dd.getYear());
		System.out.println(dd.getMonth());
		System.out.println(dd.getDate());
		//System.out.println(dd);
		*/
		//java.sql.Timestamp ts = new java.sql.Timestamp(System.currentTimeMillis());  
		//java.lang.Math.s
	}


	/**
	 * @param args
	 */
	public static void main(String[] args) {
		DoTask();

		//DoTest();
	}

}
