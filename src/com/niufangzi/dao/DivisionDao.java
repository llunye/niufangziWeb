package com.niufangzi.dao;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


import com.niufangzi.model.BreakPoint;
import com.niufangzi.model.BreakPoint.TaskType;
import com.niufangzi.util.*;


public class DivisionDao {

	private static Logger logger = LogManager.getLogger();
	
	//查询所有的市列表
	public static boolean GetCity(List<String>list_city)
	{
		ConnPoolUtil connPool = ConnPoolUtil.getInstance();
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null; 	
		list_city.clear();
		boolean res = true;
		try {
			String strSQL = "select distinct chengshi from t_division";
			conn = connPool.getConnection();
			pstmt = conn.prepareStatement(strSQL);
			rs = pstmt.executeQuery();  
            while (rs.next()) {
            	list_city.add(rs.getString("chengshi"));
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
	
	//查询一个市下所有区的列表
	public static boolean GetQuInCity(String chengshi, List<String>list_qu)
	{
		ConnPoolUtil connPool = ConnPoolUtil.getInstance();
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null; 	
		list_qu.clear();
		boolean res = true;
		try {
			String strSQL = "select distinct qu, orderNum from t_division where chengshi=? order by orderNum asc ";
			conn = connPool.getConnection();
			pstmt = conn.prepareStatement(strSQL);
			pstmt.setString(1, chengshi);
			rs = pstmt.executeQuery();  
            while (rs.next()) {
            	list_qu.add(rs.getString("qu"));
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
