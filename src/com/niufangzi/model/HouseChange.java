package com.niufangzi.model;

import java.util.Date;

//房屋的‘挂牌价格’变动记录
public class HouseChange {
	public String getHouseId() {
		return houseId;
	}
	public void setHouseId(String houseId) {
		this.houseId = houseId;
	}
	public String getChengshi() {
		return chengshi;
	}
	public void setChengshi(String chengshi) {
		this.chengshi = chengshi;
	}
	public String getQuyu1() {
		return quyu1;
	}
	public void setQuyu1(String quyu1) {
		this.quyu1 = quyu1;
	}
	public String getQuyu2() {
		return quyu2;
	}
	public void setQuyu2(String quyu2) {
		this.quyu2 = quyu2;
	}
	public String getXiaoqu() {
		return xiaoqu;
	}
	public void setXiaoqu(String xiaoqu) {
		this.xiaoqu = xiaoqu;
	}
	public String getFangwuhuxing() {
		return fangwuhuxing;
	}
	public void setFangwuhuxing(String fangwuhuxing) {
		this.fangwuhuxing = fangwuhuxing;
	}
	public float getJianzhumianji() {
		return jianzhumianji;
	}
	public void setJianzhumianji(float jianzhumianji) {
		this.jianzhumianji = jianzhumianji;
	}
	public float getOldPrice() {
		return oldPrice;
	}
	public void setOldPrice(float oldPrice) {
		this.oldPrice = oldPrice;
	}
	public float getNewPrice() {
		return newPrice;
	}
	public void setNewPrice(float newPrice) {
		this.newPrice = newPrice;
	}
	public Date getOldDate() {
		return oldDate;
	}
	public void setOldDate(Date oldDate) {
		this.oldDate = oldDate;
	}
	public Date getNewDate() {
		return newDate;
	}
	public void setNewDate(Date newDate) {
		this.newDate = newDate;
	}
	public Date getCreateTime() {
		return createTime;
	}
	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}
	
	private String  houseId;     //房屋编号
	private String  chengshi;    //城市
	private String  quyu1;       //区域1
	private String  quyu2;       //区域2
	private String  xiaoqu;      //小区名称
	private String  fangwuhuxing; //房屋户型
	private float   jianzhumianji; //建筑面积
	private float   oldPrice;    //旧价格
	private float   newPrice;    //新价格
	private Date    oldDate;     //旧挂牌时间
	private Date    newDate;     //新挂牌时间
	private Date    createTime;  //创建时间
	
	public HouseChange()
	{
		//
	}

	public HouseChange(String houseId, String chengshi, String quyu1, String quyu2, String xiaoqu, String fangwuhuxing, float jianzhumianji, 
						float oldPrice, float newPrice, Date oldDate, Date newDate)
	{
		this.houseId = houseId;
		this.chengshi = chengshi;
		this.quyu1 = quyu1;
		this.quyu2 = quyu2;
		this.xiaoqu = xiaoqu;
		this.fangwuhuxing = fangwuhuxing;
		this.jianzhumianji = jianzhumianji;
		this.oldPrice = oldPrice;
		this.newPrice = newPrice;
		this.oldDate = oldDate;
		this.newDate = newDate;
		this.createTime = new Date();
	}
	

}
