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

import com.niufangzi.model.*;
import com.niufangzi.util.UtilHelper;

//在解析转换各个字段值时，并不检查字段转换的结果(state),如果失败，就使用默认值
public class SellingParser {
	private static Logger logger = LogManager.getLogger();
	
	public static boolean ParseAreaInfo(Document doc, HouseSelling house)
	{		
		Element ele_xiaoqu = doc.getElementsByClass("communityName").first().getElementsByClass("info").first();
		house.setXiaoqu(ele_xiaoqu.text().trim());

		/*
		Element ele = doc.getElementsByClass("areaName").first().getElementsByClass("info").first();
		String[] list_area = ele.text().split(" ");
		if (list_area.length > 2)
		{
			house.setQuyu1(list_area[0]);
			house.setQuyu2(list_area[1]);
			house.setQuyu3(list_area[2]);
			house.setQuyu4("");
		}
		*/
		Elements eles = doc.getElementsByClass("intro clear").first().getElementsByTag("a");	
		String strTmp = eles.get(1).text().replace("二手房", "");  //城市
		house.setChengshi(strTmp.trim());
		strTmp = eles.get(2).text().replace("二手房", "");   //区
		house.setQuyu1(strTmp.trim());
		strTmp = eles.get(3).text().replace("二手房", "");   //街道
		house.setQuyu2(strTmp.trim());
	
		return true;
	}
	
	public static boolean ParseBaseInfo(Document doc, HouseSelling house)
	{
		OperationState state = new OperationState();
		Element ele = doc.getElementsByClass("introContent").first().getElementsByClass("content").first();
		Elements eles_base = ele.getElementsByTag("li");
		for (Element label : eles_base) {
			String strKey = label.getElementsByClass("label").first().text().trim();
			label.children().remove();
			String strValue = label.text().trim();
			
			//logger.debug("strKey : " + strKey + ",  strValue :" +  strValue);
			
			if (strKey.equals("房屋户型"))  //3室1厅1厨1卫
			{
				house.setFangwuhuxing(strValue);
			}
			else if (strKey.equals("所在楼层"))  //中楼层 (共12层)
			{
				int idx = strValue.indexOf("(");
				house.setSuozailouceng(strValue.substring(0, idx-1).trim());
				house.setZonglouceng(UtilHelper.tryStrToInt(strValue.substring(idx).replace("(共", "").replace("层)", ""), state));
			}
			else if (strKey.equals("建筑面积"))  //109.8㎡
			{
				String str = strValue.replace("㎡", "");
				house.setJianzhumianji(UtilHelper.tryStrToFloat(str, state));
			}
			else if (strKey.equals("户型结构"))  //平层
			{
				house.setHuxingjiegou(strValue);
			}		
			else if (strKey.equals("套内面积"))  //86.26㎡
			{
				if (! strValue.equals("暂无数据"))
				{
					String str = strValue.replace("㎡", "");
					house.setTaoneimianji(UtilHelper.tryStrToFloat(str, state));
				}
			}
			else if (strKey.equals("建筑类型"))  //板塔结合
			{
				house.setJianzhuleixing(strValue);
			}
			else if (strKey.equals("房屋朝向"))  //南 北
			{
				house.setFangwuchaoxiang(strValue);
			}
			else if (strKey.equals("建筑结构"))  //钢混结构
			{
				house.setJianzhujiegou(strValue);
			}
			else if (strKey.equals("装修情况"))  //简装
			{
				house.setZhuangxiuqingkuang(strValue);
			}
			else if (strKey.equals("梯户比例"))  //两梯两户
			{
				house.setTihubili(strValue);
			}
			else if (strKey.equals("供暖方式"))  //集中供暖
			{
				house.setGongnuanfangshi(strValue);
			}
			else if (strKey.equals("配备电梯"))  //有
			{
				house.setPeibeidianti(strValue);
			}
			else if (strKey.equals("产权年限"))  //70年
			{
				String str = strValue.replace("年", "");
				house.setChanquannianxian(UtilHelper.tryStrToInt(str, state));
			}
		}
		return true;
	}
	
