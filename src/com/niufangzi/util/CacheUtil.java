package com.niufangzi.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.niufangzi.dao.DivisionDao;
import com.niufangzi.dao.HouseChangeDao;

//将一些很少变化、经常访问的数据缓存在内存中，减少数据库访问
public class CacheUtil {
	private static Logger logger = LogManager.getLogger();

	//房屋的调价总次数信息(目前只保存调价次数大于1的记录)
	private static Map<String, Integer> map_changeTimes = new HashMap<String, Integer>();  
	
	private static Map<String, List<String>> map_division = new HashMap<String, List<String>>();
	
	    
    //静态代码块在类加载进内存的时候就自动执行
    /*
	static {  
        try {
        	RefreshChangeTimes();
        }catch (Exception e) {  
            logger.error("RefreshChangeTimes failed: " + e.toString());
        }  
    }  
	*/    

	public static Map<String, Integer> getChangeTimesMap() {
		return map_changeTimes;
	}
	
	
	public static boolean GetQuInCity(String city, List<String>list_qu)
	{
		list_qu.clear();
		if (map_division.containsKey(city)) {
			List<String> list_tmp = map_division.get(city);
			for(int i=0; i<list_tmp.size(); i++) {
				list_qu.add(list_tmp.get(i));
			}
			return true;
		}
		else {
			return false;
		}
	}
	
	public static void RefreshChangeTimes() {
		int minTimes =2; //最少两次
		Map<String, Integer> map_tmp = new HashMap<String, Integer>();
		boolean res = HouseChangeDao.QueryHouseChangeTimes(minTimes, map_tmp);
		int oldSize = map_changeTimes.size();
		if (res) {
			map_changeTimes.putAll(map_tmp); //将查询到的记录追到到原map, 如果原来已存在记录，则会更新value值。
		}
		logger.info("RefreshChangeTimes, oldSize:" + oldSize + "; newSize:" + map_changeTimes.size());
	}
	
	public static void RefreshCity() {
		map_division.clear();
		List<String> list_city = new ArrayList<String>();
		DivisionDao.GetCity(list_city);
		for(int i=0; i<list_city.size(); i++)
		{
			String city = list_city.get(i);
			List<String> list_qu = new ArrayList<String>();
			DivisionDao.GetQuInCity(city, list_qu);
			map_division.put(city, list_qu);
			logger.info("RefreshCity, city:" + city + ", quSize:" + list_qu.size());
		}
	}
	
}
