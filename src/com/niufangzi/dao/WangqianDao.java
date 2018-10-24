package com.niufangzi.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.niufangzi.dao.CommonDao.PeriodType;
import com.niufangzi.model.Wangqian;
import com.niufangzi.util.ConnPoolUtil;

public class WangqianDao {

	private static Logger logger = LogManager.getLogger();
	
	public static boolean AddWangqian(Wangqian wq)
	{
		ConnPoolUtil connPool = ConnPoolUtil.getInstance();
		Connection conn = null;
		PreparedStatement pstmt = null;
		try {
			String strSQL = "insert into t_wangqian ( wangqianDate, qiCount, qiArea, qiZhuCount, qiZhuArea, xianCount, xianArea,"
						  + " xianZhuCount, xianZhuArea, cunCount, cunArea, cunZhuCount, cunZhuArea, createTime, updateTime ) "
						  + " values (?,?,?,?,?,?,?,?,?,?,?,?,?, now(), now())";
			conn = connPool.getConnection();
			pstmt = conn.prepareStatement(strSQL);
			java.sql.Timestamp ts = new java.sql.Timestamp(wq.getWangqianDate().getTime());
			pstmt.setTimestamp(1, ts);
			
			pstmt.setInt(2, wq.getQiCount());
			pstmt.setInt(3, wq.getQiArea());
			pstmt.setInt(4, wq.getQiZhuCount());
			pstmt.setInt(5, wq.getQiZhuArea());
			
			pstmt.setInt(6, wq.getXianCount());
			pstmt.setInt(7, wq.getXianArea());
			pstmt.setInt(8, wq.getXianZhuCount());
			pstmt.setInt(9, wq.getXianZhuArea());
			
			pstmt.setInt(10, wq.getCunCount());
			pstmt.setInt(11, wq.getCunArea());
			pstmt.setInt(12, wq.getCunZhuCount());
			pstmt.setInt(13, wq.getCunZhuArea());
			
			/*
			java.util.Date d_now = new java.util.Date();
			ts = new java.sql.Timestamp(d_now.getTime());
			pstmt.setTimestamp(14, ts);
			pstmt.setTimestamp(15, ts);
			*/
			
			pstmt.executeUpdate();
        } catch (Exception e) {  
        	logger.error(e.getMessage());
        	System.out.println(e.toString());
        	return false;
        } finally {  
            connPool.returnConnection(conn);
            try {
				pstmt.close();
			} catch (SQLException e) {
				//
			}
        }
		
		return true;
	}
	
	
	//根据网签日期查询网签信息
	public static Wangqian GetWangqian(Date wangqianDate)
	{
		ConnPoolUtil connPool = ConnPoolUtil.getInstance();
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null; 
		Wangqian wq = null;		
		try {
			String strSQL = "select * from t_wangqian where wangqianDate=?";
			conn = connPool.getConnection();
			pstmt = conn.prepareStatement(strSQL);
			java.sql.Timestamp ts = new java.sql.Timestamp(wangqianDate.getTime());
			pstmt.setTimestamp(1, ts);
			rs = pstmt.executeQuery();  
            if (rs.next()) {
            	wq = new Wangqian();
            	wq.setWangqianDate(new Date(rs.getTimestamp("wangqianDate").getTime()));
            	wq.setQiCount(rs.getInt("qiCount"));
            	wq.setQiArea(rs.getInt("qiArea"));
            	wq.setQiZhuCount(rs.getInt("qiZhuCount"));
            	wq.setQiZhuArea(rs.getInt("qiZhuArea"));
            	wq.setXianCount(rs.getInt("xianCount"));
            	wq.setXianArea(rs.getInt("xianArea"));
            	wq.setXianZhuCount(rs.getInt("xianZhuCount"));
            	wq.setXianZhuArea(rs.getInt("xianZhuArea"));
			    wq.setCunCount(rs.getInt("cunCount"));
            	wq.setCunArea(rs.getInt("cunArea"));
            	wq.setCunZhuCount(rs.getInt("cunZhuCount"));
            	wq.setCunZhuArea(rs.getInt("cunZhuArea"));
                wq.setCreateTime(new Date(rs.getTimestamp("createTime").getTime()));
                wq.setUpdateTime(new Date(rs.getTimestamp("updateTime").getTime()));
            }  
        } catch (Exception e) {  
        	logger.error(e.getMessage());
        	wq = null;
        } finally {  
            connPool.returnConnection(conn);
            try {
        		rs.close();
				pstmt.close();
			} catch (SQLException e) {
				//
			}
        }
		
		return wq;
	}
	
	
	public static boolean DeleteWangqian(Date wangqianDate)
	{		
		ConnPoolUtil connPool = ConnPoolUtil.getInstance();
		Connection conn = null;
		PreparedStatement pstmt = null;
		try {
			String strSQL = "delete from t_wangqian where wangqiandate=?";
			conn = connPool.getConnection();
			pstmt = conn.prepareStatement(strSQL);
			java.sql.Timestamp ts = new java.sql.Timestamp(wangqianDate.getTime());
			pstmt.setTimestamp(1, ts);
			pstmt.executeUpdate();  
        } catch (Exception e) {  
        	logger.error(e.getMessage());
        } finally {  
            connPool.returnConnection(conn);
            try {
				pstmt.close();
			} catch (SQLException e) {
				//
			}
        }
		return true;
	}
	
	
	//查询列表-目前只有北京市的网签信息
	public static boolean QueryRecords(String chengshi, int maxCount, PeriodType pType, List<Integer> list_count, List<String> list_date)
	{
		ConnPoolUtil connPool = ConnPoolUtil.getInstance();
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null; 	
		Boolean res = true;
		list_count.clear();
		list_date.clear();
		String strTmp = CommonDao.GetSumDateFormat("wangqianDate", pType);
		try {
			String strSQL = "select " + strTmp + " as strDate, sum(cunZhuCount) as sumCount from t_wangqian "
							+ " group by strDate order by strDate desc limit ? ";
			conn = connPool.getConnection();
			pstmt = conn.prepareStatement(strSQL);
			pstmt.setInt(1, maxCount);
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
	

	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		//
	}

}
