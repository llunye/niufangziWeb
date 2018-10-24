package com.niufangzi.Parser;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.niufangzi.model.OperationState;
import com.niufangzi.model.Wangqian;
import com.niufangzi.util.UtilHelper;

// 从大房子网站（https://www.dafangzi.org）下载 网签信息（最近1000天）
// 此网站历史数据只含： 存量房的网签数量
public class DafangziParser {
	private static Logger logger = LogManager.getLogger();
	private static String dafangziURL = "https://www.dafangzi.org/index.php?view=table";

	
	private static boolean ParseContent(Document doc, List<Date> list_date, List<Integer> list_count)
	{	
		Elements eles = doc.getElementsByClass("ltable").first().getElementsByTag("tbody").first().getElementsByTag("tr");		
		/*
		<tr> 
        <td bgcolor="#f3f3f3">2018/1/21</td> 
        <td bgcolor="#ffffff">44</td> 
        </tr> 
        */
		
		OperationState stateDate = new OperationState();
		OperationState stateCount = new OperationState();
		for (Element ele : eles) {
			String strDate = ele.getElementsByTag("td").get(0).text().trim().replace("/", "-");
			String strCount = ele.getElementsByTag("td").get(1).text().trim();
			Date date = UtilHelper.tryStrToDate(strDate, "yyyy-MM-dd", stateDate);
			int count = UtilHelper.tryStrToInt(strCount, stateCount);
			
			if (stateDate.result && stateCount.result)
			{
				list_date.add(date);
				list_count.add(count);
			}
		}
		return true;
	}
	
	
	public static boolean ParseWangqian(List<Date> list_date, List<Integer> list_count)
	{
		boolean res = true;
		Document doc;
		try {
			doc = Jsoup.connect(dafangziURL).get();
			res = ParseContent(doc, list_date, list_count);
		}
		catch (org.jsoup.UncheckedIOException e) {
			logger.error(e.getMessage());
			return false;
		}
		catch (java.net.SocketTimeoutException e1) {
			logger.error(e1.getMessage());
			return false;
		}
		catch (IOException e2) {
			logger.error(e2.getMessage());
			return false;
		}
		
		return res;
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		//Wangqian wq = new Wangqian();
		//ParseWangQian(wq);
		
	}

}
