package com.niufangzi.task;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.niufangzi.Parser.URLHelper;
import com.niufangzi.dao.BreakPointDao;
import com.niufangzi.dao.PhotoSellingDao;
import com.niufangzi.model.BreakPoint;
import com.niufangzi.model.OperationState;
import com.niufangzi.model.BreakPoint.TaskType;
import com.niufangzi.util.UtilHelper;


public class WhatToDo {
	private static Logger logger = LogManager.getLogger();

	public static boolean DoTask()
	{	
		/*
		for(int cityIdx=0; cityIdx<URLHelper.GetCityCount(); cityIdx++)
		{
			HouseSellingTask.DoSmallTask(cityIdx);
		}
		*/
		return true;
	}
	
	public static boolean DoTaskWithArgs(String[] args)
	{	
		//0-无参数，输出帮助信息后退出
		if (args.length == 0)
		{
			logger.info("need 2 paras:");
			logger.info("para1: 0-selling download; 1-sold download; 2-wangqian; 3-report");
			logger.info("para2 (for selling/sold download): 0-small; 1-middle; 2-huge;");
			return true;
		}
		
		//1-检查第一个参数
		OperationState typeState = new OperationState();
		int nType = UtilHelper.tryStrToInt(args[0], typeState);
		if (!typeState.result)
		{
			logger.info("para1 is not number");
			return false;
		}
		if (nType<0 || nType>3 )
		{
			logger.info("para1 is out of range[0,3]");
			return false;
		}
		
		//2-如果是selling/sold download任务，则需要第二个参数
		int nLevel = 0;
		if ((nType==0) || (nType==1))
		{
			OperationState levelState = new OperationState();
			nLevel = UtilHelper.tryStrToInt(args[1], levelState);
			if (!levelState.result)
			{
				logger.info("para2 is not number");
				return false;
			}
			if (nLevel<0 || nLevel>2)
			{
				logger.info("para2 is out of range[0, 2]");
				return false;
			}
		}
	
		
		//查找上次执行到哪个城市了
		int cityStart = 0;
		if (nType==0 || nType ==1)
		{
			for(int cityIdx=0; cityIdx<URLHelper.GetCityCount(); cityIdx++)
			{
				BreakPoint bp_last = BreakPointDao.GetBreakPoint(cityIdx, nType, nLevel);
				if (bp_last != null)
				{
					cityStart = cityIdx;
					break;
				}
			}
		}
		
		
		if (nType == 0) //selling任务
		{
			for(int cityIdx=cityStart; cityIdx<URLHelper.GetCityCount(); cityIdx++)
			{
				if (nLevel == 0) //按市
				{
					HouseSellingTask.DoSmallTask(cityIdx);
				}
				else if (nLevel == 1) //按区
				{
					HouseSellingTask.DoMidTask(cityIdx);
				}
				else if (nLevel == 2) //按街道
				{
					HouseSellingTask.DoHugeTask(cityIdx);
				}
			}
		}
		else if (nType == 1) //sold任务
		{
			for(int cityIdx=cityStart; cityIdx<URLHelper.GetCityCount(); cityIdx++)
			{
				if (nLevel == 0) //按市
				{
					HouseSoldTask.DoSmallTask(cityIdx);
				}
				else if (nLevel == 1) //按区
				{
					HouseSoldTask.DoMidTask(cityIdx);
				}
				else if (nLevel == 2) //按街道
				{
					HouseSoldTask.DoHugeTask(cityIdx);
				}
			}
		}
		else if (nType == 2) //wangqian任务
		{
			WangqianTask.DoTask();
		}
		else if (nType == 3) //报表任务
		{
			RptSellingTask.DoTask();
			RptSoldTask.DoTask();
			RptChangeTask.DoTask();
		}
		
		return true;
	}
	
	private static boolean DoTest()
	{
		//
		return true;
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		DoTaskWithArgs(args);
		
	}

}
