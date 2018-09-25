
function objKeySort(arys) {  
                var newkey = Object.keys(arys).sort(); 
                var newObj = {};  
                for(var i = 0; i < newkey.length; i++) { 
                    newObj[newkey[i]] = arys[newkey[i]];   
                }
                return newObj;  
            } 
function getInstances(){ 
	$.ajax({
		url : global_service_url + "server/getInstance",
		data : {  
		},
		type : 'GET',
		dataType : "json",
		contentType : 'application/x-www-form-urlencoded; charset=UTF-8',
		success : function(data) {
			var tmp = eval(data);
			var datas = tmp[0].data; 
			datas = objKeySort(datas);
			 for(var row in datas){   
				  var instances = '<div class="row"><h3>'+row+'</h3>';
				 row = objKeySort(datas[row]); 
				 for(var r in row){ 
					instances += writeRow(r,row[r]);
				 } 
				 $("#page-wrapper .tables").append(instances+'</div>');
			 }
			 $(".loadinginfo").hide(); 
		},
		error : function(data) { 
			alert("连接失败"); 
		}
	});
}


function Map() {  
	var struct = function(key, value) {  
		this.key = key;  
		this.value = value;  
	};  
   
	var set = function(key, value){  
		for (var i = 0; i < this.arr.length; i++) {  
			if ( this.arr[i].key === key ) {  
				this.arr[i].value = value;  
				return;  
			}  
		}  
		this.arr[this.arr.length] = new struct(key, value);  
	};  
   
	var get = function(key) {  
		for (var i = 0; i < this.arr.length; i++) {  
			if ( this.arr[i].key === key ) {  
				return this.arr[i].value;  
			}  
		}  
		return null;  
	};  
   
	var remove = function(key) {  
		var v;  
		for (var i = 0; i < this.arr.length; i++) {  
			v = this.arr.pop();  
			if ( v.key === key ) {  
				continue;  
			}  
			this.arr.unshift(v);  
		}  
	};  
   
	var size = function() {  
		return this.arr.length;  
	};  
   
	var isEmpty = function() {  
		return this.arr.length <= 0;  
	};   

	var get_key = function(i) {  
		if ( 0 <= i < this.arr.length ) {  
			return this.arr[i].key;  
		}	 
		return null;  
	};
	
	var get_value = function(i) {  
		if ( 0 <= i < this.arr.length ) {  
			return this.arr[i].value;  
		}	 
		return null;  
	};  
	this.arr = new Array();  
	this.get = get;  
	this.set = set;  
	this.remove = remove;  
	this.size = size;  
	this.isEmpty = isEmpty;  
	this.get_key = get_key;
	this.get_value = get_value;
}  
 


