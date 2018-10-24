package com.niufangzi.dao;

public class CommonDao {
	
	public static enum PeriodType { DAY, WEEK, MONTH, SEASON, YEAR }; //统计周期
	
	//得到不同的统计周期对应的dateFormat
	public static String GetSumDateFormat(String dateField, PeriodType pType) {
		String strTotal = "";
		
		if (pType == PeriodType.SEASON) {
			strTotal = "concat(YEAR(" + dateField + "),'-',quarter(" + dateField + "),'季度')";
		}
		else {
			String str = "";
			if (pType == PeriodType.DAY) {
				str = "%Y%m%d";
			}
			else if (pType == PeriodType.WEEK) {
				str = "%Y-%u周";
			}
			else if (pType == PeriodType.MONTH) {
				str = "%Y%m";
			}
			else if (pType == PeriodType.YEAR) {
				str = "%Y";
			}
			else {
				str = "%Y%m%d";;
			}
			strTotal = "DATE_FORMAT(" + dateField + ", '" + str + "')";
		}		
		return strTotal;
	}
	
	// 得到某个城市某一年最低可能的正常单价(单位：万)
	public static double GetMinPrice(String chengshi, int year)
	{
		double minPrice = 0.3; 
		if (chengshi.equals("北京")) {
			if (year <= 2011) {
				minPrice = 0.5;
			}
			else if (year>2011 && year<2015 ) {
				minPrice = 0.6;
			}
			else if (year>=2015 ) {
				minPrice = 0.8;
			} 
		}
		else if (chengshi.equals("廊坊")) {
			if (year <= 2011) {
				minPrice = 0.2;
			}
			else if (year>2011 && year<2015 ) {
				minPrice = 0.3;
			}
			else if (year>=2015 ) {
				minPrice = 0.5;
			} 
		}
		else if (chengshi.equals("武汉")) {
			if (year <= 2011) {
				minPrice = 0.2;
			}
			else if (year>2011 && year<2015 ) {
				minPrice = 0.3;
			}
			else if (year>=2015 ) {
				minPrice = 0.4;
			} 
		}	
		return minPrice;
	}
	
}
