
//自动运行
$(document).ready(function(){
	LoadSoldCount(0);
	LoadSoldPrice(0);
	
	$("#select_period").change(function(){
		var period = $("#select_period").val();
		LoadSoldCount(period);
		LoadSoldPrice(period);
	});
});

//通过ajax从服务端读取(成交套数)数据
function LoadSoldCount(period) {
	chart_soldCount.showLoading(); //显示加载动画
	var reqURL = apiURL + location.search; //加上url里的get请求参数
	$.ajax({url: reqURL,				
		   type: "post",        //post请求方式
		  async: true,          //异步请求（同步请求将会锁住浏览器，用户其他操作必须等待请求完成才可以执行）
		   data: {Method: "SoldCount",
			      PeriodType: period},   //请求参数
	   dataType: "json",        //返回数据形式为json
	    success: function (data){ ShowSoldCount(data); }
		});
}

//通过ajax从服务端读取(成交均价)数据
function LoadSoldPrice(period) {
	chart_soldPrice.showLoading(); 
	var reqURL = apiURL + location.search; //加上url里的get请求参数
	$.ajax({url: reqURL,				
		   type: "post",        //post请求方式
		  async: true,          //异步请求（同步请求将会锁住浏览器，用户其他操作必须等待请求完成才可以执行）
		   data: {Method: "SoldPrice",
			      PeriodType: period},   //请求参数
	   dataType: "json",        //返回数据形式为json
	    success: function (data){ ShowSoldPrice(data); }
		});
}


function ShowSoldCount(resp) {
	if (resp.ReturnCode != "000000")
	{
		return false;
	}
	var counts=[];       //成交价格数组
	var dates=[];        //时间数组
	var nCount = resp.Records.length; //记录条数 
	for(var i=nCount-1; i>=0; i--)    //挨个取出数组值 
	{  
		counts.push(resp.Records[i].Count);        
		dates.push(resp.Records[i].Date);
	}
	
	chart_soldCount.setOption({        //载入数据
		title: {
			text: '成交套数'
		},
		xAxis: {
			data: dates    //填入X轴数据
		},
		series: [    //填入数据
				{
					name: '成交套数',
					data: counts
				}
		]
	});
	chart_soldCount.hideLoading();    //隐藏加载动画
}

function ShowSoldPrice(resp) {
	if (resp.ReturnCode != "000000")
	{
		return false;
	}
	var prices=[];       //成交价格数组
	var dates=[];        //时间数组
	var nCount = resp.Records.length; //记录条数 
	for(var i=nCount-1; i>=0; i--)    //挨个取出数组值 
	{  
		prices.push(resp.Records[i].Price);        
		dates.push(resp.Records[i].Date);
	}
	chart_soldPrice.setOption({        //载入数据
		title: {
			text: '成交均价'
		},
		xAxis: {
			data: dates    //填入X轴数据
		},
		series: [    //填入数据
				{
					name: '成交均价',
					data: prices
				}
		]
	});
	chart_soldPrice.hideLoading();    //隐藏加载动画
}