function writeRow(name,data){  
	 
	var columninfo = "";
	var ips = new Map();
	var rowinfos = "";  
	var cron_content="";
	var indexips = new Map();
	var alias="";
	var reloadIPs = new Map();

	for(var r in data){
		if(r!='IndexType')
			columninfo+='<th>'+r+'</th>'; 
	 
		var rows = data[r].split(",");
		for(var j in rows){  
			if(ips.get(rows[j].split("|")[0])==null){
				ips.set(rows[j].split("|")[0],rows[j].split("|")[0]);
			} 
		}; 
	}     
	ips.set("default","")//fix bug  
	for(var i = 0;i<ips.size();i++){  
		if(ips.get_key(i)=='default' || ips.get_key(i)=='')
			continue;
		var css="";
		var indextype="0";
		var rowcontent="";
		rowcontent+="<td scope=\"row\">"; 
		rowcontent+='<a data-ip="'+ips.get_key(i)+'"  data-id="'+name+'" class="view_info " href="" data-toggle="modal" data-target="#gridSystemModal" title="view run state">';
		rowcontent+= ips.get_key(i)+"</a></td>"; 
		for(var r in data){    
			var rows = data[r].split(",");  
			var is_set = false; 
			for(var j in rows){ 
				if(rows[j].split("|")[0]==ips.get_key(i)){
					if(r=='IndexType'){
						indextype = rows[j].split("|")[1];
					}else{
						rowcontent+="<td>"+(rows[j].split("|")[1]=='null'?'not set!':rows[j].split("|")[1])+"</td>";
					}
					if(r=='Alias'){
						alias = rows[j].split("|")[1];
					}
					is_set = true;
					if(rows[j].split("|")[1]=='true' && r=='OpenWrite'){
						css+=' info'; 
						indexips.set(ips.get_key(i),ips.get_key(i));
					}
					if(rows[j].split("|")[1]=='true' && r=='IsMaster'){
						css+=' master'; 
					}
					reloadIPs.set(ips.get_key(i),ips.get_key(i));
				}
			}
			if(is_set==false){
				rowcontent+="<td>not set!</td>";
			}
		}   
		rowinfos+='<tr class="'+css+'" data-id='+indextype+'>'+rowcontent+'</tr>';
	}
   //get indexer info
	for(var r in data){ 
			var rows = data[r].split(","); 
			for(var j in rows){ 
				if(indexips.get(rows[j].split("|")[0])){ 
					if(rows[j].split("|")[1]!='null' && r=='IndexType'){
						indexips.set(rows[j].split("|")[0],rows[j].split("|")[1]);
					}  
					if(rows[j].split("|")[1]!='null' && r=='FullCron'){
						cron_content+=",FullCron||"+rows[j].split("|")[1]+"||"+rows[j].split("|")[0];
					}
					if(rows[j].split("|")[1]!='null' && r=='DeltaCron'){
						cron_content+=",DeltaCron||"+rows[j].split("|")[1]+"||"+rows[j].split("|")[0];
					}
				}
				if(reloadIPs.get(rows[j].split("|")[0])){
					if(rows[j].split("|")[1]!='null' && r=='IndexType'){
						reloadIPs.set(rows[j].split("|")[0],rows[j].split("|")[1]);
					} 
				}
			} 
	} 

	var index_ip_info="";
	indexips.set("default","")//fix bug  
	for(var i = 0;i<indexips.size();i++){   
		if(indexips.get_key(i)!='default')
		index_ip_info+=","+indexips.get_key(i)+"||"+indexips.get_value(i);
	}
	
	var reloadinfos="";
	reloadIPs.set("default","")//fix bug 
	for(var i = 0;i<reloadIPs.size();i++){   
		if(reloadIPs.get_key(i)!='default')
			reloadinfos+=","+reloadIPs.get_key(i)+"||"+reloadIPs.get_value(i);
	}
	var str = '<div alias="'+alias+'" class="bs-example widget-shadow" data-example-id="hoverable-table">';
	str +='<h4>'+name+(' <a style="float:right;margin:0 3px;" reload-ip="'+reloadinfos+'" data-id="'+name+'" data-ip="'+index_ip_info+'" data-info="'+cron_content+'" class="manage_index btn btn-success" data-toggle="modal" data-target="#gridSystemModal"> <i class="fa fa-dashboard"></i> <span class="waitloading">Manage Writer</span></a>');
	str +='<a title="edit cloud xml config file" style="float:right;margin:0 3px;" data-id="'+name+'" data-ip="'+index_ip_info+'" class="edit_config btn btn-primary" data-toggle="modal" data-target="#gridSystemModal"><i class="fa fa-edit"></i> <span style=" display:none" class="waitloading ">Edit Cloud Config</span></a>';
	str +='<a title="upload river instance config files to cloud" style="float:right;margin:0 3px;" data-id="'+name+'" data-ip="'+index_ip_info+'" class="upload_config  btn btn-info" ><i class="fa fa-cloud-upload"></i> <span style=" display:none" class="waitloading ">Upload Instance Files</span></a>';
	str +='<a title="reset river instance cloud status" style="float:right;margin:0 3px;" data-id="'+name+'" data-ip="'+indexips.get_key(0)+'" class="reset_config  btn btn-warning" ><i class="fa fa-eraser"></i> <span   class="waitloading ">Reset Instance Status</span></a>';
	str +='<a title="Backup river instance cloud status and configs" style="float:right;margin:0 3px;" data-id="'+name+'" data-ip="'+indexips.get_key(0)+'" class="backup_config  btn btn-success" ><i class="fa fa-cloud-download"></i> <span   class="waitloading ">Backup Instance Configs and Status</span></a>';
	str +='</h4><table class="table table-hover"> <thead> <tr> <th>IP</th>'+columninfo+'</tr> </thead> <tbody> '+rowinfos+' </tbody> </table>';
	str +='</div>'; 
	return str;
}
$(function(){ 
	$(document).on("click",".view_info",function(){ 
		$("#savebtn").hide();
		$("#gridSystemModalLabel").html('<i class="fa fa-bar-chart-o"></i>View '+$(this).attr("data-id")+" Run State");
		get_info($(this).attr("data-id"),$(this).attr("data-ip")); 
	})
	$(document).on("click",".backup_config",function(){ 
		 var _instance = $(this).attr("data-id"); 
		 $.ajax({
				url : global_service_url + "server/InstancesAction",
				data : {  
					action:"backup_config",
					instance:_instance,
					ip:$(this).attr("data-ip")
				},
				type : 'GET',
				dataType : "json",
				contentType : 'application/x-www-form-urlencoded; charset=UTF-8',
				success : function(data) {
					alert("success!"); 
				},
				error : function(data) { 
					alert("连接失败!"); 
				}
			});
	})
	$(document).on("click",".reset_config",function(){ 
		 var _instance = $(this).attr("data-id"); 
		 $.ajax({
				url : global_service_url + "server/InstancesAction",
				data : {  
					action:"resetInstanceState",
					instance:_instance,
					ip:$(this).attr("data-ip")
				},
				type : 'GET',
				dataType : "json",
				contentType : 'application/x-www-form-urlencoded; charset=UTF-8',
				success : function(data) {
					alert("success!"); 
				},
				error : function(data) { 
					alert("连接失败!"); 
				}
			});
	})
	$(document).on("click",".upload_config",function(){ 
		 var _instance = $(this).attr("data-id"); 
		 $.ajax({
				url : global_service_url + "server/reloadInstanceConfig",
				data : {  
					instance:_instance
				},
				type : 'POST',
				dataType : "json",
				contentType : 'application/x-www-form-urlencoded; charset=UTF-8',
				success : function(data) {
					alert("success!"); 
				},
				error : function(data) { 
					alert("连接失败!"); 
				}
			});
	})

	$(document).on("click",".manage_index",function(){  
		$("#savebtn").hide();
		$("#gridSystemModalLabel").text("Manage Index "+$(this).attr("data-id"));
		var content="";
		var crons = $(this).attr("data-info").split(",");
		for(var i=0;i<crons.length;i++){
			if(crons[i]=='')
				continue;
			var rows = crons[i].split("||");
			content+='<div class="row" >';
			var type="";
			for(var j=0;j<rows.length;j++){
				if(rows[j]=='FullCron'){
					type = "full";
				}
				if(rows[j]=='DeltaCron'){
					type = "increment";
				}
				content+='<div class="col-md-3 grid_box1">'+rows[j]+'</div>';
			}
			content+='<div class="col-md-3 grid_box1"><button type="button" onclick="run_job(\''+$(this).attr("data-id")+'\',\''+$(this).attr("data-ip")+'\',\''+type+'\')" class="btn btn-sm btn-success"><i class="fa fa-caret-right"></i> RUN NOW</button></div></div>';
		}  
		content+='<div class="row">';
		if($(this).attr("data-ip")!=null){
			content+=' <button type="button" onclick="stop_job(\''+$(this).attr("data-id")+'\',\''+$(this).attr("data-ip")+'\',\'Increment\')" class="btn btn-danger btn-sm stop"><i class="fa fa-stop"></i> Stop Increment</button>  <button type="button" onclick="resume_job(\''+$(this).attr("data-id")+'\',\''+$(this).attr("data-ip")+'\',\'Increment\')" class="btn btn-primary  btn-sm resume"><i class="fa fa-refresh"></i> Resume Increment</button>';
			content+=' <button type="button" onclick="stop_job(\''+$(this).attr("data-id")+'\',\''+$(this).attr("data-ip")+'\',\'FULL\')" class="btn btn-danger btn-sm stop"><i class="fa fa-stop"></i> Stop Full</button>  <button type="button" onclick="resume_job(\''+$(this).attr("data-id")+'\',\''+$(this).attr("data-ip")+'\',\'FULL\')" class="btn btn-primary  btn-sm resume"><i class="fa fa-refresh"></i> Resume Full</button>';
		} 
		content+=' <button type="button" onclick="Reload_Config(\''+$(this).attr("data-id")+'\',\''+$(this).attr("reload-ip")+'\')" class="btn btn-info btn-sm Reload"><i class="fa fa fa-repeat"></i> Reload'+($(this).attr("data-id")!=null?'':' ALL')+' Config</button>';
		content+='</div>';
		$("#gridSystemModal .modal-body").attr("contentEditable",'false');
		$("#gridSystemModal .modal-body").html(content);
	}) 
	
	$(document).on("click",".edit_config",function(){  
		$("#savebtn").show();
		$("#gridSystemModalLabel").text("Edit Config "+$(this).attr("data-id"));  
		$("#hiddenval").text($(this).attr("data-id"));
		getInstancesConfig($(this).attr("data-id")); 
	})
	
	$(document).on("click","#savebtn",function(){   
		 var content = $("#gridSystemModal .modal-body textarea").val(); 
		 $.ajax({
				url : global_service_url + "server/setInstanceConfig",
				data : {  
					instance:$("#hiddenval").text(),
					data:content
				},
				type : 'POST',
				dataType : "json",
				contentType : 'application/x-www-form-urlencoded; charset=UTF-8',
				success : function(data) {
					alert("success"); 
				},
				error : function(data) { 
					alert("连接失败"); 
				}
			});
	})
})


