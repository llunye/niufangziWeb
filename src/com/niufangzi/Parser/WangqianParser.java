package com.niufangzi.Parser;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.niufangzi.model.OperationState;
import com.niufangzi.model.Wangqian;
import com.niufangzi.util.UtilHelper;

// 从北京住建委网站上下载 网签信息
public class WangqianParser {
	private static Logger logger = LogManager.getLogger();
	private static String jianweiURL = "http://www.bjjs.gov.cn/bjjs/fwgl/fdcjy/index.shtml";
	//http://210.75.213.188/shh/portal/bjjs2016/index.aspx
	
	private static boolean ParseContent(Document doc, Wangqian wq)
	{	
		OperationState state = new OperationState();
		Element eleContent = doc.getElementsByClass("fdcsjtj_content").first();
		
		//<h3>2018/1/18期房网上签约</h3> 
		String strTmp = eleContent.getElementsByTag("h3").first().text();
		String strDate = strTmp.replace("期房网上签约", "").replace("/", "-");
		Date wangqianDate = UtilHelper.tryStrToDate(strDate, "yyyy-MM-dd", state);
		if (state.result) //如果网签日期格式不正确，返回错误
		{
			wq.setWangqianDate(wangqianDate);
		}
		else
		{
			return false;
		}	
		Elements elesTmp = eleContent.getElementsByClass("r");
		if (elesTmp.size() < 12)
		{
			return false;
		}
		// 以下字段不检查转换结果(state)， 如果转换失败，就用默认值赋值。
		//期房信息
		int iTmp = UtilHelper.tryStrToInt(elesTmp.get(0).text(), state);
		wq.setQiCount(iTmp);
		iTmp = UtilHelper.tryStrToInt(elesTmp.get(1).text().replace("(㎡)", ""), state);
		wq.setQiArea(iTmp);
		iTmp = UtilHelper.tryStrToInt(elesTmp.get(2).text(), state);
		wq.setQiZhuCount(iTmp);
		iTmp = UtilHelper.tryStrToInt(elesTmp.get(3).text().replace("(㎡)", ""), state);
		wq.setQiZhuArea(iTmp);
		
		//现房信息
		iTmp = UtilHelper.tryStrToInt(elesTmp.get(4).text(), state);
		wq.setXianCount(iTmp);
		iTmp = UtilHelper.tryStrToInt(elesTmp.get(5).text().replace("(㎡)", ""), state);
		wq.setXianArea(iTmp);
		iTmp = UtilHelper.tryStrToInt(elesTmp.get(6).text(), state);
		wq.setXianZhuCount(iTmp);
		iTmp = UtilHelper.tryStrToInt(elesTmp.get(7).text().replace("(㎡)", ""), state);
		wq.setXianZhuArea(iTmp);
		
		//存量房信息
		iTmp = UtilHelper.tryStrToInt(elesTmp.get(8).text(), state);
		wq.setCunCount(iTmp);
		iTmp = UtilHelper.tryStrToInt(elesTmp.get(9).text().replace("(㎡)", ""), state);
		wq.setCunArea(iTmp);
		iTmp = UtilHelper.tryStrToInt(elesTmp.get(10).text(), state);
		wq.setCunZhuCount(iTmp);
		iTmp = UtilHelper.tryStrToInt(elesTmp.get(11).text().replace("(㎡)", ""), state);
		wq.setCunZhuArea(iTmp);
		return true;
	}
	
	
	public static boolean ParseWangqian(Wangqian wq)
	{
		Document doc;
		boolean res = true;
		try {
			doc = Jsoup.connect(jianweiURL).get();
			res = ParseContent(doc, wq);
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
