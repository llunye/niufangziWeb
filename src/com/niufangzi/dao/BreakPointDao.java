package com.niufangzi.dao;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


import com.niufangzi.model.BreakPoint;
import com.niufangzi.model.BreakPoint.TaskType;
import com.niufangzi.util.*;


public class BreakPointDao {

	private static Logger logger = LogManager.getLogger();
	
	public static boolean AddBreakPoint(BreakPoint bp)
	{
		ConnPoolUtil connPool = ConnPoolUtil.getInstance();
		Connection conn = null;
		PreparedStatement pstmt = null;
		try {
			String strSQL = "insert into t_breakpoint (cityIdx, taskType, level, quIdx, jiedaoIdx, pageNo, createTime, updateTime) values (?,?,?,?,?,?,now(),now())";
			conn = connPool.getConnection();
			pstmt = conn.prepareStatement(strSQL);
			pstmt.setInt(1, bp.getCityIdx());
			pstmt.setInt(2, bp.getTaskType());
			pstmt.setInt(3, bp.getLevel());
			pstmt.setInt(4, bp.getQuIdx());
			pstmt.setInt(5, bp.getJiedaoIdx());
			pstmt.setInt(6, bp.getPageNo());
			pstmt.executeUpdate();
        } catch (Exception e) {  
        	logger.error(e.getMessage());
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
	
	public static boolean UpdateBreakPoint(BreakPoint bp)
	{
		ConnPoolUtil connPool = ConnPoolUtil.getInstance();
		Connection conn = null;
		PreparedStatement pstmt = null;
		boolean res = true;
		try {
			String strSQL = "update t_breakpoint set quIdx=?, jiedaoIdx=?, pageNo=?, updateTime=now() where cityIdx=? and taskType=? and level=?";
			conn = connPool.getConnection();
			pstmt = conn.prepareStatement(strSQL);
			pstmt.setInt(1, bp.getQuIdx());
			pstmt.setInt(2, bp.getJiedaoIdx());
			pstmt.setInt(3, bp.getPageNo());
			pstmt.setInt(4, bp.getCityIdx());
			pstmt.setInt(5, bp.getTaskType());
			pstmt.setInt(6, bp.getLevel());
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
		
	
	public static BreakPoint GetBreakPoint(int cityIdx, int taskType, int level)
	{
		ConnPoolUtil connPool = ConnPoolUtil.getInstance();
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null; 	
		BreakPoint cp = null;
		try {
			String strSQL = "select * from t_breakpoint where cityIdx=? and taskType=? and level=? ";
			conn = connPool.getConnection();
			pstmt = conn.prepareStatement(strSQL);
			pstmt.setInt(1, cityIdx);
			pstmt.setInt(2, taskType);
			pstmt.setInt(3, level);
			rs = pstmt.executeQuery();  
            if (rs.next()) {
            	cp = new BreakPoint();
            	cp.setCityIdx(rs.getInt("cityIdx"));
            	cp.setTaskType(rs.getInt("taskType"));
            	cp.setLevel(rs.getInt("level"));
            	cp.setQuIdx(rs.getInt("quIdx"));
            	cp.setJiedaoIdx(rs.getInt("jiedaoIdx"));
            	cp.setPageNo(rs.getInt("pageNo"));
                cp.setCreateTime(new Date(rs.getTimestamp("createTime").getTime()));
                cp.setUpdateTime(new Date(rs.getTimestamp("updateTime").getTime()));
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
		return cp;
	}
	

	public static boolean DeleteBreakPoint(int cityIdx, int taskType, int level)
	{
		ConnPoolUtil connPool = ConnPoolUtil.getInstance();
		Connection conn = null;
		PreparedStatement pstmt = null;
		boolean res = true;
		try {
			String strSQL = "delete from t_breakpoint where cityIdx=? and taskType=? and level=?";
			conn = connPool.getConnection();
			pstmt = conn.prepareStatement(strSQL);
			pstmt.setInt(1, cityIdx);
			pstmt.setInt(2, taskType);
			pstmt.setInt(3, level);
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
	
	
	
	public static boolean RefreshBreakPoint(BreakPoint bp)
	{
		boolean res = true;
		if (GetBreakPoint(bp.getCityIdx(), bp.getTaskType(), bp.getLevel()) == null)  //不存在
		{
			res = AddBreakPoint(bp);
		}
		else
		{
			res = UpdateBreakPoint(bp);
		}
		return res;
	}
	
	private static void DoTest()
	{
		//BreakPoint bp = new BreakPoint(1, 1, 2, 3, 4);
		//RefreshBreakPoint(bp);
		
		BreakPoint bp = new BreakPoint(0, TaskType.SOLD.ordinal(), 1, 22, 33, 44);
		RefreshBreakPoint(bp);
		
		BreakPoint bp1 = GetBreakPoint(0, TaskType.SOLD.ordinal(), 1);
		if (bp1 != null)
		{
			System.out.println(bp1.getTaskType());
			System.out.println(bp1.getLevel());
			System.out.println(bp1.getQuIdx());
			System.out.println(bp1.getJiedaoIdx());
			System.out.println(bp1.getPageNo());
			System.out.println(bp1.getCreateTime());
			System.out.println(bp1.getUpdateTime());
		}	
	}
	
	public static void main(String[] args) {
		//
	}
	

	
	
}
