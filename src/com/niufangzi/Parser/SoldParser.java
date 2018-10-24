package com.niufangzi.Parser;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
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
public class SoldParser {
	private static Logger logger = LogManager.getLogger();
	
	public static boolean ParseAreaInfo(Document doc, HouseSold house)
	{		
		Elements eles_xiaoqu = doc.getElementsByClass("deal-bread").first().getElementsByTag("a");
		String strTmp = eles_xiaoqu.get(1).text().replace("二手房成交价格", "");  //城市
		house.setChengshi(strTmp.trim());
		strTmp = eles_xiaoqu.get(2).text().replace("二手房成交价格", "");   //区
		house.setQuyu1(strTmp.trim());
		strTmp = eles_xiaoqu.get(3).text().replace("二手房成交价格", "");   //街道
		house.setQuyu2(strTmp.trim());
		
		strTmp = doc.getElementsByClass("house-title").first().getElementsByClass("wrapper").first().text().trim();
		int idx = strTmp.indexOf(" ");
		house.setXiaoqu(strTmp.substring(0, idx));
		return true;
	}
	
	public static boolean ParseBaseInfo(Document doc, HouseSold house)
	{
		OperationState state = new OperationState();
		Element ele = doc.getElementsByClass("houseContentBox").first().getElementsByClass("introContent").first();
		Elements eles_base = ele.getElementsByClass("base").first().getElementsByClass("content").first().getElementsByTag("li");
		for (Element label : eles_base) {
			String strKey = label.getElementsByClass("label").first().text().trim();
			label.children().remove();
			String strValue = label.text().trim();
			
			//logger.debug("strKey : " + strKey + ",  strValue :" +  strValue);
			
			if (strKey.equals("房屋户型"))  //1室1厅1厨1卫
			{
				house.setFangwuhuxing(strValue);
			}
			else if (strKey.equals("所在楼层"))  //中楼层 (共6层)
			{
				int idx = strValue.indexOf("(");
				house.setSuozailouceng(strValue.substring(0, idx-1).trim());
				house.setZonglouceng(UtilHelper.tryStrToInt(strValue.substring(idx).replace("(共", "").replace("层)", ""), state));
			}
			else if (strKey.equals("建筑面积"))  //63.74㎡
			{
				String str = strValue.replace("㎡", "");
				house.setJianzhumianji(UtilHelper.tryStrToFloat(str, state));
			}
			else if (strKey.equals("户型结构"))  //平层
			{
				house.setHuxingjiegou(strValue);
			}
			else if (strKey.equals("套内面积"))  //51.79㎡
			{
				if (! strValue.equals("暂无数据"))
				{
					String str = strValue.replace("㎡", "");
					house.setTaoneimianji(UtilHelper.tryStrToFloat(str, state));
				}
			}
			else if (strKey.equals("建筑类型"))  //板楼
			{
				house.setJianzhuleixing(strValue);
			}
			else if (strKey.equals("房屋朝向"))  //西
			{
				house.setFangwuchaoxiang(strValue);
			}
			else if (strKey.equals("建筑结构"))  //钢混结构
			{
				house.setJianzhujiegou(strValue);
			}
			else if (strKey.equals("装修情况"))  //精装
			{
				house.setZhuangxiuqingkuang(strValue);
			}
			else if (strKey.equals("建成年代"))  //2009
			{
				house.setJianchengniandai(UtilHelper.tryStrToInt(strValue, state));
			}
			else if (strKey.equals("梯户比例"))  //一梯四户
			{
				house.setTihubili(strValue);
			}
			else if (strKey.equals("供暖方式"))  //自供暖
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
	
	public static boolean ParseTransInfo(Document doc, HouseSold house)
	{
		OperationState state = new OperationState();
		Element ele = doc.getElementsByClass("houseContentBox").first().getElementsByClass("introContent").first();
		Elements eles_trans = ele.getElementsByClass("transaction").first().getElementsByClass("content").first().getElementsByTag("li");
		for (Element label : eles_trans) {
			String strKey = label.getElementsByClass("label").first().text().trim();
			label.children().remove();
			String strValue = label.text().trim();
			
			//logger.debug("strKey : " + strKey + ",  strValue :" +  strValue);
			
			if (strKey.equals("挂牌时间"))  //2017-03-21
			{
				house.setGuapaishijian(UtilHelper.tryStrToDate(strValue, "yyyy-MM-dd", state));
			}
			else if (strKey.equals("交易权属"))  //商品房
			{
				house.setJiaoyiquanshu(strValue);
			}
			else if (strKey.equals("房屋用途"))  //普通住宅
			{
				house.setFangwuyongtu(strValue);
			}
			else if (strKey.equals("房屋年限"))  //满五年
			{
				house.setFangwunianxian(strValue);
			}
			else if (strKey.equals("房权所属"))  //非共有
			{
				house.setChanquansuoshu(strValue);
			}
		}
		String strTmp = doc.getElementsByClass("house-title").first().getElementsByClass("wrapper").first()
							.getElementsByTag("span").first().text();
		String[] list_tmp = strTmp.split(" ");
		if (list_tmp.length > 0)
		{
			String strChengjiao = list_tmp[0].replace(".", "-");
			// 对于第三方成交的， 可能没有详细的成交日期，  例如 2013 或 2013.07
			String list_ydm[] = strChengjiao.split("-");
			if (list_ydm.length == 1) //补上： 月、日
			{
				strChengjiao = strChengjiao + "-01-01";
			}
			else if (list_ydm.length == 2)  //补上： 日
			{
				strChengjiao = strChengjiao + "-01";
			}
			
			house.setChengjiaoriqi(UtilHelper.tryStrToDate(strChengjiao, "yyyy-MM-dd", state));
		}
		if (list_tmp.length > 1)
		{
			house.setChengjiaotujing(list_tmp[1]);
		}
		return true;
	}
	
	public static boolean ParseOverviewInfo(Document doc, HouseSold house)
	{
		OperationState state = new OperationState();
		Element eleTmp = doc.getElementsByClass("overview").first().getElementsByClass("price").first().getElementsByClass("dealTotalPrice").first();
		house.setChengjiaojiage(UtilHelper.tryStrToFloat(eleTmp.getElementsByTag("i").first().text(), state));
		
		//处理单位为‘亿’的情况
		eleTmp.children().remove();
		String strUnit = eleTmp.text().trim();
		if (strUnit.equals("亿")) {
			house.setChengjiaojiage(house.getChengjiaojiage() * 10000);
		}
		
		eleTmp = doc.getElementsByClass("overview").first().getElementsByClass("msg").first();
		Elements labels = eleTmp.getElementsByTag("label");
		house.setGuapaijiage(UtilHelper.tryStrToFloat(labels.get(0).text(), state));
		house.setZhouqi(UtilHelper.tryStrToInt(labels.get(1).text(), state));
		house.setTiaojia(UtilHelper.tryStrToInt(labels.get(2).text(), state));
		house.setDaikan(UtilHelper.tryStrToInt(labels.get(3).text(), state));
		house.setGuanzhu(UtilHelper.tryStrToInt(labels.get(4).text(), state));
		house.setLiulan(UtilHelper.tryStrToInt(labels.get(5).text(), state));
		return true;
	}
	
	public static boolean ParsePhotoInfo(Document doc, String houseId, List<PhotoSold> list_photo)
	{		
		Elements elePhotos = doc.getElementsByClass("overview").first().getElementById("thumbnail2").getElementsByTag("li");
		for (Element ele : elePhotos) {
			PhotoSold photo = new PhotoSold();
			photo.setHouseId(houseId);
			photo.setPhotoName(ele.attr("data-desc").trim());
			//photo.setUrl1(ele.getElementsByTag("img").first().attr("src").trim());
			photo.setUrl(ele.attr("data-src").trim());  //只保存中等尺寸的图片
			list_photo.add(photo);
		}
		return true;
	}
	
	public static boolean ParseAHouse(String houseURL, String houseId, HouseSold house, List<PhotoSold> list_photo)
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
		house.setZhuangtai("成交");
		house.setShangcijiaoyi(new Date());
		
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
			//logger.error(e.getMessage());  //有些市‘自行成交’ 或 ‘第三方成交’，没有价格信息，此处不打印错误信息
		}
		
		try {
			ParsePhotoInfo(doc, houseId, list_photo);
		} catch (Exception e) {
			logger.error(e.getMessage());
		}

		return true;
	}
	
	
	private static void DoTest()
	{
		String houseURL = "https://bj.lianjia.com/chengjiao/101102222025.html";
		String houseId = "101102222025";
		
		HouseSold house = new HouseSold();
		List<PhotoSold> list_photo = new ArrayList<PhotoSold>();
		
		boolean res = ParseAHouse(houseURL, houseId, house, list_photo);
		String str =UtilHelper.ObjectToString(house);
		System.out.println(str);
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		//DoTest();
	}

}
