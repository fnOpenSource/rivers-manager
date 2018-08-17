//get site main domain 
var global_service_url='//'+window.location.host+'/';  
var _tmpurl = window.location.href.split('/');
for(var i = 3;i<_tmpurl.length-1;i++){
  global_service_url+=_tmpurl[i]+"/";
}
var CURRENT_FILE = location.href;
CURRENT_FILE = CURRENT_FILE.substr(CURRENT_FILE.lastIndexOf('/')+1);  
var CURRENT_PAGE = "";
if(CURRENT_FILE.indexOf(".html") > 0){
  CURRENT_PAGE = CURRENT_FILE.slice(0,CURRENT_FILE.indexOf(".html"));
  CURRENT_FILE = CURRENT_PAGE+".html";

} 
if(CURRENT_FILE.indexOf(".jsp") > 0){
  CURRENT_PAGE = CURRENT_FILE.slice(0,CURRENT_FILE.indexOf(".jsp"));
  CURRENT_FILE = CURRENT_PAGE+".jsp";
} 

var Envirements = [{url:"10.202.185.150",proj:"river",name:"Dev",port:'80'},{url:"product-search-indexing.beta1.fn",proj:"river",name:"Beta",port:'80'},{url:"stage-product_search_indexing.idc1.fn",proj:"river",name:"Preview",port:'80'},{url:"river.idc1.fn",proj:"",name:"Online",port:'80'}]; 
function getCurrent(){ 
	$("title").html(getCurrentEnvirement()+" Rivers,river connect all kinds of floor!");
	document.write(" "+getCurrentEnvirement()+" ");   
}
function getCurrentEnvirement(){
	var _c = "Local";
	for(var i=0;i<Envirements.length;i++){ 
       if(document.domain==Envirements[i].url){
          _c = Envirements[i].name;
	   }
	}
	return _c; 
}
function writeEnvirements(){
	var _html = "";
    for(var i=0;i<Envirements.length;i++){
    	_html +='<li><a onclick="switchEnvirement(\''+Envirements[i].url+'\',\''+Envirements[i].port+'\',\''+Envirements[i].proj+'\')" href="#"><i class="fa fa-off"></i>'+Envirements[i].name+' 环境</a></li> <li class="divider"></li>';
    } 
    document.write(_html);   
} 
function switchEnvirement(ip,port,proj){  
	 window.location.href="http://"+ip+":"+port+"/"+proj+"/"+CURRENT_FILE; 
}  
function ckpage(page){
	if(page.indexOf(".html") > 0)
page = page.slice(0,page.indexOf(".html")); 
if(page.indexOf(".jsp") > 0)
page = page.slice(0,page.indexOf(".jsp"));  
  if(AUTH_PAGE.get("*")!=null || AUTH_PAGE.get(page)!=null){ 
      return true;
  }
  return false;
}  