package com.niufangzi.dao;

import java.io.UnsupportedEncodingException;
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

//统计每个市的日调价信息
public class RptChangeDayDao {

	private static Logger logger = LogManager.getLogger();
	
	//(此函数暂未使用)
	private static boolean AddChangeDay(Date changeDate, String chengshi, int cCount, float cRate)
	{
		ConnPoolUtil connPool = ConnPoolUtil.getInstance();
		Connection conn = null;
		PreparedStatement pstmt = null;
		boolean res = true;
		try {
			String strSQL = "insert into r_ChangeDay(changeDate, chengshi, cCount, cRate, createTime) values(?,?,?,?, now())";
			conn = connPool.getConnection();
			pstmt = conn.prepareStatement(strSQL);

			java.sql.Timestamp ts = new java.sql.Timestamp(changeDate.getTime());
			pstmt.setTimestamp(1, ts);
			pstmt.setString(2, chengshi);
			pstmt.setInt(3, cCount);
			pstmt.setFloat(4, cRate);
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
	
	
	//删除某个城市某时间段的调价统计信息 (此函数暂未使用)
	private static boolean DeleteChangeDays(String chengshi, Date startDate, Date endDate)
	{
		ConnPoolUtil connPool = ConnPoolUtil.getInstance();
		Connection conn = null;
		PreparedStatement pstmt = null;
		boolean res = true;
		try {
			String strSQL = "Delete from r_ChangeDay where chengshi=? and changeDate>=? and changeDate<=?";
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
	
	//从‘调价详情’表中生成统计信息写入统计表
	public static Boolean SumDays(Date startDate)
	{
		ConnPoolUtil connPool = ConnPoolUtil.getInstance();
		Connection conn = null;
		PreparedStatement pstmt1 = null;
		PreparedStatement pstmt2 = null;
		Boolean res = true;
		try {
			conn = connPool.getConnection();
			java.sql.Timestamp tsStart = new java.sql.Timestamp(startDate.getTime());
			pstmt1 = conn.prepareStatement("Delete from r_ChangeDay where datediff(changeDate, ?)>=0 ");
			pstmt1.setTimestamp(1, tsStart);
			// 20180404_yef， mysql 5.7.21版本的bug：
			// insert into... select ...语句中如果除数为0(尽管已经通过where语句过滤掉)， 仍会报： Data Truncate, Division by 0 错误。
			// 增加 greatest(oldPrice, 1) 暂时解决此问题。
			String strSQL = "Insert into r_changeDay(chengshi, changeDate, cCount, cRate, createTime) "
						+ "select chengshi, strDate, count(*) as ntotal, avg(rate) as rtotal, now() from "
						+ "(select chengshi, DATE_FORMAT(createTime,'%Y%m%d') as strDate, (newPrice-oldPrice)*100/greatest(oldPrice, 1) as rate "
						+ "from t_housechange where datediff(createTime, ?)>=0 and oldPrice>0) as a "
						+ "where rate>-50 and rate<50 group by chengshi, strDate ";  //合理的调价幅度在-50%至50%之间 
			pstmt2 = conn.prepareStatement(strSQL);
			pstmt2.setTimestamp(1, tsStart);
			
			pstmt1.executeUpdate();
			pstmt2.executeUpdate();
        } catch (Exception e) {  
        	logger.error(e.getMessage());
        	res = false;
        } finally {  
            connPool.returnConnection(conn);
            try {
				pstmt1.close();
				pstmt2.close();
			} catch (SQLException e) {
				//
			}
        }
		return res;
	}	
	
	
	//查询列表-调价套数&平均幅度 信息
	// queryType: 0:按日统计；   1：按周统计；  2：按月统计；   3：按季度统计；   4：按年统计；
	public static boolean QueryRecords(String chengshi, int maxCount, PeriodType pType, List<String> list_date, List<Integer> list_count, List<Float> list_rate)
	{
		ConnPoolUtil connPool = ConnPoolUtil.getInstance();
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null; 	
		Boolean res = true;
		list_date.clear();
		list_count.clear();
		list_rate.clear();
		String strTmp = CommonDao.GetSumDateFormat("changeDate", pType);
		try {
			String strSQL = "select " + strTmp + " as strDate, sum(cCount) as sumCount, sum(cCount*cRate)/sum(cCount) as sumRate " 
						+ " from r_changeDay where chengshi=? group by strDate order by strDate desc limit ? ";
			
			conn = connPool.getConnection();
			pstmt = conn.prepareStatement(strSQL);
			pstmt.setString(1, chengshi);
			pstmt.setInt(2, maxCount);
			rs = pstmt.executeQuery();
            while (rs.next()) {
            	list_date.add(rs.getString("strDate"));
            	list_count.add(rs.getInt("sumCount"));
            	list_rate.add(rs.getFloat("sumRate"));
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
	
	//查找表r_ChangeDay中已有数据的changeDate最大值
	public static Date QueryMaxDate(OperationState state)
	{
		Date maxDate = new Date();
		state.result = false;
		ConnPoolUtil connPool = ConnPoolUtil.getInstance();
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null; 	
		try {
			String strSQL = "select max(changeDate) as maxDate from r_changeDay";
			conn = connPool.getConnection();
			pstmt = conn.prepareStatement(strSQL);
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
		//
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		String str;
		try {
			str = java.net.URLDecoder.decode("西城", "UTF-8");
			System.out.println(str);
			str = java.net.URLDecoder.decode("%E7%9F%B3%E6%99%AF%E5%B1%B1", "UTF-8");
			System.out.println(str);
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		//DoTest();
	}

}