function getInstancesConfig(val){
	$.ajax({
		url : global_service_url + "server/getInstanceConfig",
		data : {  
			instance:val
		},
		type : 'GET',
		dataType : "json",
		contentType : 'application/x-www-form-urlencoded; charset=UTF-8',
		success : function(data) {
			var tmp = eval(data); 
			var datas = "<textarea style='width:100%;height:600px;'>"+tmp[0].data+"</textarea>"; 
			
			$("#gridSystemModal .modal-body").html(datas);  
		},
		error : function(data) { 
			alert("连接失败"); 
		}
	});
}

function get_info(indexs,ips){
	var ips = ips.split(",");
	var is_set =false;
	$("#gridSystemModal .modal-body").attr("contentEditable",'false');
	$("#gridSystemModal .modal-body").html("loading...");
	for(var i=0;i<ips.length;i++){
		if(ips[i]!=''){
			$.ajax({
		url : global_service_url + "server/get_info",
		data : {  
			ip:ips[i].split("||")[0], 
			instance:indexs
		},
		type : 'GET',
		dataType : "json",
		contentType : 'application/x-www-form-urlencoded; charset=UTF-8',
		success : function(data) { 
			is_set = true;
			var tmps = eval(data); 
			var content=""; 
			tmps = tmps.info.split("[");
			for(var i=0;i<tmps.length;i++){
				if(tmps[i].length>1){
					var row = tmps[i].split("]");
					content+='<div class="row" >';  
					content+= '<h4 class="hdg">'+row[0]+'</h4>';
					if(row[0].indexOf('状态')>0){ 
						var columns = row[1].split(";");
					}else{  
						var columns = row[1].split(",");
					}
					
					for(var j=0;j<columns.length;j++){
						if( columns[j].split(':').length>1){   
							if(row[0].indexOf('状态')>0){  
								if(columns[j].search('full:null')!= -1 || columns[j].search('increment:null')!= -1){ 
									content+= '<div class="col-md-10 grid_box1 gray"> 未启动或已执行完毕！ </div>'; 
								}else{
									if(columns[j].search(":,") != -1){
										content+= '<div class="col-md-10 grid_box1"> <span class="light-blue"><i class="fa fa-minus "></i> seq'+columns[j].replace(':,','</span><br>')+'</div>'; 
									}else{
										content+= '<div class="col-md-10 grid_box1"> <span class="light-blue"><i class="fa fa-minus "></i> '+columns[j].replace(':','</span><br>')+'</div>'; 
									}
									
								}
								
							}else{
								content+= '<div class="col-md-4 grid_box1"><span class="light-green">'+columns[j].split(':')[1]+'</span>'+columns[j].split(':')[0]+'</div>';
				
							} 
						}else{
							if(row[0].search('增量状态')!= -1 || row[0].search('增量状态')!= -1){
								content+= '<div class="col-md-10 grid_box1 gray"> 未启动或已执行完毕！ </div>';
							}else{
								content+= '<div class="col-md-10 grid_box1">'+columns[j] +'</div>';
							} 
						}
							
					}
					content+= '</div>';
				} 
			}
			$("#gridSystemModal .modal-body").attr("contentEditable",'false');
			if(content.length<2){
				$("#gridSystemModal .modal-body").html("no data!");
			}else{
				$("#gridSystemModal .modal-body").html(content);
			} 
		},
		error : function(data) { 
			alert("连接失败"); 
		}
	});
		}
	} 
	if(!is_set)
		$("#gridSystemModal .modal-body").html("no data!");
}