	public static boolean ParseTransInfo(Document doc, HouseSelling house)
	{
		OperationState state = new OperationState();
		Element ele = doc.getElementsByClass("transaction").first().getElementsByClass("content").first();
		Elements eles_trans = ele.getElementsByTag("li");
		for (Element label : eles_trans) {
			String strKey = label.getElementsByClass("label").first().text().trim();
			String strValue = "";
			
			strValue = label.getElementsByTag("span").get(1).text().trim();
			if (strKey.equals("挂牌时间"))  //2018-01-06
			{
				house.setGuapaishijian(UtilHelper.tryStrToDate(strValue, "yyyy-MM-dd", state));
			}
			else if (strKey.equals("交易权属"))  //已购公房
			{
				house.setJiaoyiquanshu(strValue);
			}
			else if (strKey.equals("上次交易"))  //2001-10-10
			{
				house.setShangcijiaoyi(UtilHelper.tryStrToDate(strValue, "yyyy-MM-dd", state));
			}
			else if (strKey.equals("房屋用途"))  //普通住宅
			{
				house.setFangwuyongtu(strValue);
			}
			else if (strKey.equals("房屋年限"))  //满五年
			{
				house.setFangwunianxian(strValue);
			}
			else if (strKey.equals("产权所属"))  //非共有
			{
				house.setChanquansuoshu(strValue);
			}
			else if (strKey.equals("抵押信息"))  //无抵押
			{
				house.setDiyaxinxi(strValue);
			}
			else if (strKey.equals("房本备件"))  //已上传房本照片
			{
				house.setFangbenbeijian(strValue);
			}
		}
		return true;
	}
	
	public static boolean ParseOverviewInfo(Document doc, HouseSelling house)
	{
		OperationState state = new OperationState();
		Element eleTmp = doc.getElementsByClass("overview").first().getElementsByClass("price").first().getElementsByClass("total").first();
		house.setGuapaijiage(UtilHelper.tryStrToFloat(eleTmp.text(), state));
		
		//处理单位为‘亿’的情况
		eleTmp = doc.getElementsByClass("overview").first().getElementsByClass("price").first().getElementsByClass("unit").first();
		String strUnit =  eleTmp.getElementsByTag("span").first().text().trim();
		if (strUnit.equals("亿")) {
			house.setGuapaijiage(house.getGuapaijiage() * 10000);
		}
		
		eleTmp = doc.getElementsByClass("sellDetailHeader").first();
		house.setGuanzhu(UtilHelper.tryStrToInt(eleTmp.getElementById("favCount").text(), state));
		house.setDaikan(UtilHelper.tryStrToInt(eleTmp.getElementById("cartCount").text(), state));
		
		eleTmp = doc.getElementsByClass("overview").first().getElementsByClass("houseInfo").first().getElementsByClass("area").first();
		String strTmp = eleTmp.getElementsByClass("subInfo").first().text().substring(0, 4);
		house.setJianchengniandai(UtilHelper.tryStrToInt(strTmp, state));
		return true;
	}
	
	public static boolean ParsePhotoInfo(Document doc, String houseId, List<PhotoSelling> list_photo)
	{		
		Elements elePhotos = doc.getElementsByClass("overview").first().getElementsByClass("smallpic").first().getElementsByTag("li");
		for (Element ele : elePhotos) {
			PhotoSelling photo = new PhotoSelling();
			photo.setHouseId(houseId);
			photo.setPhotoName(ele.attr("data-desc").trim());
			//photo.setUrl1(ele.getElementsByTag("img").first().attr("src").trim());
			photo.setUrl(ele.attr("data-src").trim());  //只保存中等尺寸的图片
			//photo.setUrl3(ele.attr("data-pic").trim());
			list_photo.add(photo);
		}
		return true;
	}
	
	public static boolean ParseAHouse(String houseURL, String houseId, HouseSelling house, List<PhotoSelling> list_photo)
	{
		Document doc;
		try {
			doc = Jsoup.connect(houseURL).get();
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
		catch (NullPointerException e3) {
			logger.error(e3.getMessage());
			return false;
		}
		
		house.setHouseId(houseId);
		house.setZhuangtai("在售");
		
		try {
			ParseAreaInfo(doc, house);
		} catch (Exception e) {
			logger.error(e.getMessage());
		}
		
		try {
			ParseBaseInfo(doc, house);
		} catch (Exception e) {
			logger.error(e.getMessage());
		}
		
		try {
			ParseTransInfo(doc, house);
		} catch (Exception e) {
			logger.error(e.getMessage());
		}
		
		try {
			ParseOverviewInfo(doc, house);
		} catch (Exception e) {
			logger.error(e.getMessage());
		}
		
		try {
			ParsePhotoInfo(doc, houseId, list_photo);
		} catch (Exception e) {
			logger.error(e.getMessage());
		}

		return true;
	}
	
	
	private static boolean DoTest()
	{
		//101102498522 (万)
		//101102105035 （亿）
		String houseURL = "https://bj.lianjia.com/ershoufang/101102105035.html";
		String houseId = "101102105035";
		HouseSelling house = new HouseSelling();
		List<PhotoSelling> list_photo = new ArrayList<PhotoSelling>();
		
		boolean res = ParseAHouse(houseURL, houseId, house, list_photo);	
		String str =UtilHelper.ObjectToString(house);
		System.out.println(str);
		return res;
	}
	
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		DoTest();
	}

}
