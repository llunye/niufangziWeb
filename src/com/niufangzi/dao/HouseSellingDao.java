package com.niufangzi.dao;

import java.sql.Connection;
//import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.niufangzi.model.HouseSelling;
import com.niufangzi.model.OperationState;
import com.niufangzi.model.PageInfo;
import com.niufangzi.util.ConnPoolUtil;
import com.niufangzi.util.UtilHelper;

public class HouseSellingDao {

	private static Logger logger = LogManager.getLogger();
	
	public static boolean AddHouse(HouseSelling house)
	{
		ConnPoolUtil connPool = ConnPoolUtil.getInstance();
		Connection conn = null;
		PreparedStatement pstmt = null;
		
		try {
			String strSQL = "insert into t_houseSelling (chengshi, quyu1, quyu2, quyu3, quyu4, xiaoqu, houseId, fangwuhuxing, jianzhumianji, taoneimianji, "
						  + "fangwuchaoxiang, zhuangxiuqingkuang, gongnuanfangshi, chanquannianxian, suozailouceng, zonglouceng, huxingjiegou, "
						  + "jianzhuleixing, jianchengniandai, jianzhujiegou, tihubili, peibeidianti, guapaishijian, shangcijiaoyi, fangwunianxian, "
						  + "diyaxinxi, jiaoyiquanshu, fangwuyongtu, chanquansuoshu, fangbenbeijian, zhuangtai, guapaijiage, chengjiaojiage, "
						  + "chengjiaoriqi, chengjiaotujing, liulan, guanzhu, daikan, tiaojia, zhouqi, createTime, updateTime) "
						  + " values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
			
			conn = connPool.getConnection();
			pstmt = conn.prepareStatement(strSQL);
			pstmt.setString(1, house.getChengshi());
			pstmt.setString(2, house.getQuyu1());
			pstmt.setString(3, house.getQuyu2());
			pstmt.setString(4, house.getQuyu3());
			pstmt.setString(5, house.getQuyu4());
			pstmt.setString(6, house.getXiaoqu());
			pstmt.setString(7, house.getHouseId());
			pstmt.setString(8, house.getFangwuhuxing());
			pstmt.setFloat(9, house.getJianzhumianji());
			pstmt.setFloat(10, house.getTaoneimianji());
			
			pstmt.setString(11, house.getFangwuchaoxiang());
			pstmt.setString(12, house.getZhuangxiuqingkuang());
			pstmt.setString(13, house.getGongnuanfangshi());
			pstmt.setInt(14, house.getChanquannianxian());
			pstmt.setString(15, house.getSuozailouceng());
			pstmt.setInt(16, house.getZonglouceng());
			pstmt.setString(17, house.getHuxingjiegou());
			
			pstmt.setString(18, house.getJianzhuleixing());
			pstmt.setInt(19, house.getJianchengniandai());
			pstmt.setString(20, house.getJianzhujiegou());
			pstmt.setString(21, house.getTihubili());
			pstmt.setString(22, house.getPeibeidianti());
			java.sql.Timestamp ts = new java.sql.Timestamp(house.getGuapaishijian().getTime());
			pstmt.setTimestamp(23, ts);
			ts = new java.sql.Timestamp(house.getShangcijiaoyi().getTime());
			pstmt.setTimestamp(24, ts);
			pstmt.setString(25, house.getFangwunianxian());
			
			pstmt.setString(26, house.getDiyaxinxi());
			pstmt.setString(27, house.getJiaoyiquanshu());
			pstmt.setString(28, house.getFangwuyongtu());
			pstmt.setString(29, house.getChanquansuoshu());
			pstmt.setString(30, house.getFangbenbeijian());
			pstmt.setString(31, house.getZhuangtai());
			pstmt.setFloat(32, house.getGuapaijiage());
			pstmt.setFloat(33, house.getChengjiaojiage());

			ts = new java.sql.Timestamp(house.getChengjiaoriqi().getTime());
			pstmt.setTimestamp(34, ts);
			pstmt.setString(35, house.getChengjiaotujing());
			pstmt.setInt(36, house.getLiulan());
			pstmt.setInt(37, house.getGuanzhu());
			pstmt.setInt(38, house.getDaikan());
			pstmt.setInt(39, house.getTiaojia());
			pstmt.setInt(40, house.getZhouqi());
			java.util.Date d_now = new java.util.Date();
			ts = new java.sql.Timestamp(d_now.getTime());
			pstmt.setTimestamp(41, ts);
			pstmt.setTimestamp(42, ts);
			
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
	
	//目前只更新 挂牌价格 和 挂牌时间
	public static boolean UpdateHouse(HouseSelling house)
	{
		ConnPoolUtil connPool = ConnPoolUtil.getInstance();
		Connection conn = null;
		PreparedStatement pstmt = null;
		
		try {
			String strSQL = "update t_houseSelling set guapaijiage=?, guapaishijian=?, updateTime=now() where chengshi=? and houseId=? ";
			conn = connPool.getConnection();
			pstmt = conn.prepareStatement(strSQL);
			pstmt.setFloat(1, house.getGuapaijiage());
			java.sql.Timestamp ts = new java.sql.Timestamp(house.getGuapaishijian().getTime());
			pstmt.setTimestamp(2, ts);
			pstmt.setString(3, house.getChengshi());
			pstmt.setString(4, house.getHouseId());
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
	
	//删除房屋
	public static boolean DeleteHouse(String houseId)
	{
		ConnPoolUtil connPool = ConnPoolUtil.getInstance();
		Connection conn = null;
		PreparedStatement pstmt = null;
		
		try {
			String strSQL = "delete from t_houseSelling where houseId=? ";
			conn = connPool.getConnection();
			pstmt = conn.prepareStatement(strSQL);
			pstmt.setString(1, houseId);
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
	
	
	public static HouseSelling GetHouse(String houseId)
	{
		ConnPoolUtil connPool = ConnPoolUtil.getInstance();
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null; 
		HouseSelling house = null;		
		try {
			String strSQL = "select * from t_houseSelling where houseId=?";
			conn = connPool.getConnection();
			pstmt = conn.prepareStatement(strSQL);
			pstmt.setString(1, houseId);
			rs = pstmt.executeQuery();  
            if (rs.next()) {
            	house = new HouseSelling();
            	
            	house.setChengshi(rs.getString("chengshi"));
            	house.setQuyu1(rs.getString("quyu1"));
            	house.setQuyu2(rs.getString("quyu2"));
            	house.setQuyu3(rs.getString("quyu3"));
            	house.setQuyu4(rs.getString("quyu4"));
            	house.setXiaoqu(rs.getString("xiaoqu"));
            	            	
            	house.setHouseId(rs.getString("houseId"));
            	house.setFangwuhuxing(rs.getString("fangwuhuxing"));
            	house.setJianzhumianji(rs.getFloat("jianzhumianji"));
            	house.setTaoneimianji(rs.getFloat("taoneimianji"));
            	house.setFangwuchaoxiang(rs.getString("fangwuchaoxiang"));
            	house.setZhuangxiuqingkuang(rs.getString("zhuangxiuqingkuang"));
            	house.setGongnuanfangshi(rs.getString("gongnuanfangshi"));
            	house.setChanquannianxian(rs.getInt("chanquannianxian"));
            	house.setSuozailouceng(rs.getString("suozailouceng"));
            	
            	house.setZonglouceng(rs.getInt("zonglouceng"));
            	house.setHuxingjiegou(rs.getString("huxingjiegou"));
            	house.setJianzhuleixing(rs.getString("jianzhuleixing"));
            	house.setJianchengniandai(rs.getInt("jianchengniandai"));
            	house.setJianzhujiegou(rs.getString("jianzhujiegou"));
            	house.setTihubili(rs.getString("tihubili"));
            	house.setPeibeidianti(rs.getString("peibeidianti"));
            	
            	house.setGuapaishijian(new Date(rs.getTimestamp("guapaishijian").getTime()));
            	house.setShangcijiaoyi(new Date(rs.getTimestamp("shangcijiaoyi").getTime()));
            	house.setFangwunianxian(rs.getString("fangwunianxian"));
            	house.setDiyaxinxi(rs.getString("diyaxinxi"));
            	house.setJiaoyiquanshu(rs.getString("jiaoyiquanshu"));
            	house.setFangwuyongtu(rs.getString("fangwuyongtu"));
            	house.setChanquansuoshu(rs.getString("chanquansuoshu"));
            	house.setFangbenbeijian(rs.getString("fangbenbeijian"));

            	house.setZhuangtai(rs.getString("zhuangtai"));
            	house.setGuapaijiage(rs.getFloat("guapaijiage"));
            	house.setChengjiaojiage(rs.getFloat("chengjiaojiage"));
            	house.setChengjiaoriqi(new Date(rs.getTimestamp("chengjiaoriqi").getTime()));
            	house.setChengjiaotujing(rs.getString("chengjiaotujing"));
            	house.setLiulan(rs.getInt("liulan"));
            	house.setGuanzhu(rs.getInt("guanzhu"));
            	house.setDaikan(rs.getInt("daikan"));
            	house.setTiaojia(rs.getInt("tiaojia"));
            	house.setZhouqi(rs.getInt("zhouqi"));
            	house.setCreateTime(new Date(rs.getTimestamp("createTime").getTime()));
                house.setUpdateTime(new Date(rs.getTimestamp("updateTime").getTime()));
            }  
        } catch (Exception e) {  
        	logger.error(e.getMessage());
        	house = null;
        } finally {  
            connPool.returnConnection(conn);
            try {
            	rs.close();
				pstmt.close();
			} catch (SQLException e) {
				//
			}
        }
		
		return house;
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
			String strSQL = " select count(*) as nTotal from t_houseSelling where chengshi=? " + filterStr;
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
	public static boolean QueryRecords(String chengshi, String filterStr, int nOrderField, int nOrderType, 
										PageInfo pi, List<HouseSelling> list_selling)
	{
		ConnPoolUtil connPool = ConnPoolUtil.getInstance();
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null; 	
		Boolean res = true;
		list_selling.clear();
		
		// 拼排序字符串
		String strOrder = "";
		if (nOrderField == 0)
			strOrder = " createTime ";
		else if (nOrderField == 1)
			strOrder = " createTime ";
		else if (nOrderField == 2)
			strOrder = " guapaijiage ";
		else if (nOrderField == 3)
			strOrder = " jianzhumianji ";
		else if (nOrderField == 4)
			strOrder = " daikan ";
		if (nOrderField >= 0) {  //小于0为不指定排序
			strOrder = " order by " + strOrder;
			if (nOrderType == 0)
				strOrder = strOrder + " asc ";
			else
				strOrder = strOrder + " desc ";
		}
		
		int nOffset = (pi.getPageNo()-1) * pi.getPageSize(); 
		try {
			String strSQL = "select * from t_houseSelling where chengshi=? " + filterStr + strOrder + " limit ?,? ";
			conn = connPool.getConnection();
			pstmt = conn.prepareStatement(strSQL);
			pstmt.setString(1, chengshi);
			pstmt.setInt(2, nOffset);
			pstmt.setInt(3, pi.getPageSize());
			rs = pstmt.executeQuery();
            while (rs.next()) {
            	HouseSelling house = new HouseSelling();
            	
            	house.setChengshi(rs.getString("chengshi"));
            	house.setQuyu1(rs.getString("quyu1"));
            	house.setQuyu2(rs.getString("quyu2"));
            	house.setQuyu3(rs.getString("quyu3"));
            	house.setQuyu4(rs.getString("quyu4"));
            	house.setXiaoqu(rs.getString("xiaoqu"));
            	            	
            	house.setHouseId(rs.getString("houseId"));
            	house.setFangwuhuxing(rs.getString("fangwuhuxing"));
            	house.setJianzhumianji(rs.getFloat("jianzhumianji"));
            	house.setTaoneimianji(rs.getFloat("taoneimianji"));
            	house.setFangwuchaoxiang(rs.getString("fangwuchaoxiang"));
            	house.setZhuangxiuqingkuang(rs.getString("zhuangxiuqingkuang"));
            	house.setGongnuanfangshi(rs.getString("gongnuanfangshi"));
            	house.setChanquannianxian(rs.getInt("chanquannianxian"));
            	house.setSuozailouceng(rs.getString("suozailouceng"));
            	
            	house.setZonglouceng(rs.getInt("zonglouceng"));
            	house.setHuxingjiegou(rs.getString("huxingjiegou"));
            	house.setJianzhuleixing(rs.getString("jianzhuleixing"));
            	house.setJianchengniandai(rs.getInt("jianchengniandai"));
            	house.setJianzhujiegou(rs.getString("jianzhujiegou"));
            	house.setTihubili(rs.getString("tihubili"));
            	house.setPeibeidianti(rs.getString("peibeidianti"));
            	
            	house.setGuapaishijian(new Date(rs.getTimestamp("guapaishijian").getTime()));
            	house.setShangcijiaoyi(new Date(rs.getTimestamp("shangcijiaoyi").getTime()));
            	house.setFangwunianxian(rs.getString("fangwunianxian"));
            	house.setDiyaxinxi(rs.getString("diyaxinxi"));
            	house.setJiaoyiquanshu(rs.getString("jiaoyiquanshu"));
            	house.setFangwuyongtu(rs.getString("fangwuyongtu"));
            	house.setChanquansuoshu(rs.getString("chanquansuoshu"));
            	house.setFangbenbeijian(rs.getString("fangbenbeijian"));

            	house.setZhuangtai(rs.getString("zhuangtai"));
            	house.setGuapaijiage(rs.getFloat("guapaijiage"));
            	house.setChengjiaojiage(rs.getFloat("chengjiaojiage"));
            	house.setChengjiaoriqi(new Date(rs.getTimestamp("chengjiaoriqi").getTime()));
            	house.setChengjiaotujing(rs.getString("chengjiaotujing"));
            	house.setLiulan(rs.getInt("liulan"));
            	house.setGuanzhu(rs.getInt("guanzhu"));
            	house.setDaikan(rs.getInt("daikan"));
            	house.setTiaojia(rs.getInt("tiaojia"));
            	house.setZhouqi(rs.getInt("zhouqi"));
            	house.setCreateTime(new Date(rs.getTimestamp("createTime").getTime()));
                house.setUpdateTime(new Date(rs.getTimestamp("updateTime").getTime()));
            	
            	list_selling.add(house);
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
	
	
	public static boolean houseExist(String houseId)
	{		
		ConnPoolUtil connPool = ConnPoolUtil.getInstance();
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		boolean res = false;
		try {
			String strSQL = "select * from t_houseSelling where houseId=?";
			conn = connPool.getConnection();
			pstmt = conn.prepareStatement(strSQL);
			pstmt.setString(1, houseId);
			rs = pstmt.executeQuery();  
            if (rs.next()) { 
            	res = true;
            }  
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
		return res;
	}

	//查找表r_houseSelling中已有数据的guapaishijian最小值
	public static Date QueryMinDate(String city, OperationState state)
	{
		Date minDate = new Date();
		state.result = false;
		ConnPoolUtil connPool = ConnPoolUtil.getInstance();
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null; 	
		try {
			String strSQL = "select min(guapaishijian) as minDate from t_houseSelling where chengshi=?";
			conn = connPool.getConnection();
			pstmt = conn.prepareStatement(strSQL);
			pstmt.setString(1, city);
			rs = pstmt.executeQuery();
            if (rs.next()) {
            	if (rs.getTimestamp("minDate") != null)
            	{
            		minDate = new Date(rs.getTimestamp("minDate").getTime());
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
		return minDate;
	}	
	
	//查找表r_houseSelling中已有数据的guapaishijian最大值
	public static Date QueryMaxDate(String city, OperationState state)
	{
		Date maxDate = new Date();
		state.result = false;
		ConnPoolUtil connPool = ConnPoolUtil.getInstance();
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null; 
		try {
			String strSQL = "select max(guapaishijian) as maxDate from t_houseSelling where chengshi=?";
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

	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		//
		HouseSelling house = GetHouse("101102410314");
		
		
		String str = UtilHelper.ObjectToString(house);
		System.out.println(str);
		
		
	}

}
