
function getHosts(){ 
	$.ajax({
		url : global_service_url + "server/getHosts",
		data : {  
		},
		type : 'GET',
		dataType : "json",
		contentType : 'application/x-www-form-urlencoded; charset=UTF-8',
		success : function(data) {
			var tmp = eval(data);
			var datas = tmp[0].data;
			tmp = datas.split(",");  
			for(var row in tmp){   
				 var _row = '<option value="'+tmp[row]+'">'+tmp[row]+'</option>';  
				 $("#hosts").append(_row); 
			} 
		},
		error : function(data) { 
			alert("连接失败"); 
		}
	});
}

 


function writeRow(name,data){  
	  
}
$(function(){ 
	var height=300;
	$(document).on("click","#sendcode",function(){   
		var codes = $(".messages-input-form textarea").val();
		if($("#hosts").val().length<10){
			alert("host not set ip!")
			return;
		}
		if(codes.length<32){
			alert("code error!")
			return;
		}
		height+=100;  
		$(".messages-input-form textarea").val(""); 
		$(".messages").append("<li><b>&gt;</b><div>"+codes+"</div></li>");
		$('.contents').scrollTop(height);	
		$.ajax({
			url : global_service_url + "server/InstancesAction",
			data : {  
				action:"runCode",
				instance:codes,
				ip:$("#hosts").val()
			},
			type : 'GET',
			dataType : "json",
			contentType : 'application/x-www-form-urlencoded; charset=UTF-8',
			success : function(data) {
				var tmp = eval(data);
				height+=20; 
				$('.contents').scrollTop(height);	
				$(".messages").append("<li><b>&gt;</b><div>"+tmp[0].data+"</div></li>");
			},
			error : function(data) { 
				alert("连接失败"); 
			}
		}); 
	})  
})

 