package com.niufangzi.model;

import java.util.Date;

// 断点类
// 记录了任务终止时的信息， 下次重新执行任务时，从上次断点开始继续执行
public class BreakPoint {

	public int getCityIdx() {
		return cityIdx;
	}
	public void setCityIdx(int cityIdx) {
		this.cityIdx = cityIdx;
	}
	public int getTaskType() {
		return taskType;
	}
	public void setTaskType(int taskType) {
		this.taskType = taskType;
	}
	public int getLevel() {
		return level;
	}
	public void setLevel(int level) {
		this.level = level;
	}
	public int getQuIdx() {
		return quIdx;
	}
	public void setQuIdx(int quIdx) {
		this.quIdx = quIdx;
	}
	public int getJiedaoIdx() {
		return jiedaoIdx;
	}
	public void setJiedaoIdx(int jiedaoIdx) {
		this.jiedaoIdx = jiedaoIdx;
	}
	public int getPageNo() {
		return pageNo;
	}
	public void setPageNo(int pageNo) {
		this.pageNo = pageNo;
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
	
	private int cityIdx;    //城市编号
	private int taskType;   //任务类型  0：selling;  1:sold;
	private int level;      //任务深度
	private int quIdx;      //区索引
	private int jiedaoIdx;  //街道索引
	private int pageNo;     //页号
	private Date createTime; //创建时间
	private Date updateTime; //更新时间
	
	public enum TaskType { SELLING, SOLD }
	public enum TaskLevel { CITY, QU, JIEDAO }
		
	//构造函数
	public BreakPoint()
	{
		//
	}		
	
	//构造函数
	public BreakPoint(int cityIdx, int taskType, int level, int quIdx, int jiedaoIdx, int pageNo)
	{
		this.cityIdx = cityIdx;
		this.taskType = taskType;
		this.level = level;
		this.quIdx = quIdx;
		this.jiedaoIdx = jiedaoIdx;
		this.pageNo = pageNo;
		this.createTime = new Date();
		this.updateTime = new Date();
	}
	
}