function stop_job(indexs,ips,isIncrement){
	var ips = ips.split(",");
	for(var i=0;i<ips.length;i++){
		if(ips[i]!=''){
			$.ajax({
		url : global_service_url + "server/stop_job",
		data : {  
			ip:ips[i].split("||")[0],
			instancetype:ips[i].split("||")[1],
			instance:indexs,
			type:isIncrement
		},
		type : 'GET',
		dataType : "json",
		contentType : 'application/x-www-form-urlencoded; charset=UTF-8',
		success : function(data) { 
			var tmps = eval(data);
			alert(tmps.info); 
		},
		error : function(data) { 
			alert("连接失败"); 
		}
	});
		}
	} 
}


function resume_job(indexs,ips,isIncrement){
	var ips = ips.split(",");
	for(var i=0;i<ips.length;i++){
		if(ips[i]!=''){
			$.ajax({
		url : global_service_url + "server/resume_job",
		data : {  
			ip:ips[i].split("||")[0],
			instancetype:ips[i].split("||")[1],
			instance:indexs,
			type:isIncrement
		},
		type : 'GET',
		dataType : "json",
		contentType : 'application/x-www-form-urlencoded; charset=UTF-8',
		success : function(data) { 
			var tmps = eval(data);
			alert(tmps.info); 
		},
		error : function(data) { 
			alert("连接失败"); 
		}
	});
		}
	} 
}


