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

public class SellingPageParser {
	private static Logger logger = LogManager.getLogger();
	
	public static boolean ParseSellingPage(Document doc, List<String> list_id, List<String> list_url)
	{		
		//Elements eles_clear = doc.getElementsByClass("sellListContent").first().getElementsByClass("clear");
		Elements eles = doc.getElementsByClass("bigImgList").first().getElementsByClass("img");
		for (Element ele : eles) {
			//Element eleTmp = ele.getElementsByClass("info clear").first().getElementsByClass("title").first().getElementsByTag("a").first();
			String id = ele.attr("data-housecode").trim();
			String url = ele.attr("href").trim();
			if (url.length() > 0)
			{
				if (! list_id.contains(id))
				{
					list_id.add(id);
					list_url.add(url);
				}
			}
		}
		return true;
	}
	
	
	public static boolean ParseAPage(String pageURL, List<String> list_id, List<String> list_url)
	{
		Document doc;
		try {
			doc = Jsoup.connect(pageURL).get();
			ParseSellingPage(doc, list_id, list_url);
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
		List<String> list_id = new ArrayList<String>();
		List<String> list_url = new ArrayList<String>();
		boolean res = false;
		String pageURL = "https://bj.lianjia.com/ershoufang/andingmen/pg1/";
		try{
			res = SellingPageParser.ParseAPage(pageURL, list_id, list_url);
		}
		catch(Exception e) {
			logger.error(e.getMessage());
		}
		System.out.println("size:" + list_id.size());
		for(int i=0; i< list_id.size(); i++)
		{
			System.out.println(list_id.get(i) + ": " + list_url.get(i));
		}
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		//doTest();
	}

}
