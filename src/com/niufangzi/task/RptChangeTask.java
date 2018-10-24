package com.niufangzi.task;

import java.util.Calendar;
import java.util.Date;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import com.niufangzi.dao.RptChangeDayDao;
import com.niufangzi.model.OperationState;
import com.niufangzi.util.UtilHelper;


public class RptChangeTask {

	private static Logger logger = LogManager.getLogger();
	
	public static Boolean DoTask()
	{
		logger.info("Change Report CityDayTask Begin ");
		DoCityDayTask();
		logger.info("Change Report CityDayTask Completed ");
		
		return true;
	}

	
	//'调价'报表(市-日 )
	private static Boolean DoCityDayTask()
	{
		OperationState state = new OperationState();
		Date dBegin = RptChangeDayDao.QueryMaxDate(state);
		String strBegin = dBegin.toLocaleString();
		if (! state.result) //如果此表中尚无记录，则取一个包含所有调价记录的较小时间即可。
		{
			Calendar c_begin = Calendar.getInstance();
			c_begin.setTime(new Date());
	    	c_begin.add(Calendar.YEAR, -3);  //从3年前开始
	    	dBegin = c_begin.getTime();
	    	strBegin = "long long ago";
		};
		logger.info("refresh change report from " + strBegin);
		Date dStart = new Date();
		RptChangeDayDao.SumDays(dBegin);
		Date dEnd = new Date();
	    long secs = dEnd.getTime() - dStart.getTime();
	    logger.info("完成， 耗时：" + secs + " 毫秒");

		return true;
	}
	
	 
	
	private static void DoTest() {
		//
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		//DoTest();
		
		DoTask();
		
	}

}
