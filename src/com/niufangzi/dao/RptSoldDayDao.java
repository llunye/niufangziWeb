package com.niufangzi.dao;

import java.sql.Connection;
import java.util.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.niufangzi.dao.CommonDao.PeriodType;
import com.niufangzi.model.OperationState;
import com.niufangzi.util.ConnPoolUtil;

//统计每个市的日成交均价
public class RptSoldDayDao {

	private static Logger logger = LogManager.getLogger();
	
	private static boolean AddSoldDay(Date soldDate, String chengshi, int sCount, int sPrice)
	{
		ConnPoolUtil connPool = ConnPoolUtil.getInstance();
		Connection conn = null;
		PreparedStatement pstmt = null;
		boolean res = true;
		try {
			String strSQL = "insert into r_SoldDay(soldDate, chengshi, sCount, sPrice, createTime) values(?,?,?,?, now())";
			conn = connPool.getConnection();
			pstmt = conn.prepareStatement(strSQL);

			java.sql.Timestamp ts = new java.sql.Timestamp(soldDate.getTime());
			pstmt.setTimestamp(1, ts);
			pstmt.setString(2, chengshi);
			pstmt.setInt(3, sCount);
			pstmt.setInt(4, sPrice);
			pstmt.executeUpdate();
        } catch (Exception e) {  
        	logger.error(e.getMessage());
        	res = false;
        } finally {  
            connPool.returnConnection(conn);
        	try {
				pstmt.close();
			} catch (SQLException e) {
				//
			}
        }
		
		return res;
	}
	
	
	//删除某个城市某个月的统计信息
	private static boolean DeleteSoldDays(String chengshi, Date startDate, Date endDate)
	{
		ConnPoolUtil connPool = ConnPoolUtil.getInstance();
		Connection conn = null;
		PreparedStatement pstmt = null;
		boolean res = true;
		try {
			String strSQL = "delete from r_SoldDay where chengshi=? and soldDate>=? and soldDate<=?";
			conn = connPool.getConnection();
			pstmt = conn.prepareStatement(strSQL);
			pstmt.setString(1, chengshi);
			java.sql.Timestamp tsStart = new java.sql.Timestamp(startDate.getTime());
			java.sql.Timestamp tsEnd = new java.sql.Timestamp(endDate.getTime());
			pstmt.setTimestamp(2, tsStart);
			pstmt.setTimestamp(3, tsEnd);
			pstmt.executeUpdate();
        } catch (Exception e) {  
        	logger.error(e.getMessage());
        	res = false;
        } finally {  
            connPool.returnConnection(conn);
        	try {
				pstmt.close();
			} catch (SQLException e) {
				//
			}
        }
		
		return res;
	}
	
	//从‘成交’表中统计一天的信息(全表扫描)
	private static Boolean QueryReportInfo(String chengshi, Date startDate, Date endDate, List<Date> list_date, 
											List<Integer> list_count, List<Integer> list_price)
	{
		ConnPoolUtil connPool = ConnPoolUtil.getInstance();
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null; 	
		Boolean res = true;
		list_date.clear();
		list_count.clear();
		list_price.clear();
		try {
			String strSQL = "select chengjiaoriqi, count(*) as nCount, avg(chengjiaojiage/jianzhumianji) as nPrice from t_housesold "
							+ " where fangwuyongtu!=? and suozailouceng!=? and (chengjiaojiage/jianzhumianji)>? "
							+ " and chengshi=? and chengjiaoriqi>=? and chengjiaoriqi<=?  group by chengjiaoriqi";	
			
			conn = connPool.getConnection();
			pstmt = conn.prepareStatement(strSQL);
			pstmt.setString(1, "别墅");
			pstmt.setString(2, "地下室");
			double minPrice = CommonDao.GetMinPrice(chengshi, startDate.getYear());  //此处取开始年份的阈值
			pstmt.setDouble(3, minPrice);
			pstmt.setString(4, chengshi);
			java.sql.Timestamp tsStart = new java.sql.Timestamp(startDate.getTime());
			java.sql.Timestamp tsEnd = new java.sql.Timestamp(endDate.getTime());
			pstmt.setTimestamp(5, tsStart);
			pstmt.setTimestamp(6, tsEnd);
			rs = pstmt.executeQuery();
			
            while (rs.next()) {
            	list_date.add(new Date(rs.getTimestamp("chengjiaoriqi").getTime()));
            	list_count.add(rs.getInt("nCount"));
            	Float fTmp = rs.getFloat("nPrice") * 10000;
            	list_price.add(fTmp.intValue());
            }  
            rs.close();
        } catch (Exception e) {  
        	logger.error(e.getMessage());
        	res = false;
        } finally {  
            connPool.returnConnection(conn);
            try {
        		rs.close();
				pstmt.close();
			} catch (SQLException e) {
				//
			}
        }
		return res;
	}

	
	public static Boolean SumDays(String chengshi, Date startDate, Date endDate)
	{
		List<Date> list_date = new ArrayList<Date>(); 
		List<Integer> list_count = new ArrayList<Integer>();
		List<Integer> list_price = new ArrayList<Integer>();
		
		if (startDate.after(endDate))
		{
			return false;
		}
		
		boolean res = QueryReportInfo(chengshi, startDate, endDate, list_date, list_count, list_price);
		if (! res)
		{
			return false;
		}
		
		DeleteSoldDays(chengshi, startDate, endDate);
		for(int i=0; i<list_date.size(); i++)
		{
			AddSoldDay(list_date.get(i), chengshi, list_count.get(i), list_price.get(i));
		}
		return res;
	}
	
	
	//查询列表-成交均价
	public static boolean QueryPriceRecords(String chengshi, int maxCount, PeriodType pType, List<Integer> list_price, List<String> list_date)
	{
		ConnPoolUtil connPool = ConnPoolUtil.getInstance();
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null; 	
		Boolean res = true;
		list_date.clear();
		list_price.clear();
		String strTmp = CommonDao.GetSumDateFormat("soldDate", pType);
		try {
			String strSQL = " select " + strTmp + " as strDate, avg(sPrice) as sumPrice from r_soldDay where chengshi=? "
							+ " group by strDate order by strDate desc limit ? ";
			conn = connPool.getConnection();
			pstmt = conn.prepareStatement(strSQL);
			pstmt.setString(1, chengshi);
			pstmt.setInt(2, maxCount);
			rs = pstmt.executeQuery();
            while (rs.next()) {
            	list_date.add(rs.getString("strDate"));
            	list_price.add(rs.getInt("sumPrice"));
            }  
            rs.close();
        } catch (Exception e) {  
        	logger.error(e.getMessage());
        	res = false;
        } finally {  
            connPool.returnConnection(conn);
            try {
        		rs.close();
				pstmt.close();
			} catch (SQLException e) {
				//
			}
        }
		return res;
	}
	
