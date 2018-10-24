package com.niufangzi.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.niufangzi.model.PhotoSelling;
import com.niufangzi.util.*;


public class PhotoSellingDao {

	private static Logger logger = LogManager.getLogger();
	
	public static int AddAPhoto(PhotoSelling photo)
	{
		ConnPoolUtil connPool = ConnPoolUtil.getInstance();
		Connection conn = null;
		PreparedStatement pstmt = null;
		try {
			String strSQL = "insert into t_photoSelling (chengshi, houseId, photoName, url, status, createTime, updateTime) values (?,?,?,?,?,?,?)";
			conn = connPool.getConnection();
			pstmt = conn.prepareStatement(strSQL);
			
			pstmt.setString(1, photo.getChengshi());
			pstmt.setString(2, photo.getHouseId());
			pstmt.setString(3, photo.getPhotoName());
			pstmt.setString(4, photo.getUrl());
			pstmt.setInt(5, photo.getStatus());
			java.util.Date d_now = new java.util.Date();
			java.sql.Timestamp ts = new java.sql.Timestamp(d_now.getTime());
			pstmt.setTimestamp(6, ts);
			pstmt.setTimestamp(7, ts);
			
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
	
	public static int DeletePhotos(String houseId)
	{
		ConnPoolUtil connPool = ConnPoolUtil.getInstance();
		Connection conn = null;
		PreparedStatement pstmt = null;
		try {
			String strSQL = "delete from t_photoSelling where houseId=?";
			conn = connPool.getConnection();
			pstmt = conn.prepareStatement(strSQL);
			pstmt.setString(1, houseId);
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
	
	public static int AddPhotos(List<PhotoSelling> list_photo)
	{
		int i=0;
		for(i=0; i<list_photo.size(); i++)
		{
			AddAPhoto(list_photo.get(i));
		}
		return 0;
	}
	
	
	public static String GetPhotoURL(String city, String houseId, String photoName)
	{
		ConnPoolUtil connPool = ConnPoolUtil.getInstance();
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null; 	
		String strURL = "";
		try {
			String strSQL = "select url from t_photoSelling where chengshi=? and houseId=? and PhotoName=?";
			conn = connPool.getConnection();
			pstmt = conn.prepareStatement(strSQL);
			pstmt.setString(1, city);
			pstmt.setString(2, houseId);
			pstmt.setString(3, photoName);
			rs = pstmt.executeQuery();
            if (rs.next()) {
            	strURL = rs.getString("url");
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
		return strURL;
	}
	
	
	
	public static void main(String[] args) {
		//
		
	}
	

	
	
}
