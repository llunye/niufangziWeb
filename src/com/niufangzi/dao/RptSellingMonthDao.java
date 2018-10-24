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

import com.niufangzi.model.OperationState;
import com.niufangzi.util.ConnPoolUtil;
import com.niufangzi.util.UtilHelper;


//统计每个区的月挂牌均价
public class RptSellingMonthDao {

	private static Logger logger = LogManager.getLogger();
	
	private static boolean AddsellingMonth(Date sellingDate, String chengshi, String qu, int sCount, int sPrice)
	{
		ConnPoolUtil connPool = ConnPoolUtil.getInstance();
		Connection conn = null;
		PreparedStatement pstmt = null;
		boolean res = true;
		try {
			String strSQL = "insert into r_sellingMonth(sellingDate, chengshi, qu, sCount, sPrice, createTime) values(?,?,?,?,?, now())";
			conn = connPool.getConnection();
			pstmt = conn.prepareStatement(strSQL);
			
			java.sql.Timestamp ts = new java.sql.Timestamp(sellingDate.getTime());
			pstmt.setTimestamp(1, ts);
			pstmt.setString(2, chengshi);
			pstmt.setString(3, qu);
			pstmt.setInt(4, sCount);
			pstmt.setInt(5, sPrice);
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
	private static boolean DeletesellingMonth(String chengshi, int year, int month)
	{
		ConnPoolUtil connPool = ConnPoolUtil.getInstance();
		Connection conn = null;
		PreparedStatement pstmt = null;
		boolean res = true;
		try {
			String strSQL = "delete from r_sellingMonth where chengshi=? and year(sellingDate)=? and month(sellingDate)=? ";
			conn = connPool.getConnection();
			pstmt = conn.prepareStatement(strSQL);
			pstmt.setString(1, chengshi);
			pstmt.setInt(2, year);
			pstmt.setInt(3, month);
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
	
	//从‘在售’表中查询得到统计信息
	private static Boolean QueryReportInfo(String chengshi, int year, int month, List<String> list_qu, List<Integer> list_count, List<Integer> list_price)
	{
		ConnPoolUtil connPool = ConnPoolUtil.getInstance();
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null; 	
		Boolean res = true;
		list_qu.clear();
		list_count.clear();
		list_price.clear();
		try {
			String strSQL = "select quyu1, junCount, junTotal/junCount as junPrice from "
							+ " (select quyu1, count(*) as junCount, sum(guapaijiage/jianzhumianji) as junTotal from t_houseselling "
							+ " where fangwuyongtu!=? and suozailouceng!=? and (guapaijiage/jianzhumianji)>? "
							+ " and chengshi=? and year(guapaishijian)=? and month(guapaishijian)=? "
							+ " group by quyu1) as tableTmp";	
			
			conn = connPool.getConnection();
			pstmt = conn.prepareStatement(strSQL);
			pstmt.setString(1, "别墅");
			pstmt.setString(2, "地下室");
			double minPrice = CommonDao.GetMinPrice(chengshi, year);
			pstmt.setDouble(3, minPrice);
			pstmt.setString(4, chengshi);
			pstmt.setInt(5, year);
			pstmt.setInt(6, month);
			rs = pstmt.executeQuery();
			
            while (rs.next()) {
            	list_qu.add(rs.getString("quyu1"));
            	list_count.add(rs.getInt("junCount"));
            	Float fTmp = rs.getFloat("junPrice") * 10000;
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

	
	public static Boolean SumAMonth(String chengshi, int year, int month)
	{
		List<String> list_qu = new ArrayList<String>(); 
		List<Integer> list_count = new ArrayList<Integer>();
		List<Integer> list_price = new ArrayList<Integer>();
		
		OperationState state = new OperationState();
		String strDate = String.valueOf(year) + "-" + String.valueOf(month) + "-1";
		Date sellingDate = UtilHelper.tryStrToDate(strDate, "yyyy-MM-dd", state);
		if (! state.result)
		{
			return false;
		}
		
		boolean res = QueryReportInfo(chengshi, year, month, list_qu, list_count, list_price);
		if (! res)
		{
			return false;
		}
		
		DeletesellingMonth(chengshi, year, month);
		for(int i=0; i<list_qu.size(); i++)
		{
			AddsellingMonth(sellingDate, chengshi, list_qu.get(i), list_count.get(i), list_price.get(i));
		}
		return res;
	}
	
	
	//查找表r_sellingMonth中已有数据的SellingDate最大值
	public static Date QueryMaxDate(String city, OperationState state)
	{
		Date maxDate = new Date();
		state.result = false;
		ConnPoolUtil connPool = ConnPoolUtil.getInstance();
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		try {
			String strSQL = "select max(sellingDate) as maxDate from r_sellingMonth where chengshi=?";
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
		String chengshi = "北京";
		int year = 2013;
		int month = 1;
		List<String> list_qu = new ArrayList<String>(); 
		List<Integer> list_count = new ArrayList<Integer>();
		List<Integer> list_price = new ArrayList<Integer>();
		
		QueryReportInfo(chengshi, year, month, list_qu, list_count, list_price);
		for(int i=0; i<list_qu.size(); i++)
		{
			System.out.println(list_qu.get(i) + " - " + list_count.get(i) + " - " + list_price.get(i) );
		}
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		//DoTest();
		
		//SumMonth("北京", 2013, 1);
	}

}