	//查询列表-成交套数
	public static boolean QueryCountRecords(String chengshi, int maxCount, PeriodType pType, List<Integer> list_count, List<String> list_date)
	{
		ConnPoolUtil connPool = ConnPoolUtil.getInstance();
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null; 	
		Boolean res = true;
		list_date.clear();
		list_count.clear();
		String strTmp = CommonDao.GetSumDateFormat("soldDate", pType);
		try {
			String strSQL = " select " + strTmp + " as strDate, sum(sCount) as sumCount from r_SoldDay where chengshi=? "
							+ " group by strDate order by strDate desc limit ? ";
			conn = connPool.getConnection();
			pstmt = conn.prepareStatement(strSQL);
			pstmt.setString(1, chengshi);
			pstmt.setInt(2, maxCount);
			rs = pstmt.executeQuery();
            while (rs.next()) {
            	list_date.add(rs.getString("strDate"));
            	list_count.add(rs.getInt("sumCount"));
            }  
            rs.close();
        } catch (Exception e) {  
        	logger.error(e.getMessage());
        	res = false;
        } finally {  
            connPool.returnConnection(conn);
            try {
        		rs.close();
				pstmt.close();
			} catch (SQLException e) {
				//
			}
        }
		return res;
	}
	
	//查找表r_soldDay中已有数据的SoldDate最大值
	public static Date QueryMaxDate(String city, OperationState state)
	{
		Date maxDate = new Date();
		state.result = false;
		ConnPoolUtil connPool = ConnPoolUtil.getInstance();
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null; 	
		try {
			String strSQL = "select max(soldDate) as maxDate from r_soldDay where chengshi=?";
			conn = connPool.getConnection();
			pstmt = conn.prepareStatement(strSQL);
			pstmt.setString(1, city);
			rs = pstmt.executeQuery();
            if (rs.next()) {
            	if (rs.getTimestamp("maxDate") != null)
            	{
            		maxDate = new Date(rs.getTimestamp("maxDate").getTime());
            		state.result = true;
            	}
            }
            rs.close();
        } catch (Exception e) {  
        	logger.error(e.getMessage());
        } finally {  
            connPool.returnConnection(conn);
            try {
        		rs.close();
				pstmt.close();
			} catch (SQLException e) {
				//
			}
        }
		return maxDate;
	}

	private static void DoTest()
	{
		List<Integer> list_price = new ArrayList<Integer>();
		List<String> list_date =  new ArrayList<String>();
		String city = "北京";
		int maxCount = 50;
		PeriodType pType = PeriodType.DAY;
		boolean res = RptSoldDayDao.QueryPriceRecords(city, maxCount, pType, list_price, list_date);
		System.out.println(list_price.size());
		for (int i=0; i<list_price.size(); i++)
		{
			System.out.println(list_price.get(i) + " : " + list_date.get(i));
		}
		
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		//DoTest();
	}

}