function Reload_Config(indexs,ips){
	var ips = ips.split(",");
	for(var i=0;i<ips.length;i++){
		if(ips[i]!=''){
			$.ajax({
		url : global_service_url + "server/reload_config",
		data : {  
			ip:ips[i].split("||")[0],
			instancetype:ips[i].split("||")[1],
			instance:indexs
		},
		type : 'GET',
		dataType : "json",
		contentType : 'application/x-www-form-urlencoded; charset=UTF-8',
		success : function(data) { 
			var tmps = eval(data);
			alert(tmps.info); 
		},
		error : function(data) { 
			alert("连接失败"); 
		}
	});
		}
	} 
}

function run_job(indexs,ips,type){
	var ips = ips.split(",");
	for(var i=0;i<ips.length;i++){
		if(ips[i]!=''){
			$.ajax({
		url : global_service_url + "server/run_job",
		data : {  
			ip:ips[i].split("||")[0],
			indextype:ips[i].split("||")[1],
			jobtype:type,
			instance:indexs
		},
		type : 'GET',
		dataType : "json",
		contentType : 'application/x-www-form-urlencoded; charset=UTF-8',
		success : function(data) { 
			var tmp = eval(data);
			alert(tmp.info); 
		},
		error : function(data) { 
			alert("连接失败"); 
		}
	});
		}
	} 
}