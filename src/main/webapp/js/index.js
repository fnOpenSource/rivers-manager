var actions = "";
function getLevel(v, ip) {
	var str = "";
	actions = "";
	if ((v & 1) > 0) {
		str += "searcher/";
		actions += '<a onclick="searcher_ac(\''
				+ ip
				+ '\',this,\'stopSearcher\')" class="stop_searcher btn">Stop Searcher</a> ';
	} else {
		actions += '<a onclick="searcher_ac(\''
				+ ip
				+ '\',this,\'startSearcher\')" class="start_searcher btn">Start Searcher</a> ';
	}
	if ((v & 2) > 0) {
		str += "writer/";
	}
	if ((v & 4) > 0) {
		str += "http_reader";
		actions += '<a class="btn" onclick="searcher_ac(\''
				+ ip
				+ '\',this,\'stopHttpReader\')" class="stop_HttpReader btn">Stop HttpReader</a> ';
	} else {
		actions += '<a class="btn" onclick="searcher_ac(\''
				+ ip
				+ '\',this,\'startHttpReader\')" class="start_HttpReader btn">Start HttpReader</a> ';
	}
	str += " service";
	actions += '<a  class="editconfig btn" data-ip="'
			+ ip
			+ '" data-toggle="modal" data-target="#gridSystemModal">Edit Config</a>';
	return str;
}
function getStatus() {
	$("#status_info tbody").html('<tr><td rowspan="7">loading...</td></tr>');
	$.ajax({
				url : global_service_url + "server/getStatus",
				data : {},
				type : 'GET',
				dataType : "json",
				contentType : 'application/x-www-form-urlencoded; charset=UTF-8',
				success : function(data) {
					var tmp = eval(data);
					var datas = tmp[0].data;
					$("#instances").text(datas.instances);
					$("#servers").text(datas.servers);
					$("#versions").text(datas.version);
					var datas = objKeySort(tmp[0].instance_status);
					var content = "";
					for ( var row in datas) {
						var r = eval(datas[row]);
						var cpu = parseInt(r.CPU);
						var mem = parseInt(r.MEMORY);
						content += '<tr><th scope="row">' + row + '</th>';
						content += '<td>' + getLevel(r.SERVICE_LEVEL, row)
								+ '</td>';
						content += '<td><span class="label label-success">'
								+ r.STATUS + '</span></td>';
						content += '<td>' + r.TASKS + '</td>';
						content += '<td>' + r.NODE_TYPE + '</td>';
						content += '<td style="font-size:12px"><span class="pull-right">'
								+ cpu
								+ '%</span><div class="progress progress-striped  progress-left"><div class="bar '
								+ (cpu < 70 ? 'green' : (cpu < 90 ? 'yellow'
										: 'red'))
								+ '" style="width:'
								+ cpu
								+ '%;"></div></div></td>';
						content += '<td><span class="pull-right">'
								+ mem
								+ '%</span><div class="progress progress-striped  progress-left"><div class="bar '
								+ (mem < 80 ? 'green' : (mem < 90 ? 'yellow'
										: 'red')) + '" style="width:' + mem
								+ '%;"></div></div></td>';
						content += '<td>' + actions + '</td>';
						content += '</tr>';
					}
					$("#status_info tbody").html(content);
				},
				error : function(data) {
					alert("连接失败");
				}
			});
}
getStatus();
function searcher_ac(ips, _obj, type) {
	$.ajax({
		url : global_service_url + "server/InstancesAction",
		data : {
			action : type,
			instance : '',
			ip : ips
		},
		type : 'GET',
		dataType : "json",
		contentType : 'application/x-www-form-urlencoded; charset=UTF-8',
		success : function(data) {
			if (data == "100") {
				$(_obj).remove();
			} else {
				alert("处理失败！");
			}
		},
		error : function(data) {
			alert("连接失败");
		}
	});
}

function objKeySort(arys) {
	var newkey = Object.keys(arys).sort();
	var newObj = {};
	for (var i = 0; i < newkey.length; i++) {
		newObj[newkey[i]] = arys[newkey[i]];
	}
	return newObj;
} 
$(function() {
	var configData;
	var dataip;
	$(document).on("click",".editconfig",function() {
						$("#gridSystemModalLabel").html(
								'<i class="fa fa-bar-chart-o"></i>View '
										+ $(this).attr("data-ip") + " Configs");
						dataip = $(this).attr("data-ip");
						$.ajax({
									url : global_service_url
											+ "server/InstancesAction",
									data : {
										action : "getNodeConfig",
										instance : '',
										ip : $(this).attr("data-ip")
									},
									type : 'GET',
									dataType : "json",
									contentType : 'application/x-www-form-urlencoded; charset=UTF-8',
									success : function(data) {
										var tmp = eval(data);
										tmp = $.parseJSON(tmp[0].data);
										tmp = tmp.info;
										configData = objKeySort(tmp);
										var content="";
										for ( var o in configData) {
											content+='<div class="form-group">'
												content+='<div >'
												content+='<div class="grid_box1">'
												content+='<label  >'+o+'</label>'
												content+='</div>'
												content+='<div class="col-md-12">'
												content+='<input data-k="'+o+'" class="form-control1 node_configs" value="'+configData[o]+'" type="text">'
												content+='</div>' 
												content+='</div>'
												content+='</div>';
											}
										$("#gridSystemModal .modal-body").html('<div class="grids widget-shadow">'+content+'<div class="clearfix"></div></div>');
									},
									error : function(data) {
										alert("连接失败!");
									}
								});
					});
	$(document).on("click","#savebtn",function() { 
		$(".node_configs").each(function(){
			saveConfig("set",$(this).attr("data-k"),$(this).val(),dataip);
		})
		alert("success!");
	});
});
function saveConfig(_type,_k,_v,_ip){
	$.ajax({
		url : global_service_url
				+ "server/saveNodeConfig",
		data : {
			type : _type,
			k : _k,
			v : _v,
			ip : _ip
		},
		type : 'GET',
		dataType : "json",
		contentType : 'application/x-www-form-urlencoded; charset=UTF-8',
		success : function(data) {
			var tmp = eval(data);
			tmp = $.parseJSON(tmp[0].data);
			tmp = tmp.info;
			configData = objKeySort(tmp);
			var content="";
			for ( var o in configData) { }
			$("#gridSystemModal .modal-body").html('<div class="grids widget-shadow">'+content+"</div>");
		},
		error : function(data) {
			alert("连接失败!");
		}
	});
}