package com.niufangzi.listener;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.niufangzi.util.CacheUtil;
import com.niufangzi.util.ConfigUtil;

public class AutoRun implements ServletContextListener {

	private static Logger logger = LogManager.getLogger();
	
	@Override
    public void contextDestroyed(ServletContextEvent event) {
		// and store it as a context attribute
    }

    
	@Override
    public void contextInitialized(ServletContextEvent event) {
		CacheRefreshThread m_task = new CacheRefreshThread();
		m_task.start();	
		logger.info(" ************************* CacheRefreshThread is start");		
	}
	
	
	//
	class CacheRefreshThread extends Thread{
	    public void run(){	
	    	long i=0;
	    	long millis = ConfigUtil.getCacheRefreshInterval() * 1000; //转成毫秒
	        while(true)
	        {
	        	i++;
	        	logger.debug(" ************************* CacheRefreshThread " + i);
				try {
					CacheUtil.RefreshChangeTimes();
					CacheUtil.RefreshCity();
					Thread.sleep(millis);
				}catch (InterruptedException e) {
					logger.error(e.toString());
				}
	        }//while
	    }
	    
	}
	

	
	/**
	 * @param args
	 */
	public static void main(String[] args) {

		//
	}

}
