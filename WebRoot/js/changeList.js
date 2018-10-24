var filterQu = "";
var gPageSize = 10;  //默认每页记录条数

//自动运行
$(document).ready(function(){	
	InitNavigator();
	InitDialog();
	LoadArea();
	LoadChangeList(1);
});


//解析get参数
function GetRequest() {
	var url = location.search; //获取url中含"?"符后的字串
	var theRequest = new Object();
	if (url.indexOf("?") != -1) {
		var str = url.substr(1);
		strs = str.split("&");
		for(var i = 0; i < strs.length; i ++) {
			theRequest[strs[i].split("=")[0]]=unescape(strs[i].split("=")[1]);
		}
	}
	return theRequest;
}

//通过ajax从服务端读取城市下的区列表信息 （city参数在url 参数列表里）
function LoadArea() {
	var reqURL = apiURL + location.search; //加上url里的get请求参数 
	$.ajax({url: reqURL,				
		   type: "post",        //post请求方式
		  async: true,          //异步请求（同步请求将会锁住浏览器，用户其他操作必须等待请求完成才可以执行）
		   data: {Method: "QuInCity"},   //请求参数
	   dataType: "json",        //返回数据形式为json
        success: function (data){ FillSelectArea(data); }		
		});
}

//通过ajax从服务端读取调价详细记录
function LoadChangeList(currPageNo) {
	var reqURL = apiURL + location.search; //加上url里的get请求参数 
	$.ajax({url: reqURL,				
		   type: "post",        //post请求方式
		  async: true,          //异步请求（同步请求将会锁住浏览器，用户其他操作必须等待请求完成才可以执行）
		   data: {Method: "ChangeList",
				PageSize: gPageSize,
				  PageNo: currPageNo,
				      Qu: filterQu},   //请求参数
	   dataType: "json",        //返回数据形式为json
        success: function (data){ ShowChangeList(currPageNo, data); }		
		});
}


//通过ajax从服务端读取某套房子的所有调价历史记录
function LoadChangeHistory(houseId, hintInfo) {
	var reqURL = apiURL + location.search; //加上url里的get请求参数
	//var tmpId = "101102481757";
	$.ajax({url: reqURL,				
		   type: "post",        //post请求方式
		  async: true,          //异步请求（同步请求将会锁住浏览器，用户其他操作必须等待请求完成才可以执行）
		   data: {Method: "ChangeHistory",
				 HouseId: houseId},   //请求参数
	   dataType: "json",        //返回数据形式为json
		success: function (data) { ShowChangeHistory(data, hintInfo); }	
		});
}

//显示调价详细记录列表
function ShowChangeList(currPageNo, resp) {
	if (resp.ReturnCode != "000000")
	{
		return false;
	}
	$("#changeList").empty();
	var nCount = resp.Records.length; //记录条数 
	for(var i=0; i<nCount; i++)    //挨个取出数组值 
	{  
		var rec = resp.Records[i];
		//1. 计算调价幅度信息
		var rate = 0;
		if (rec.oldPrice != 0)
		{
			rate = Math.round((rec.newPrice-rec.oldPrice)*10000/rec.oldPrice) / 100;
		}
		var strRate = String(rate) + "%";
		if(rate > 0)
			strRate = "<font color='red'>" + strRate + "</font>";
		else if (rate < 0)
			strRate = "<font color='green'>" + strRate + "</font>";
		var htmlRate = "<td bgcolor='#f3f3f3'>"+ strRate +"</td>";
		
		//2. 生成房屋链接地址
		var Request = new Object();
		Request = GetRequest();
		var city = Request['City'];
		if (city == null) //默认为北京
			city = 'bj';
		var strTmp = "https://" + city + ".lianjia.com/ershoufang/"+ rec.houseId + ".html";
		var htmlXiaoqu = "<a href='"+ strTmp + "' target='_blank'>" + rec.xiaoqu + "</a>";
		
		//3. 生成‘调价历史’的查看按钮
		var htmlHistory = "";
		if (rec.changeTimes > 1)
			htmlHistory = "<button class='btnHistory' name='" + rec.houseId + "'>" + rec.changeTimes + "次</button>";
		
		//动态生一条记录的html内容。
		var tr = $("<tr></tr>");
		tr.html("<td bgcolor='#f3f3f3'>" + rec.createTime +"</td>" 
				+ "<td bgcolor='#ffffff'>"+ rec.quyu1 +"</td>"
				+ "<td bgcolor='#f3f3f3'>"+ rec.quyu2 +"</td>"
				+ "<td bgcolor='#ffffff'>"+ htmlXiaoqu +"</td>"
				+ "<td bgcolor='#f3f3f3'>"+ rec.oldPrice +"</td>"
				+ "<td bgcolor='#ffffff'>"+ rec.newPrice +"</td>"
				+ htmlRate
				+ "<td bgcolor='#ffffff'>"+ rec.jianzhumianji +"</td>"
				+ "<td bgcolor='#f3f3f3'>"+ rec.fangwuhuxing +"</td>"
				+ "<td bgcolor='#ffffff'>"+ rec.oldDate +"</td>"
				+ "<td bgcolor='#f3f3f3'>"+ htmlHistory +"</td>");
		$("#changeList").append(tr);
	} //for

	$("#container_pagination").pagination('setPage', currPageNo, resp.TotalPage);
	$(".btnHistory").on( "click", function() { //需要重新设置弹出窗口
		var houseId = $(this).attr("name");
		var tr = $(this).parent().parent().find("td");  
		var hintInfo = tr.eq(1).text() + "-" + tr.eq(2).text() + "-" + tr.eq(3).text();
		LoadChangeHistory(houseId, hintInfo);
	});
}

