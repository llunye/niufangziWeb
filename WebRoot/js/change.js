//自动运行
$(document).ready(function(){
	LoadChangeCount(0);
	LoadChangeRate(0);
	
	$("#select_period").change(function(){
		var period = $("#select_period").val();
		LoadChangeCount(period);
		LoadChangeRate(period);
	});
});

//通过ajax从服务端读取(调价次数)数据
function LoadChangeCount(period) {
	chart_changeCount.showLoading(); //显示加载动画
	var reqURL = apiURL + location.search; //加上url里的get请求参数
	$.ajax({url: reqURL,				
		   type: "post",        //post请求方式
		  async: true,          //异步请求（同步请求将会锁住浏览器，用户其他操作必须等待请求完成才可以执行）
		   data: {Method:"ChangeCount",
			      PeriodType: period},   //请求参数
	   dataType: "json",        //返回数据形式为json
	    success: function (data){ ShowChangeCount(data); }
		});	
}

//通过ajax从服务端读取(调价幅度)数据
function LoadChangeRate(period) {
	chart_changeRate.showLoading(); 
	var reqURL = apiURL + location.search; //加上url里的get请求参数
	$.ajax({url: reqURL,				
		   type: "post",        //post请求方式
		  async: true,          //异步请求（同步请求将会锁住浏览器，用户其他操作必须等待请求完成才可以执行）
		   data: {Method:"ChangeRate",
			      PeriodType: period},   //请求参数
	   dataType: "json",        //返回数据形式为json
	    success: function (data){ ShowChangeRate(data); }
		});
}


function ShowChangeCount(resp) {
	if (resp.ReturnCode != "000000")
	{
		return false;
	}
	var counts=[];       //调价次数数组
	var dates=[];        //时间数组
	var nCount = resp.Records.length; //记录条数 
	for(var i=nCount-1; i>=0; i--)    //挨个取出数组值 
	{  
		counts.push(resp.Records[i].Count);        
		dates.push(resp.Records[i].Date);
	}
	
	chart_changeCount.setOption({        //载入数据
		title: {
			text: '调价套数'
		},
		xAxis: {
			data: dates    //填入X轴数据
		},
		series: [    //填入数据
				{
					name: '调价套数',
					data: counts
				}
		]
	});
	chart_changeCount.hideLoading();    //隐藏加载动画
}


function ShowChangeRate(resp) {
	if (resp.ReturnCode != "000000")
	{
		return false;
	}
	var rates=[];       //调价幅度数组
	var dates=[];        //时间数组
	var nCount = resp.Records.length; //记录条数 
	for(var i=nCount-1; i>=0; i--)    //挨个取出数组值 
	{  
		var rateFixed = Math.round(resp.Records[i].Rate*100) / 100;
		rates.push(rateFixed);
		dates.push(resp.Records[i].Date);
	}
	chart_changeRate.setOption({        //载入数据
		title: {
			text: '平均调价幅度'
		},
		xAxis: {
			data: dates    //填入X轴数据
		},
		series: [    //填入数据
				{
					name: '平均调价幅度',
					data: rates
				}
		]
	});
	chart_changeRate.hideLoading();    //隐藏加载动画
}
