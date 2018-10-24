package com.niufangzi.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.niufangzi.model.HouseChange;
import com.niufangzi.model.PageInfo;
import com.niufangzi.util.*;


public class HouseChangeDao {

	private static Logger logger = LogManager.getLogger();
	
	public static int AddHouseChange(HouseChange hc)
	{
		ConnPoolUtil connPool = ConnPoolUtil.getInstance();
		Connection conn = null;
		PreparedStatement pstmt = null;
		try {
			String strSQL = "insert into t_houseChange (houseId, chengshi, quyu1, quyu2, xiaoqu, fangwuhuxing, jianzhumianji, "
							+ "oldPrice, newPrice, oldDate, newDate, createTime) values (?,?,?,?,?,?,?,?,?,?,?,now())";
			conn = connPool.getConnection();
			pstmt = conn.prepareStatement(strSQL);
			
			pstmt.setString(1, hc.getHouseId());
			pstmt.setString(2, hc.getChengshi());
			pstmt.setString(3, hc.getQuyu1());
			pstmt.setString(4, hc.getQuyu2());
			pstmt.setString(5, hc.getXiaoqu());
			pstmt.setString(6, hc.getFangwuhuxing());
			pstmt.setFloat(7, hc.getJianzhumianji());
			pstmt.setFloat(8, hc.getOldPrice());
			pstmt.setFloat(9, hc.getNewPrice());
			java.sql.Timestamp ts = new java.sql.Timestamp(hc.getOldDate().getTime());
			pstmt.setTimestamp(10, ts);
			ts = new java.sql.Timestamp(hc.getNewDate().getTime());
			pstmt.setTimestamp(11, ts);
			pstmt.executeUpdate();
        } catch (Exception e) {  
        	logger.error(e.getMessage());
        	return -1;
        } finally {  
            connPool.returnConnection(conn);
            try {
				pstmt.close();
			} catch (SQLException e) {
				//
			}
        }
		
		return 0;
	}
	
	//查询记录总条数
	public static int QueryTotalCount(String chengshi, String filterStr)
	{
		ConnPoolUtil connPool = ConnPoolUtil.getInstance();
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null; 	
		int nTotal = 0;
		
		try {
			String strSQL = " select count(*) as nTotal from t_houseChange where chengshi=? " + filterStr;
			conn = connPool.getConnection();
			pstmt = conn.prepareStatement(strSQL);
			pstmt.setString(1, chengshi);
			rs = pstmt.executeQuery();
            if (rs.next()) {
            	nTotal = rs.getInt("nTotal");
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
		return nTotal;
	}
	
	
	//查询记录列表(分页)
	public static boolean QueryRecords(String chengshi, String filterStr, PageInfo pi, List<HouseChange> list_change)
	{
		ConnPoolUtil connPool = ConnPoolUtil.getInstance();
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null; 	
		Boolean res = true;
		list_change.clear();
		
		int nOffset = (pi.getPageNo()-1) * pi.getPageSize(); 
		try {
			String strSQL = "select * from t_houseChange where chengshi=? " + filterStr
							+ " order by createTime desc limit ?,? ";
			conn = connPool.getConnection();
			pstmt = conn.prepareStatement(strSQL);
			pstmt.setString(1, chengshi);
			pstmt.setInt(2, nOffset);
			pstmt.setInt(3, pi.getPageSize());
			rs = pstmt.executeQuery();
            while (rs.next()) {
            	HouseChange hc = new HouseChange();
            	hc.setHouseId(rs.getString("houseId"));
            	hc.setChengshi(rs.getString("chengshi"));
            	hc.setQuyu1(rs.getString("quyu1"));
            	hc.setQuyu2(rs.getString("quyu2"));
            	hc.setXiaoqu(rs.getString("xiaoqu"));
            	hc.setFangwuhuxing(rs.getString("fangwuhuxing"));
            	hc.setJianzhumianji(rs.getFloat("jianzhumianji"));
            	hc.setOldPrice(rs.getFloat("oldPrice"));
            	hc.setNewPrice(rs.getFloat("newPrice"));
            	hc.setOldDate(new Date(rs.getTimestamp("oldDate").getTime()));
            	hc.setNewDate(new Date(rs.getTimestamp("newDate").getTime()));
            	hc.setCreateTime(new Date(rs.getTimestamp("createTime").getTime()));
            	
            	list_change.add(hc);
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
	
	
	//查询某一套房子的所有调价记录
	public static boolean QueryRecordsById(String houseId, List<HouseChange> list_change)
	{
		ConnPoolUtil connPool = ConnPoolUtil.getInstance();
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null; 	
		Boolean res = true;
		list_change.clear();
		
		try {
			String strSQL = "select * from t_houseChange where houseId=? order by createTime desc";
			conn = connPool.getConnection();
			pstmt = conn.prepareStatement(strSQL);
			pstmt.setString(1, houseId);
			rs = pstmt.executeQuery();
            while (rs.next()) {
            	HouseChange hc = new HouseChange();
            	hc.setHouseId(rs.getString("houseId"));
            	hc.setChengshi(rs.getString("chengshi"));
            	hc.setQuyu1(rs.getString("quyu1"));
            	hc.setQuyu2(rs.getString("quyu2"));
            	hc.setXiaoqu(rs.getString("xiaoqu"));
            	hc.setFangwuhuxing(rs.getString("fangwuhuxing"));
            	hc.setJianzhumianji(rs.getFloat("jianzhumianji"));
            	hc.setOldPrice(rs.getFloat("oldPrice"));
            	hc.setNewPrice(rs.getFloat("newPrice"));
            	hc.setOldDate(new Date(rs.getTimestamp("oldDate").getTime()));
            	hc.setNewDate(new Date(rs.getTimestamp("newDate").getTime()));
            	hc.setCreateTime(new Date(rs.getTimestamp("createTime").getTime()));
            	
            	list_change.add(hc);
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
	
	//查询某一套房子的调价总次数
	public static int QueryHouseChangeCount(String houseId)
	{
		ConnPoolUtil connPool = ConnPoolUtil.getInstance();
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null; 	
		int nTotal = 0;
		
		
		try {
			String strSQL = "select count(*) as nTotal from t_houseChange where houseId=? ";
			conn = connPool.getConnection();
			pstmt = conn.prepareStatement(strSQL);
			pstmt.setString(1, houseId);
			rs = pstmt.executeQuery();
            if (rs.next()) {
            	nTotal = rs.getInt("nTotal");
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
		return nTotal;
	}
	
	
	
	//查询房子的调价总次数
	public static boolean QueryHouseChangeTimes(int minTimes, Map<String, Integer> map_changeTimes)
	{
		ConnPoolUtil connPool = ConnPoolUtil.getInstance();
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		map_changeTimes.clear();
		boolean res = true;
		try {
			String strSQL = "select * from " 
						+ " (select houseId, count(*) as nTimes from t_houseChange group by houseId) as a " 
						+ " where nTimes>=? order by nTimes desc";
			conn = connPool.getConnection();
			pstmt = conn.prepareStatement(strSQL);
			pstmt.setInt(1, minTimes);
			rs = pstmt.executeQuery();
            while (rs.next()) {
            	map_changeTimes.put(rs.getString("houseId"), rs.getInt("nTimes"));
            }  
            rs.close();
        } catch (Exception e) {
        	res = false;
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
		return res;
	}
	
	
	public static void main(String[] args) {
		//
		
	}
	

	
	
}
