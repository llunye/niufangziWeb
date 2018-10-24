
var FilterStr = "";
var OrderField = 0; //排序的字段索引
var OrderType = 1; //排序方式 (0:升序； 1：降序)

//自动运行
$(document).ready(function(){	
	LoadArea();
	InitNavigator();
	LoadSellingList(1);
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

function Sleep(numberMillis) { 
	var now = new Date(); 
	var exitTime = now.getTime() + numberMillis; 
	while (true) { 
		now = new Date(); 
		if (now.getTime() > exitTime) 
			return; 
	} 
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

//通过ajax从服务端读取在售房屋记录列表
function LoadSellingList(currPageNo) {
	var reqURL = apiURL + location.search; //加上url里的get请求参数 
	$.ajax({url: reqURL,				
		   type: "post",        //post请求方式
		  async: true,          //异步请求（同步请求将会锁住浏览器，用户其他操作必须等待请求完成才可以执行）
		   data: {Method: "SellingList",
			   FilterStr: FilterStr,
			  OrderField: OrderField,
			   OrderType: OrderType,
				  PageNo: currPageNo},   //请求参数
	   dataType: "json",        //返回数据形式为json
        success: function (data){ ShowSellingList(currPageNo, data); }		
		});
}

//通过ajax从服务端读取户型图的内容
function LoadHuxingPhoto(houseId) {
	var reqURL = apiURL + location.search; //加上url里的get请求参数 
	$.ajax({url: reqURL,				
		   type: "post",        //post请求方式
		  async: true,          //异步请求（同步请求将会锁住浏览器，用户其他操作必须等待请求完成才可以执行）
		   data: {Method: "HuxingPhotoContent",
				 HouseId: houseId,
			   HouseType: 0},   //请求参数
	   dataType: "json",        //返回数据形式为json
        success: function (data){ 
        	if (data.ReturnCode == "000000") {
        		$("#img_huxing_" + houseId).attr("src", data.PhotoContent);
        	}
          }
		});
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
}

//点击查询按钮
function FilterHouse()
{
	var jsonFilter = {};
	if ($("#select_area").val() != "")
		jsonFilter["area"] = $("#select_area").val();
	 
	if ($("#trans_price_min").val() != "")
		jsonFilter["priceMin"] = $("#trans_price_min").val();
		
	if ($("#trans_price_max").val() != "")
		jsonFilter["priceMax"] = $("#trans_price_max").val();
	
	if ($("#trans_square_min").val() != "")
		jsonFilter["squareMin"] = $("#trans_square_min").val();
	
	if ($("#trans_square_max").val() != "")
		jsonFilter["squareMax"] = $("#trans_square_max").val();
	
	if ($("#select_room").val() != "")
		jsonFilter["room"] = $("#select_room").val();
	
	FilterStr = JSON.stringify(jsonFilter);
	LoadSellingList(1);
}

function SetOrderField(nField) {
	if (nField>=0 && nField<=4)
	{
		if (OrderField == nField) {  //点击的是当前字段，则改变排序方式
			OrderType = (OrderType==0) ? 1:0; 
		}
		else {  //点击的是不同字段
			$("#ulOrderField li").attr("class", "");
			$("#ulOrderField li").eq(nField).attr("class", "selected");
			OrderField = nField;
			if (nField==2) //按总价格排序时，默认为升序，其他默认为降序
				OrderType = 0;
			else
				OrderType = 1;
		}
		//console.log(OrderField);
	}
}


//显示在售房屋记录列表
function ShowSellingList(currPageNo, resp) {
	if (resp.ReturnCode != "000000")
	{
		return false;
	}
	$("#sellingList").empty();
	var nCount = resp.Records.length; //记录条数 
	for(var i=0; i<nCount; i++)    //挨个取出数组值 
	{  
		var rec = resp.Records[i];
		
		//1. 生成房屋链接地址
		var Request = new Object();
		Request = GetRequest();
		var city = Request['City'];
		if (city == null) //默认为北京
			city = 'bj';
		var houseURL = "https://" + city + ".lianjia.com/ershoufang/"+ rec.houseId + ".html";
		//2. 计算发布间隔天数
	    var separator = "-"; //日期分隔符    
	    var startDates = rec.guapaishijian.split(separator);  
	    var startDate = new Date(startDates[0], startDates[1]-1, startDates[2]);  
	    var endDate = new Date();  
	    var dayDiff = parseInt(Math.abs(endDate - startDate ) / 1000 / 60 / 60 /24);//把相差的毫秒数转换为天
		//3. 计算均价
		var avgPrice = "--";
		if (rec.jianzhumianji > 0)
			avgPrice = Math.round((rec.guapaijiage*10000)/rec.jianzhumianji);

	    var strHtml = "<li class='clear'>"
	    			+ "<a class='img ' href='" + houseURL +"' target='_blank' >"
	    			//+ "<img class='lj-lazy' src='" + rec.photoContent + "'></a>"
	    			
	    			+ "<img id='img_huxing_" + rec.houseId + "' class='lj-lazy' src=''></a>"
	    			
	    			+ "<div class='info clear'>"
	    			+ "<div class='title'>"
	    			+ "<a class='' href='" + houseURL + "' target='_blank'>" + rec.xiaoqu + "--" + rec.fangwuhuxing.substring(0,4) + "--" + rec.guapaijiage + "万</a></div>"
	    			+ "<div class='address'>"
	    			+ "<div class='houseInfo'>"
	    			+ rec.xiaoqu
	    			+ "<span class='divide'>/</span>" + rec.fangwuhuxing.substring(0,4)
	    			+ "<span class='divide'>/</span>" + rec.jianzhumianji + "平米"
	    			+ "<span class='divide'>/</span>" + rec.fangwuchaoxiang
	    			+ "<span class='divide'>/</span>" + rec.zhuangxiuqingkuang +"</div></div>"
	    			+ "<div class='flood'>"
	    			+ "<div class='positionInfo'>" + rec.suozailouceng + "(共" + rec.zonglouceng + "层)" //顶层(共12层)
	    			+ "<span class='divide'>/</span>" + rec.jianchengniandai + "年建" + rec.jianzhuleixing  //2001年建板楼
	    			+ "<span class='divide'>/</span>" + rec.quyu2 + "</div>"
	    			+ "</div>"
	    			+ "<div class='followInfo'>" + rec.quyu1 
	    			+ "<span class='divide'>/</span>" + rec.guanzhu +"人关注"
	    			+ "<span class='divide'>/</span>" + rec.daikan +"次带看"
	    			+ "<div class='timeInfo'>"
	    			+ "<span class='timeIcon'></span>" + dayDiff + "天以前发布</div>"
	    			+ "<div class='tag'>"
	    			//+ "<span class='subway'>距离10号线车道沟站906米</span>"
	    			+ "<span class='taxfree'>房本" + rec.fangwunianxian + "</span>"
	    			+ "<span class='haskey'>随时看房</span></div>"
	    			+ "<div class='priceInfo'>"
	    			+ "<div class='totalPrice'>"
	    			+ "<span>" + rec.guapaijiage + "</span>万</div>"
	    			+ "<div class='unitPrice' data-price='120474'>"
	    			+ "<span>单价" + avgPrice +"元/平米</span></div>"
	    			+ "</div>"
	    			+ "</div>"
	    			//+ "<div class='listButtonContainer'>"
	    			//+ "<div class='btn-follow followBtn' data-hid='101102726501'>"
	    			//+ "<span class='follow-text'>关注</span></div>"
	    			//+ "<div class='compareBtn LOGCLICK' data-hid='101102726501' log-mod='101102726501' data-log_evtid='10230'>加入对比</div></div>"
	    			+ "</li>";
		$("#sellingList").append(strHtml);
	} //for

	$("#container_pagination").pagination('setPage', currPageNo, resp.TotalPage);
	
	
	$("#totalCount").text(resp.TotalRecord);
	
	for(var i=0; i<nCount; i++) //动态的加载户型图 
	{  
		var rec = resp.Records[i];
		LoadHuxingPhoto(rec.houseId);
	}
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
        isShowPageSizeOpt: false,
        isShowSkip: false,
        isShowRefresh: false,
        isShowTotalPage: true,
        isResetPage: false,
        callBack: function (currPage, pageSize) {
        	LoadSellingList(currPage);
        }
    });
}

