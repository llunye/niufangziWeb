package com.niufangzi.model;

//在函数调用间，传递操作结果
public class OperationState {

	public boolean result;        //操作结果
	public String functionName;   //函数名
	public String errorMsg;       //错误消息
	
	public OperationState()
	{
		result = true;
		functionName = "";
		errorMsg = "";
	}
	
	public void SetState(boolean result, String functionName, String errorMsg)
	{
		this.result = result;
		this.functionName = functionName;
		this.errorMsg = errorMsg;
	}

}
