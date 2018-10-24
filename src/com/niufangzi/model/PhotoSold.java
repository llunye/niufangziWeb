package com.niufangzi.model;

import java.util.Date;

//“成交”房屋的图片
public class PhotoSold {

	public String getChengshi() {
		return chengshi;
	}
	public void setChengshi(String chengshi) {
		this.chengshi = chengshi;
	}
	public String getHouseId() {
		return houseId;
	}
	public void setHouseId(String houseId) {
		this.houseId = houseId;
	}
	public String getPhotoName() {
		return photoName;
	}
	public void setPhotoName(String photoName) {
		this.photoName = photoName;
	}
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	public int getStatus() {
		return status;
	}
	public void setStatus(int status) {
		this.status = status;
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

	private String chengshi;   //城市
	private String houseId;    //房屋编号
	private String photoName;  //图片名称(客厅/卧室/厨房 等)
	private String url;        //图片URL
	private int status;        //状态(0:未下载到本地； 1：已下载)
	private Date createTime;   //创建时间
	private Date updateTime;   //更新时间
	
	public PhotoSold()
	{
		status = 0;
		createTime = new Date();
		updateTime = new Date();
	}

}
