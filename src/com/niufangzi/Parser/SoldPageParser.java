package com.niufangzi.Parser;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;


public class SoldPageParser {
	private static Logger logger = LogManager.getLogger();
	
	public static boolean ParseSoldPage(Document doc, List<String> list_id, List<String> list_url)
	{	
		Elements eles_title = doc.getElementsByClass("listContent").first().getElementsByClass("title");
		for (Element ele : eles_title) {
			Element eleTmp = ele.getElementsByTag("a").first();
			String url = eleTmp.attr("href");
			String list_tmp[] = url.split("/");
			String strTmp = list_tmp[list_tmp.length-1];
			String id = strTmp.replace(".html", "");
			if (! list_id.contains(id))
			{
				list_id.add(id);
				list_url.add(url);
			}
		}
		return true;
	}
	
	
	public static boolean ParseAPage(String pageURL, List<String> list_id, List<String> list_url)
	{
		Document doc;
		try {
			doc = Jsoup.connect(pageURL).get();
			ParseSoldPage(doc, list_id, list_url);
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

		return true;
	}
	
	private static void doTest()
	 {
		String pageURL = "https://bj.lianjia.com/chengjiao/pg10/";		
		List<String> list_id = new ArrayList<String>();
		List<String> list_url = new ArrayList<String>();
		
		boolean res = ParseAPage(pageURL, list_id, list_url);
		
		System.out.println("record count:" + list_id.size());
		for(int i=0; i<list_id.size(); i++)
		{
			System.out.println(list_id.get(i) + " : " + list_url.get(i));
		}	
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		//doTest();
	}

}