//显示某套房子的调价历史
function ShowChangeHistory(resp, hintInfo) {
	if (resp.ReturnCode != "000000")
	{
		return false;
	}	
	$("#changeHistoryList").empty();
	var nCount = resp.Records.length; //记录条数 
	for(var i=0; i<nCount; i++)    //挨个取出数组值 
	{  
		var rec = resp.Records[i];
		//1. 计算调价幅度信息
		var rate = 0;
		if (rec.oldPrice != 0)
		{
			rate = Math.round((rec.newPrice-rec.oldPrice)*10000/rec.oldPrice) / 100;
		}
		var strRate = String(rate) + "%";
		
		if(rate > 0)
			strRate = "<font color='red'>" + strRate + "</font>";
		else if (rate < 0)
			strRate = "<font color='green'>" + strRate + "</font>";
		
		
		var htmlRate = "<td bgcolor='#ffffff'>"+ strRate +"</td>";
		
		//动态生一条记录的html内容。
		var tr = $("<tr></tr>");
		tr.html("<td bgcolor='#f3f3f3'>" + rec.createTime +"</td>" 
				+ "<td bgcolor='#ffffff'>"+ rec.oldPrice +"</td>"
				+ "<td bgcolor='#f3f3f3'>"+ rec.newPrice +"</td>"
				+ htmlRate
				);
		$("#changeHistoryList").append(tr);
	}// for	

	$("#historyDialog").dialog({
		title: hintInfo
	});
	$("#historyDialog").dialog("open");
}

//填充'区域'选择下拉框的内容
function FillSelectArea(resp)
{
	$("#select_area").empty();
	var strHtml = "<option value='' selected='selected'>所有</option>";
	var nCount = resp.Records.length; //记录条数 
	for(var i=0; i<nCount; i++)    //挨个取出数组值 
	{  
		var rec = resp.Records[i];
		strHtml = strHtml + "<option value='" + rec.qu + "'>" + rec.qu + "</option>";
	}
	$("#select_area").append(strHtml);
	
	$("#select_area").on("change", function() { //重新onChange事件
		filterQu = $("#select_area").val();
		LoadChangeList(1);
	});
	
}

//初始化分页控件的样式
function InitNavigator() {
	$("#container_pagination").pagination({
		pageSizeOpt: [
		    {'value': 10, 'text': '10条/页', 'selected': true},
            {'value': 20, 'text': '20条/页'},
            {'value': 50, 'text': '50条/页'},
            {'value': 100, 'text': '100条/页'}
        ],
        //css: 'css-2',
        totalPage: 1,
        showPageNum: 10,
        firstPage: '首页',
        previousPage: '上一页',
        nextPage: '下一页',
        lastPage: '尾页',
        skip: '跳至',
        confirm: '确认',
        refresh: '刷新',
        totalPageText: '共{}页',
        isShowFL: true,
        isShowPageSizeOpt: true,
        isShowSkip: false,
        isShowRefresh: false,
        isShowTotalPage: true,
        isResetPage: false,
        callBack: function (currPage, pageSize) {
        	gPageSize = pageSize;
        	LoadChangeList(currPage);
        }
    });
}

//初始化弹出窗体的样式
function InitDialog() {
	$( "#historyDialog" ).dialog({
		autoOpen: false,
		width : 400,
		show: {
			effect: "blind",
			duration: 800  //毫秒
		},
		hide: {
			effect: "explode",
			duration: 800
		}
	});
}

