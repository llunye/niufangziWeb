package com.niufangzi.model;

import java.util.Date;

//网签信息
public class Wangqian {

	public Date getWangqianDate() {
		return wangqianDate;
	}
	public void setWangqianDate(Date wangqianDate) {
		this.wangqianDate = wangqianDate;
	}
	public int getQiCount() {
		return qiCount;
	}
	public void setQiCount(int qiCount) {
		this.qiCount = qiCount;
	}
	public int getQiArea() {
		return qiArea;
	}
	public void setQiArea(int qiArea) {
		this.qiArea = qiArea;
	}
	public int getQiZhuCount() {
		return qiZhuCount;
	}
	public void setQiZhuCount(int qiZhuCount) {
		this.qiZhuCount = qiZhuCount;
	}
	public int getQiZhuArea() {
		return qiZhuArea;
	}
	public void setQiZhuArea(int qiZhuArea) {
		this.qiZhuArea = qiZhuArea;
	}
	public int getXianCount() {
		return xianCount;
	}
	public void setXianCount(int xianCount) {
		this.xianCount = xianCount;
	}
	public int getXianArea() {
		return xianArea;
	}
	public void setXianArea(int xianArea) {
		this.xianArea = xianArea;
	}
	public int getXianZhuCount() {
		return xianZhuCount;
	}
	public void setXianZhuCount(int xianZhuCount) {
		this.xianZhuCount = xianZhuCount;
	}
	public int getXianZhuArea() {
		return xianZhuArea;
	}
	public void setXianZhuArea(int xianZhuArea) {
		this.xianZhuArea = xianZhuArea;
	}
	public int getCunCount() {
		return cunCount;
	}
	public void setCunCount(int cunCount) {
		this.cunCount = cunCount;
	}
	public int getCunArea() {
		return cunArea;
	}
	public void setCunArea(int cunArea) {
		this.cunArea = cunArea;
	}
	public int getCunZhuCount() {
		return cunZhuCount;
	}
	public void setCunZhuCount(int cunZhuCount) {
		this.cunZhuCount = cunZhuCount;
	}
	public int getCunZhuArea() {
		return cunZhuArea;
	}
	public void setCunZhuArea(int cunZhuArea) {
		this.cunZhuArea = cunZhuArea;
	}
	public Date getCreateTime() {
		return createTime;
	}
	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}
	public Date getUpdateTime() {
		return updateTime;
	}
	public void setUpdateTime(Date updateTime) {
		this.updateTime = updateTime;
	}
	private Date wangqianDate;
	
	//期房(新房)
	private int qiCount;
	private int qiArea;
	private int qiZhuCount;
	private int qiZhuArea;
	
	//现房(新房)
	private int xianCount;
	private int xianArea;
	private int xianZhuCount;
	private int xianZhuArea;
	
	//存量房(二手房)
	private int cunCount;
	private int cunArea;
	private int cunZhuCount;
	private int cunZhuArea;
	
	private Date createTime;   //创建时间
	private Date updateTime;   //更新时间
	
	//构造函数
	public Wangqian()
	{
		qiCount  = 0;
		qiArea = 0;
		qiZhuCount = 0;
		qiZhuArea = 0;
		xianCount = 0;
		xianArea = 0;
		xianZhuCount = 0;
		xianZhuArea = 0;
		cunCount = 0;
		cunArea = 0;
		cunZhuCount = 0;
		cunZhuArea = 0;
		createTime = new Date();
		updateTime = new Date();
	}
	
	
}
