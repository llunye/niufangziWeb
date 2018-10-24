//自动运行
$(document).ready(function(){
	InitHeader();
	InitFooter();
	InitSelect();
});

function OpenWindow(url) {
	var city = $("#select_city").val();
	var str = url + '?City=' + city;
	window.open(str,'_self');
	return false;
}

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

function InitSelect() {
	var Request = new Object();
	Request = GetRequest();
	var city = Request['City'];
	if (city == null) //默认为北京
		city = 'bj';
	
	$("#select_city").val(city);
	/*
	$("#select_city").change(function(){
		var city = $("#select_city").val();
  		console.log("changed city is :" + city);
	});
	*/
}

function InitHeader() {
	$("#container_header").empty();
	$("#container_header").attr("class", "banner");
	var strHtml = "<div class='container'>"
		        + "<ul class='channelList'>"
		        + "<li><a href='#' onclick=OpenWindow('index.html')>首页</a></li>"
		        + "<li><a href='#' onclick=OpenWindow('index.html')>成交趋势</a></li>"
		        + "<li><a href='#' onclick=OpenWindow('selling.html')>在售趋势</a></li>"
		        + "<li><a href='#' onclick=OpenWindow('change.html')>调价趋势</a></li>"
		        + "<li><a href='#' onclick=OpenWindow('soldList.html')>成交列表</a></li>"
		        + "<li><a href='#' onclick=OpenWindow('sellingList.html')>在售列表</a></li>"
		        + "<li><a href='#' onclick=OpenWindow('changeList.html')>调价列表</a></li>"
		        + "<li>"
		        + "<select name='city' id='select_city' style='height:30px;width:70px;margin-left:200px'>"
		        + "<option value='bj' selected='selected'>北京</option>"
		        + "<option value='lf'>廊坊</option>"
		        + "<option value='wh'>武汉</option>"
		        + "</select>"
		        + "</li>"
		        + "</ul>"
		        + "</div>";
	$("#container_header").append(strHtml); 
}

function InitFooter() {
	$("#container_footer").empty();
	//$("#container_footer").attr("class", "banner");
	var strHtml = "<span style='margin-left:400px'>牛房子科技 | 网络经营许可证 京ICP备18010039号</span>";
	$("#container_footer").append(strHtml); 
}


function InitHeaderOld() {
	//<div id="container_header" style="height:40px;width:90%;padding:.5em 0;"></div>
	$("#container_header").empty();
	var str = "<a style='margin-left:10px' target='_parent' href='#' onclick=OpenWindow('index.html')>成交趋势 </a>"
			+ "<a style='margin-left:30px' target='_parent' href='#' onclick=OpenWindow('selling.html')>在售趋势 </a>"
			+ "<a style='margin-left:30px' target='_parent' href='#' onclick=OpenWindow('change.html')>调价趋势 </a>"
			+ "<a style='margin-left:30px' target='_parent' href='#' onclick=OpenWindow('changeDetail.html')>调价详情 </a>"
			+ "<select name='city' id='select_city' style='height:30px;width:70px;margin-left:500px'>"
			+	"<option value='bj' selected='selected'>北京</option>"
			+	"<option value='lf'>廊坊</option>"
			+	"<option value='wh'>武汉</option>"
			+ "</select>";
	$("#container_header").append(str);
}


