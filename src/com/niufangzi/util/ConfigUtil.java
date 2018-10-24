package com.niufangzi.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.niufangzi.model.OperationState;

public class ConfigUtil {
	private static Logger logger = LogManager.getLogger();

	private static int sellingRptDelay;          //selling报表延迟天数
	private static int soldRptDelay;             //sold报表延迟天数
	private static int cacheRefreshInterval;     //缓存内容的更新间隔时间(单位：分钟)
	
	private static int maxPageSize;              //允许的最大每页记录数
	private static int maxPageNo;                //允许的最大页号 
	
	private static int sellingListPageSize;      //selling记录列表每页记录数(取值范围：5-50)
	private static int soldListPageSize;         //sold记录列表每页记录数(取值范围：5-50)
	private static boolean allowedCrossDomain;   //是否允许跨域访问接口
	
	//静态代码块在类加载进内存的时候就自动执行
    static {  
        Properties prop = new Properties();  
        InputStream in = ConfigUtil.class.getClassLoader().getResourceAsStream("config.properties");  
        try {  
            prop.load(in);             
            setSellingRptDelay(UtilHelper.tryStrToInt(prop.getProperty("sellingRptDelay"), null));
            setSoldRptDelay(UtilHelper.tryStrToInt(prop.getProperty("soldRptDelay"), null));
            setCacheRefreshInterval(UtilHelper.tryStrToInt(prop.getProperty("cacheRefreshInterval"), null));
            setMaxPageSize(UtilHelper.tryStrToInt(prop.getProperty("maxPageSize"), null));
            setMaxPageNo(UtilHelper.tryStrToInt(prop.getProperty("maxPageNo"), null));
            setSellingListPageSize(UtilHelper.tryStrToInt(prop.getProperty("sellingListPageSize"), null));
            setSoldListPageSize(UtilHelper.tryStrToInt(prop.getProperty("soldListPageSize"), null));
            setAllowedCrossDomain(prop.getProperty("soldListPageSize"));
            logger.info("load config.properties ");
            logger.info("     sellingRptDelay: " + sellingRptDelay);
            logger.info("        soldRptDelay: " + soldRptDelay);
            logger.info("cacheRefreshInterval: " + cacheRefreshInterval);
            logger.info("         maxPageSize: " + maxPageSize);
            logger.info("           maxPageNo: " + maxPageNo);
            logger.info("  selingListPageSize: " + sellingListPageSize);
            logger.info("    soldListPageSize: " + soldListPageSize);
        }catch (Exception e) {  
            logger.error("read config.properties failed: " + e.toString());
        }  
    }  
    
    
	public static int getSellingRptDelay() {
		return sellingRptDelay;
	}
	private static void setSellingRptDelay(int nSellingRptDelay) {
		if (nSellingRptDelay < 1)
			ConfigUtil.sellingRptDelay = 1;
		else if (nSellingRptDelay > 30)
			ConfigUtil.sellingRptDelay = 30;
		else
			ConfigUtil.sellingRptDelay = nSellingRptDelay;
	}

	
	public static int getSoldRptDelay() {
		return soldRptDelay;
	}
	private static void setSoldRptDelay(int nSoldRptDelay) {
		if (nSoldRptDelay < 1) 
			ConfigUtil.soldRptDelay = 1;
		else if (nSoldRptDelay > 30) 
			ConfigUtil.soldRptDelay = 30;
		else 
			ConfigUtil.soldRptDelay = nSoldRptDelay;
	}

	
	public static int getCacheRefreshInterval() {
		return cacheRefreshInterval;
	}
	private static void setCacheRefreshInterval(int nCacheRefreshInterval) {
		if (nCacheRefreshInterval < 10)
			ConfigUtil.cacheRefreshInterval = 10;
		else if (nCacheRefreshInterval > 86400)
			ConfigUtil.cacheRefreshInterval = 86400;
		ConfigUtil.cacheRefreshInterval = nCacheRefreshInterval;
	}


	public static int getSellingListPageSize() {
		return sellingListPageSize;
	}
	private static void setSellingListPageSize(int nSellingListPageSize) {
		if (nSellingListPageSize < 5)
			ConfigUtil.sellingListPageSize = 5;
		else if (nSellingListPageSize > 50)
			ConfigUtil.sellingListPageSize = 50;
		else
			ConfigUtil.sellingListPageSize = nSellingListPageSize;
	}


	public static int getSoldListPageSize() {
		return soldListPageSize;
	}
	private static void setSoldListPageSize(int nSoldListPageSize) {
		if (nSoldListPageSize < 5)
			ConfigUtil.soldListPageSize = 5;
		else if (nSoldListPageSize > 50)
			ConfigUtil.soldListPageSize = 50;
		else
			ConfigUtil.soldListPageSize = nSoldListPageSize;
	}
    
	
    public static boolean getAllowedCrossDomain() {
		return allowedCrossDomain;
	}
	public static void setAllowedCrossDomain(String strAllowed) {
		if (strAllowed.equalsIgnoreCase("0"))
			ConfigUtil.allowedCrossDomain = false;
		else
			ConfigUtil.allowedCrossDomain = true;
	}
	
	public static int getMaxPageSize() {
		return maxPageSize;
	}
	public static void setMaxPageSize(int maxPageSize) {
		if (maxPageSize >= 10)
			ConfigUtil.maxPageSize = maxPageSize;
		else
			ConfigUtil.maxPageSize = 10;
	}
	
	
	public static int getMaxPageNo() {
		return maxPageNo;
	}
	public static void setMaxPageNo(int maxPageNo) {
		if (maxPageNo >= 10)
			ConfigUtil.maxPageNo = maxPageNo;
		else
			ConfigUtil.maxPageNo = 10;
	}
	
	
	
	
	
	
}
