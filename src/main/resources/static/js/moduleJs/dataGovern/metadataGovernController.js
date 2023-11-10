var dataGovernApp = angular.module('dataGovernApp',['ng-pagination','ngDialog','ngResource','svgMap']);
function upLoadFiles(objThis){
    alert(objThis);	
}

dataGovernApp.controller('metadataMapController',['$scope','ngDialog','$timeout','$http','jdgpPath',function($scope,ngDialog,$timeout,$http,jdgpPath){
	$scope.metaFTPFilePath="";
	 /*批量上传事件注册*/
     angular.element(document).delegate("#upLoadId","change",function(e){
    	var fd=new FormData();
    	var fileList = this.files;
    	 angular.forEach(fileList, function (file) {
    		 fd.append("file", file);
         });
    	var tableData = $scope.metaFTPDataObj;
//    		fd.append('info',tableData)
    	$http({
    		method:'POST',
    		url:jdgpPath.finalRoot+'/uploadExcel?id='+tableData.treeId+'&ftpFilePath='+$scope.metaFTPFilePath+'&timeStamp='+new Date().getTime(),
    		headers: {
	            'Content-Type': undefined
	          }, 	
	          data:fd,
	          transformRequest: angular.identity
    	}).success(function(data){
    		if(data==0){
    			ngDialog.open({
                    template: '<br/><p>上传excel文件成功</p><br/>',
                    plain: true
                 });
    		}else{
    			ngDialog.open({
                    template: '<br/><p>上传excel文件成功，上传文件中包含非excel文件，已过虑</p><br/>',
                    plain: true
                 });
    		}
    	}).error(function(){
    		ngDialog.open({
                template: '<br/><p>数据错误</p><br/>',
                plain: true
             });
    	})
    	this.value="";
    	$scope.metaFTPFilePath="";
     })
     
	//是否在加载
    $scope.isLoading=false;

    $scope.treeOptions = {nodeChildren: "node"};
    //数据结果集合
    $scope.treeData = [{id:"0",topicName:"数据中心库",dataType:1,parentId:null,node:[{id:"加载中...",topicName:"加载中...",parentId:null,databaseId:null}]}];
    /*节点改变事件 加载子节点数据*/
    $scope.searchKey=null;

    /*keyUp事件添加*/
    $scope.keyup_queryTableValue = function(e){
    	keyCode = window.event?e.keyCode:e.which;
    	if(keyCode==13){
    		$scope.queryTableValue();
    	}
    }   
    /*默认开始被选中节点为最外层第一个数据库类型节点*/
    /*$scope.selectedNode = $scope.treeData[0];*/
    $scope.expanded_Nodes=null;
    var currentNode=null;
    var parentNode=null;
    var dataLength=null;
    $scope.loadNodeData = function(node,expanded,$parentNode,flag){
        if(expanded == false){
            //第一次点击时加载数据
            //苏州项目根主题，只有中心数据库,其他地区的项目调用其他的数据主题$scope.otherDataTheme(node)
            /*$scope.suzhouDataTheme(node);*/
            $scope.isLoading=false;
            
            /*当前节点下所有扩展的子节点全部收缩start*/
            var expandedNodeS = $scope.expanded_Nodes;
            var parentId = node.id;
            function testValue(){
            	var tempList = [];
            	for(var i=0; i< expandedNodeS.length;i++){
            		if(expandedNodeS[i].parentId==parentId){
            			tempList.push(expandedNodeS[i]);
            		}
            	}       		
            	if(tempList.length!=0){
            		//从expandedNodeS删除选出来的子节点
            		for(var m=0;m<tempList.length;m++){
            			parentId = tempList[m].id;
            			for(var n=0;n<expandedNodeS.length;n++){
            				if(tempList[m].id==expandedNodeS[n].id){
            					expandedNodeS.splice(n,1);
            					break;
            				}
            			}
            			testValue();
            		}
            		
            	}
            	
            }
            testValue();
           /*当前节点下所有扩展的子节点全部收缩end*/
        }
        else if (expanded){
        	/*当前节点下所有扩展的子节点全部收缩start*/
            var expandedNodeS = $scope.expanded_Nodes;
            var parentId = node.id;
            function testValue(){
            	var tempList = [];
            	for(var i=0; i< expandedNodeS.length;i++){
            		if(expandedNodeS[i].parentId==parentId){
            			tempList.push(expandedNodeS[i]);
            		}
            	}       		
            	if(tempList.length!=0){
            		//从expandedNodeS删除选出来的子节点
            		for(var m=0;m<tempList.length;m++){
            			parentId = tempList[m].id;
            			for(var n=0;n<expandedNodeS.length;n++){
            				if(tempList[m].id==expandedNodeS[n].id){
            					expandedNodeS.splice(n,1);
            					break;
            				}
            			}
            			testValue();
            		}
            		
            	}
            	
            }
            testValue();
           /*当前节点下所有扩展的子节点全部收缩end*/
        	
            if (node.dataType != undefined && node.dataType == 1){
                var param={
                   id: node.id
                };
                    $http({
                       method:'POST',
                       url:jdgpPath.finalRoot +'/findbypid',
                       params:param
                    }).success(function (data) {
                        var result = [];
                        dataLength=data.length;
                        
                        for(var o in data){
                        	if(data[o].defname!=undefined){
                        		result.push({id:data[o].id,topicName:data[o].name+'('+data[o].defname+')',
                                parentId:data[o].pid,databaseId:data[o].flag,
                                dataType:data[o].type,node:[{id:"加载中...",topicName:"加载中..."}]});
                        	}else{
                        		result.push({id:data[o].id,topicName:data[o].name,
                                parentId:data[o].pid,databaseId:data[o].flag,
                                dataType:data[o].type,node:[{id:"加载中...",topicName:"加载中..."}]});
                        	}

                       }
                        node.node = result;
                        /*if(result.length>0){
                            node.node = result;
                        }else{
                            $scope.expanded_Nodes.pop();
                            delete node.node
                        }*/
                        //新增之后
                        if($scope.metaDataObj!=undefined && $scope.metaDataObj!=null){
                            for(var key2 in result){
                                if(result[key2].topicName==$scope.metaDataObj.datasourceName){
                                    $scope.selectedNode=node.node[key2];
                                }
                            }
                        }
                       //删除Loading
                       $scope.isLoading=false;
                    }).error(function () {
                    	$scope.isLoading=false;
                        ngDialog.open({
                           template: '<br/><p>数据错误</p><br/>',
                           plain: true
                        });
                    });
            }
            //数据源(库)级别
            else if (node.dataType != undefined && node.dataType ==2){
                var param={
                    id: node.id
                };
                $http({
                   method:'POST',
                   url:jdgpPath.finalRoot +'/findbypid',
                   params:param
                }).success(function (data) {
                    dataLength=data.length;
                   
                   var result = [];
                    for(var o in data) {
                        if (data[o].type == 3) {
                        	if(data[o].defname!=undefined){
                        		result.push({id:data[o].id,topicName:data[o].name+'('+data[o].defname+')',
                                parentId:data[o].pid,databaseId:data[o].flag,
                                dataType:data[o].type,node:[{id:"加载中...",topicName:"加载中..."}]});
                        	}else{
                        		result.push({id:data[o].id,topicName:data[o].name,
                                parentId:data[o].pid,databaseId:data[o].flag,
                                dataType:data[o].type,node:[{id:"加载中...",topicName:"加载中..."}]});
                        	}
                        }else if (data[o].type == 1) {
                        	if(data[o].defname!=undefined){
                        		result.push({id:data[o].id,topicName:data[o].name+'('+data[o].defname+')',
                                parentId:data[o].pid,databaseId:data[o].flag,
                                dataType:data[o].type,node:[{id:"加载中...",topicName:"加载中..."}]});
                        	}else{
                        		result.push({id:data[o].id,topicName:data[o].name,
                                parentId:data[o].pid,databaseId:data[o].flag,
                                dataType:data[o].type,node:[{id:"加载中...",topicName:"加载中..."}]});
                        	}
                        }
                    }
                    node.node = result;
                    /*if(result.length>0){
                        node.node = result;
                    }else{
                        $scope.expanded_Nodes.pop();
                        delete node.node
                    }*/
                   //删除Loading
                   $scope.isLoading=false;
                }).error(function () {
                	$scope.isLoading=false;
                    ngDialog.open({
                       template: '<br/><p>数据错误</p><br/>',
                       plain: true
                    });
                });
            }
            //数据表
            else if (node.dataType != undefined && node.dataType ==3){
                var param ={
                    id:node.id
                }
                $http({
                   method:'POST',
                   url:jdgpPath.finalRoot +'/findbypid',
                   params:param
                }).success(function (data) {
                    dataLength=data.length;
                    
                    var result = [];
                    for(var o in data) {
                        if (data[o].type == 4) {
                        	if(data[o].defname!=undefined){
                        		result.push({id:data[o].id,topicName:data[o].name+'('+data[o].defname+')',
                                parentId:data[o].pid,databaseId:data[o].flag,
                                dataType:data[o].type});
                        	}else{
                        		result.push({id:data[o].id,topicName:data[o].name,
                                parentId:data[o].pid,databaseId:data[o].flag,
                                dataType:data[o].type});
                        	}
                        }else{
                        	if(data[o].defname!=undefined){
                        		result.push({id:data[o].id,topicName:data[o].name+'('+data[o].defname+')',
                                parentId:data[o].pid,databaseId:data[o].flag,
                                dataType:data[o].type,node:[{id:"加载中...",topicName:"加载中..."}]});
                        	}else{
                        		result.push({id:data[o].id,topicName:data[o].name,
                                parentId:data[o].pid,databaseId:data[o].flag,
                                dataType:data[o].type});
                        	}
                        }
                    }
                    node.node = result;
                    /*if(result.length>0){
                        node.node = result;
                    }else{
                        $scope.expanded_Nodes.pop();
                        delete node.node
                    }*/
                   //删除Loading
                   $scope.isLoading=false;
                }).error(function () {
                	$scope.isLoading=false;
                    ngDialog.open({
                       template: '<br/><p>数据错误</p><br/>',
                       plain: true
                    });
                });
            }
            //ftp服务器
            else if (node.dataType != undefined && node.dataType ==5){
                var param={
                		treeId: node.id
                };
                $http({
                    method:'POST',
                    url:jdgpPath.finalRoot +'/getSubDir',
                    params:param
                }).success(function (data) {
                    var result = [];
                    dataLength=data.length;
                    
                    for(var o in data){
//                        if(data[o].defname!=undefined){
//                            result.push({id:data[o].id,topicName:data[o].name+'('+data[o].defname+')',
//                                parentId:data[o].pid,databaseId:data[o].flag,
//                                dataType:data[o].type,node:[{id:"加载中...",topicName:"加载中..."}]});
//                        }else{
                            result.push({
                            	id:data[o].id,
                            	topicName:data[o].name,
                                parentId:data[o].pid,
//                            	databaseId:data[o].flag,
                                dataType:data[o].type,
                                defname:data[o].defname
//                            	node:[{id:"加载中...",topicName:"加载中..."}]
                            });
//                        }

                    }
                    node.node = result;
                    /*if(result.length>0){
                        node.node = result;
                    }else{
                        $scope.expanded_Nodes.pop();
                        delete node.node
                    }*/
                    //新增之后
                    if($scope.metaDataObj!=undefined && $scope.metaDataObj!=null){
                        for(var key2 in result){
                            if(result[key2].topicName==$scope.metaDataObj.datasourceName){
                                $scope.selectedNode=node.node[key2];
                            }
                        }
                    }
                    //删除Loading
                    $scope.isLoading=false;
                }).error(function () {
                	$scope.isLoading=false;
                    ngDialog.open({
                        template: '<br/><p>数据错误</p><br/>',
                        plain: true
                    });
                });
            }
            //ftp文件
            else if (node.dataType != undefined && node.dataType ==6){
                var param={
                    id: node.id
                };
                $http({
                    method:'POST',
                    url:jdgpPath.finalRoot +'/findbypid',
                    params:param
                }).success(function (data) {
                    var result = [];
                    dataLength=data.length;
                    
                    for(var o in data){
                        if(data[o].defname!=undefined){
                            result.push({id:data[o].id,topicName:data[o].name+'('+data[o].defname+')',
                                parentId:data[o].pid,databaseId:data[o].flag,
                                dataType:data[o].type,node:[{id:"加载中...",topicName:"加载中..."}]});
                        }else{
                            result.push({id:data[o].id,topicName:data[o].name,
                                parentId:data[o].pid,databaseId:data[o].flag,
                                dataType:data[o].type,node:[{id:"加载中...",topicName:"加载中..."}]});
                        }
                    }
                    node.node = result;
                    /*if(result.length>0){
                        node.node = result;
                    }else{
                        $scope.expanded_Nodes.pop();
                        delete node.node
                    }*/
                    //新增之后
                    if($scope.metaDataObj!=undefined && $scope.metaDataObj!=null){
                        for(var key2 in result){
                            if(result[key2].topicName==$scope.metaDataObj.datasourceName){
                                $scope.selectedNode=node.node[key2];
                            }
                        }
                    }
                    //删除Loading
                    $scope.isLoading=false;
                }).error(function () {
                	$scope.isLoading=false;
                    ngDialog.open({
                        template: '<br/><p>数据错误</p><br/>',
                        plain: true
                    });
                });
            }
            //sheet
            else if (node.dataType != undefined && node.dataType ==7){
                var param={
                    id: node.id
                };
                $http({
                    method:'POST',
                    url:jdgpPath.finalRoot +'/findbypid',
                    params:param
                }).success(function (data) {
                    var result = [];
                    dataLength=data.length;
                    
                    for(var o in data){
                        if(data[o].type== 8){
                            result.push({id:data[o].id,topicName:data[o].name,
                                parentId:data[o].pid,databaseId:data[o].flag,
                                dataType:data[o].type});
                        }else {
                            result.push({id:data[o].id,topicName:data[o].name+'('+data[o].defname+')',
                                parentId:data[o].pid,databaseId:data[o].flag,
                                dataType:data[o].type,node:[{id:"加载中...",topicName:"加载中..."}]});
                        }
                    }
                    node.node = result;
                    /*if(result.length>0){
                        node.node = result;
                    }else{
                        $scope.expanded_Nodes.pop();
                        delete node.node
                    }*/
                    //新增之后
                    if($scope.metaDataObj!=undefined && $scope.metaDataObj!=null){
                        for(var key2 in result){
                            if(result[key2].topicName==$scope.metaDataObj.datasourceName){
                                $scope.selectedNode=node.node[key2];
                            }
                        }
                    }
                    //删除Loading
                    $scope.isLoading=false;
                }).error(function () {
                	$scope.isLoading=false;
                    ngDialog.open({
                        template: '<br/><p>数据错误</p><br/>',
                        plain: true
                    });
                });
            }
        }
    };

    //苏州项目的根主题库
    $scope.suzhouDataTheme = function(node){
        var datas = [];
        var data = {};
        data.id = 0;
        data.topicName = "主题类别";
        data.parentId = null;
        data.dataType=1;
        data.node=[{id:"加载中...",topicName:"加载中..."}]
        datas.push(data);
        node.node = datas;
    };

    //节点点击事件
    $scope.selectNodeClick = function(node, selected,$parentNode){
//    	if($parentNode!=null && $parentNode.dataType == 5)
//    		return;
    	$scope.isLoading = true;
    	$scope.pageCount =1; 
    	$scope.columnValue1="";
    	$scope.columnName1 = "";
    	//翻页控件初始化
    	$scope.currentPage2_show = false;
    	
    	//删除节点初始化
    	if(!(node.node && node.node.length>0 && node.id)){
    		$scope.showDeletBtn='删除';
    	}else{
    		$scope.showDeletBtn='';
    	}
        //传入当前选中节点额id
        console.log(node);
        parentNode=$parentNode
        currentNode=node;
        //保存当前点击节点的id，方便删除节点
        $scope.currentTableIdParams={
            id:node.id
        };
        if(node.dataType == 1){
        	$scope.isLoading = false;
            $scope.addDataNode=true;
            $scope.showmenu='数据源';
            $scope.initMetaData(undefined,undefined,null);
            
        }
        //在右侧显示数据源(库)的信息
        else if(node.dataType == 2){
            $scope.addDataNode=true;
            $scope.showmenu='';
            var params={
                id:node.id
            };
            $http({
              method:'POST',
              url:jdgpPath.finalRoot+'/dataSource/getdsinfo',
              params:params
            }).success(function (data) {
                $scope.initMetaData(false,'1',data);
                $scope.refreshCurrentNode = $parentNode;
                $scope.isLoading=false;
            }).error(function () {
        	  $scope.isLoading=false;
              ngDialog.open({
                  template: '<br/><p>数据错误</p><br/>',
                  plain: true
              });
            });
        }
        //右侧显示表的信息
        else if (node.dataType == 3){
            $scope.addDataNode=true;
            $scope.showmenu='字段';
            $scope.isDataBase='2';
            var datas = $parentNode.data;
            var flag = node.databaseId;
            if('hive'==flag.split('|')[0]){
            	
            	$scope.isDataBase = "hive";
                $scope.isLoading=false;
            }else{
	            var param = {
	                id:node.id
	            };
	            $scope.currentTableName = node.topicName;
	            $scope.queryTableData();  
        	}
        }
        //右侧展示某个字段的信息
        else if(node.dataType== 4){
            $scope.isDataBase='3';
            $scope.addDataNode=false;
            var param = {
                id:node.id
            };
            $http({
                method:'POST',
                url:jdgpPath.finalRoot+'/dataSource/table/column',
                params:param
            }).success(function (data) {
                $scope.fieldList2= data;
                $scope.isLoading=false;
               
            }).error(function () {
            	$scope.isLoading=false;
                ngDialog.open({
                    template: '<br/><p>数据错误</p><br/>',
                    plain: true
                });
            });
        }
        //右侧ftp地址信息展示
        else if(node.dataType == 5){
        	$scope.metaFTPFilePath="";
//        	$scope.showDeletBtn='删除';
            $scope.addDataNode=true;
            $scope.showmenu='';
            var params={
                id:node.id
            };
            $http({
                method:'POST',
                url:jdgpPath.finalRoot+'/dataSource/getdsinfo',
                params:params
            }).success(function (data) {
                $scope.initMetaFTPData(false,'4',data);
                $scope.refreshCurrentNode = $parentNode;
                $scope.isLoading=false;
            }).error(function () {
            	$scope.isLoading=false;
                ngDialog.open({
                    template: '<br/><p>数据错误</p><br/>',
                    plain: true
                });
            });
        }
        //点击ftp文件
        else if(node.dataType == 6){
        	$scope.isLoading=false;
        	$scope.metaFTPFilePath=node.defname;
//            $scope.addDataNode=true;
//            $scope.showmenu   ='Excel字段';
//            $scope.isDataBase='showSheet';
//            $scope.currentTableName = node.topicName;
//            $scope.querySheetData();
        	$scope.addDataNode=true;
            $scope.showmenu='';
            var params={
                id:node.parentId
            };
            $http({
                method:'POST',
                url:jdgpPath.finalRoot+'/dataSource/getdsinfo',
                params:params
            }).success(function (data) {
                $scope.initMetaFTPData(false,'4',data);
                $scope.refreshCurrentNode = $parentNode;
                $scope.isLoading=false;
            }).error(function () {
            	$scope.isLoading=false;
                ngDialog.open({
                    template: '<br/><p>数据错误</p><br/>',
                    plain: true
                });
            });
        }
        //点击sheet信息展示
        else if(node.dataType == 7){
        	$scope.isLoading = false;
            $scope.addDataNode=true;
            $scope.showmenu   ='Excel字段';
            $scope.isDataBase='showSheet';
            $scope.currentTableName = node.topicName;
//            $scope.querySheetData();
        }else if(node.dataType == 8){
        	//hdfs信息
        	$scope.showDeletBtn='删除';
            $scope.addDataNode=false;
            $scope.showmenu='';
            var params={
                id:node.id
            };
            $http({
                method:'POST',
                url:jdgpPath.finalRoot+'/dataSource/getdsinfo',
                params:params
            }).success(function (data) {
                $scope.initMetaHDFSData(false,'5',data);
                $scope.refreshCurrentNode = $parentNode;
                $scope.isLoading=false;
            }).error(function () {
            	$scope.isLoading=false;
                ngDialog.open({
                    template: '<br/><p>数据错误</p><br/>',
                    plain: true
                });
            });
        }else{
        	$scope.showDeletBtn='';
        	$scope.isLoading=false;
        	$scope.initMetaData(undefined,undefined,null);
        }

    }

    //右侧元数据信息
    $scope.initMetaData=function(modifyStatus,status,data){
        //是否处于修改状态
        $scope.isDataBaseModify=modifyStatus;
        //是否处于页面刚开始状态
        $scope.isDataBase=status;
        $scope.metaDataObj=data;
        
    };
    $scope.initMetaFTPData=function(modifyStatus,status,data){
        //是否处于修改状态
        $scope.isDataBaseModify=modifyStatus;
        //是否处于页面刚开始状态
        $scope.isDataBase=status;
        $scope.metaFTPDataObj=data;
    };
    
    $scope.initMetaHDFSData=function(modifyStatus,status,data){
    	$scope.isDataBaseModify=modifyStatus;
        //是否处于页面刚开始状态
        $scope.isDataBase=status;
        $scope.metaHDFSDataObj=data;
    }

    //展示数据表的记录数据
    $scope.showTableRecord = function(obj){
        _url = jdgpPath.finalRoot+'/html/dataGovern/metadataGovern/metaDataTableData.html';
        ngDialog.open({
              template:_url,
              className:'',
              scope:$scope
        });//根据数据源信息查询表字段，并配置每个字段的校验规则
        /*去掉弹框一个插件的多余样式*/
        var a=$timeout(function(){
              angular.element("div[class='ngdialog-close']").hide();
        },100);
    };

    $scope.queryTableData=function(){
//    	$scope.isLoading=true;
        var dataSourceParamObj = {};
       dataSourceParamObj.tableName = $scope.currentTableName;
       $scope.dataSourceParam = dataSourceParamObj;
       _initData();
    };
    //查询数据表
    var columnName = '';
    var columnValue='';
    $scope.queryTableValue= function(){
    	$scope.isLoading = true;
        var Names = angular.element('.selectBorderBlue option:selected').val();
        columnValue= angular.element('.inputDiv input').val();
        // console.log(Names.indexOf('(')+"++++"+Names.indexOf(')'));
        var NameLength1 = Names.indexOf('(');
        var NameLength2 = Names.indexOf(')');
        if(NameLength1 == -1 && NameLength2 == -1){
            columnName = Names;
        }else{
            columnName = Names.slice(NameLength1+1,NameLength2)
        }
        console.log(columnName+"++++"+columnValue);
        _initData(columnName,columnValue);
    };

    //展示sheet Excel表的记录数据
    $scope.querySheetData=function(){
        var dataSourceParamObj = {};
        dataSourceParamObj.tableName = $scope.currentTableName;
        $scope.dataSourceParam = dataSourceParamObj;
        _initSheet();
    };
    
    //表 翻页事件
    $scope.onPageChange=function(currentPage){
       $scope.isLoading=true;
       var dataSourceParamObj = {};
       dataSourceParamObj.tableName = $scope.currentTableName;
       $scope.dataSourceParam = dataSourceParamObj;
        _pageChange(currentPage);
    };
    
    //sheet 翻页事件
    $scope.onPageChange3=function(currentPage){
    	$scope.isLoading=true;
        var dataSourceParamObj = {};
        dataSourceParamObj.tableName = $scope.currentTableName;
        $scope.dataSourceParam = dataSourceParamObj;
        _pageChange3(currentPage);
    };
    
    //新增数据表 翻页事件
    $scope.onPageChangeTable=function(currentPage1) {
    	$scope.isLoading=true;
    	var tableName= angular.element('.inputNameDiv input').val();
    	console.log('tableName'+tableName);
    	if(tableName == undefined ){
    		tableName='';
    	}
        $scope.currentPage1=currentPage1
        /*http请求*/
        var arr=currentNode.databaseId.split('|');
        var params={
        		dbType:arr[0],
                ip:arr[1],
                ds:arr[2],
                tableName:tableName
        };
        $http({
            method:"POST",
            url:jdgpPath.finalRoot +'/dataSource/unselectedtables/'+$scope.currentPage1+'/'+$scope.pageSize,
            params:params
        }).success(function (object) {
        	$scope.isLoading=false;
            console.log($scope.currentPage1);
            $scope.metaTabeName = object.rowslist;
            $scope.showRT="";
            //删除Loading
            //angular.element(".vSpilt_left").scope().isLoading=false;
        }).error(function () {
        	$scope.isLoading=false;
            ngDialog.open({
                template: '<br/><p>数据错误</p><br/>',
                plain: true
            });
        });
    };

    
    /******************数据表分页*********************/
    //动态表格初始化
    _initData = function(columnName,columnValue)
    {
        $scope.currentPage = 1;
        $scope.pageSize = 10;
        $scope.columns=[];
        $scope.values=[];
        /*过滤字段*/
        // $scope.selectTableName = $scope.dataSourceParam.tableName;//选择的表
        $scope.currentTableIdParams.columnName=columnName;
        $scope.currentTableIdParams.columnValue=columnValue;
        /*http请求*/
        $http({
            method:'POST',
            url:jdgpPath.finalRoot+'/db/queryTableData/'+$scope.currentPage+'/'+$scope.pageSize,//分页参数
            params:$scope.currentTableIdParams
            }).success(function (object) {
        	$scope.isLoading=false;
            console.log(object);
            $scope.selectTableName = $scope.dataSourceParam.tableName;//选择的表名称
            	 
                $scope.currentPage2_show = true;
                $scope.currentPage = 1;
                var data = object.rowslist;
                $scope.pageCount = parseInt(object.pagecount); //总页数
                if(data!=null&&data!="" && data.length>0){
                    $scope.columns = data[0].slice(1,data[0].length);
                    $scope.columncomments = data[1];
                    $scope.values = data.slice(2,data.length);//获取除第一条之外的其它数据
                    for(var i=0;i<$scope.values.length;i++){
                        for(var j=0;j<$scope.values[i].length;j++){
                            if(j==0){
                                $scope.values[i][j]=($scope.currentPage-1)*$scope.pageSize+parseInt($scope.values[i][j])+1;
                            }
                        }
                    }
                }
            if(data.length == 0 || data.slice(2,data.length).length== 0){
                $scope.values.length < 0;
                $scope.showSpan=true;
            }else{
            	$scope.values.length > 0;
            	$scope.showSpan=false;
            }
            console.log($scope.values);
            $scope.showRT="";
            //删除Loading
            //angular.element(".vSpilt_left").scope().isLoading=false;
            }).error(function () {
            	$scope.isLoading=false;
                ngDialog.open({
                    template: '<br/><p>数据错误</p><br/>',
                    plain: true
                });
            });
    };
    _pageChange = function(currentPage)
    {
        $scope.currentPage=currentPage;
        //添加Loading
        //angular.element(".vSpilt_left").scope().isLoading=true;
        $scope.selectTableName = $scope.dataSourceParam.tableName;//选择的表
        /*过滤字段*/
        $scope.currentTableIdParams.columnName=columnName;
        $scope.currentTableIdParams.columnValue=columnValue;
        /*http请求*/
        $http({
            method:'POST',
            url:jdgpPath.finalRoot+'/db/queryTableData/'+$scope.currentPage+'/'+$scope.pageSize,//分页参数
            params:$scope.currentTableIdParams
        }).success(function (object) {
        	$scope.isLoading=false;
            console.log($scope.currentPage);
            var data = object.rowslist;
            if(data!=null&&data!="" && data.length>0){
                $scope.pageCount = parseInt(object.pagecount);
                $scope.columns = data[0];
                $scope.columncomments = data[1];
                $scope.values = data.slice(2,data.length);//获取除第一条之外的其它数据
                for(var i=0;i<$scope.values.length;i++){
                    for(var j=0;j<$scope.values[i].length;j++){
                        if(j==0){
                            $scope.values[i][j]=($scope.currentPage-1)*$scope.pageSize+parseInt($scope.values[i][j])+1;
                        }
                    }
                }
            }
            $scope.showRT="";
            //删除Loading
            //angular.element(".vSpilt_left").scope().isLoading=false;
        }).error(function () {
        	$scope.isLoading=false;
            ngDialog.open({
                template: '<br/><p>数据错误</p><br/>',
                plain: true
            });
        });
    };
    /******************sheet Excel表分页*********************/
    //动态sheet初始化
    _initSheet = function () {
        $scope.currentPage = 0;
        $scope.pageSize = 10;
        $scope.columns=[];
        $scope.values=[];
        var arr=currentNode.databaseId.split('|');
        var params={
            ip:arr[0],
            port:arr[1],
            username:arr[2],
            password:arr[3],
            ftpPath:arr[4]
        };
        /*过滤字段*/
        // $scope.selectTableName = $scope.dataSourceParam.tableName;//选择的表
        /*http请求*/
        $http({
            method:'POST',
            url:jdgpPath.finalRoot+'/db/queryExcelData/'+$scope.currentPage+'/'+$scope.pageSize+'?fileName='+arr[5],//分页参数
            params:params
        }).success(function (object) {
        	$scope.isLoading = false;
            $scope.sheetHeader=object.headers
            $scope.selectTableName = $scope.dataSourceParam.tableName;//选择的表名称
            var data = object.rowslist;
            $scope.currentPage=1;
            $scope.pageCount = parseInt(object.pagecount); //总页数
            if(data!=null&&data!="" && data.length>0){
                $scope.sheetValues = data;//获取除第一条之外的其它数据
                for(var i=0;i<$scope.sheetValues.length;i++){
                    for(var j=0;j<$scope.sheetValues[i].length;j++){
                        if(j==0){
                            $scope.sheetValues[i][j]=($scope.currentPage-1)*$scope.pageSize+parseInt($scope.sheetValues[i][j])+1;
                        }
                    }
                }
            }
            console.log($scope.sheetValues);
            $scope.showRT="";
            //删除Loading
            angular.element(".vSpilt_left").scope().isLoading=false;
        }).error(function () {
        	$scope.isLoading=false;
            ngDialog.open({
                template: '<br/><p>数据错误</p><br/>',
                plain: true
            });
        });
    }
    _pageChange3 = function(currentPage)
    {
        $scope.currentPage=currentPage-1;
        //添加Loading
        var arr=currentNode.databaseId.split('|');
        var params={
            ip:arr[0],
            port:arr[1],
            username:arr[2],
            password:arr[3],
            ftpPath:arr[4]
        };
        /*http请求*/
        $http({
            method:'POST',
            url:jdgpPath.finalRoot+'/db/queryExcelData/'+$scope.currentPage+'/'+$scope.pageSize+'?fileName='+arr[5],//分页参数
            params:params
        }).success(function (object) {
        	$scope.isLoading=false;
            var data = object.rowslist;
            if(data!=null&&data!="" && data.length>0){
                $scope.pageCount = parseInt(object.pagecount); //总页数
                $scope.sheetValues = data//获取除第一条之外的其它数据
                for(var i=0;i<$scope.sheetValues.length;i++){
                    for(var j=0;j<$scope.sheetValues[i].length;j++){
                        if(j==0){
                            $scope.sheetValues[i][j]=($scope.currentPage-1)*$scope.pageSize+parseInt($scope.sheetValues[i][j])+1;
                        }
                    }
                }
            }
            $scope.showRT="";
            //删除Loading
            angular.element(".vSpilt_left").scope().isLoading=false;
        }).error(function () {
        	$scope.isLoading=false;
            ngDialog.open({
                template: '<br/><p>数据错误</p><br/>',
                plain: true
            });
        });
    };

    $scope.returnBack = function(){
        angular.element(".ngdialog-close").click();
    };
    //修改数据库点击
    $scope.dataBaseModified=function(){
        $scope.isDataBaseModify=true;
    };

    //保存数据库修改
    $scope.dataBaseSave=function(obj){
        console.log(obj);
        var params={
            pid:currentNode.parentId,
            id:currentNode.id,
            type:2,
            flag:currentNode.databaseId,
            name:obj.databaseDesc,
            defname:obj.instance
           
        };
        //后台保存交互
        $http({
          method:'POST',
          url:jdgpPath.finalRoot+'/savenode',
          params:params
        }).success(function (data) {
            $scope.isDataBaseModify=false;
            $http({
                method:'POST',
                url:jdgpPath.finalRoot+'/savedatasource',
                params:obj
            }).success(function (data) {
                console.log("成功")
                $scope.loadNodeData($scope.refreshCurrentNode,true,null,1);

            }).error(function () {
            	$scope.isLoading=false;
                ngDialog.open({
                    template: '<br/><p>数据错误</p><br/>',
                    plain: true
                });
            });
        }).error(function () {
        	$scope.isLoading=false;
          ngDialog.open({
              template: '<br/><p>数据错误</p><br/>',
              plain: true
          });
        });
    }

    //保存FTP修改
    $scope.dataFTPBaseSave=function(obj){
        console.log(obj);
        var params={
            pid:currentNode.parentId,
            id:currentNode.id,
            type:5,
            flag:currentNode.databaseId,
            name:obj.databaseDesc,
        };
        //后台保存交互
        $http({
            method:'POST',
            url:jdgpPath.finalRoot+'/savenode',
            params:params
        }).success(function (data) {
            $scope.isDataBaseModify=false;
            $http({
                method:'POST',
                url:jdgpPath.finalRoot+'/savedatasource',
                params:obj
            }).success(function (data) {
                console.log("成功")
                $scope.loadNodeData($scope.refreshCurrentNode,true,null,1);
            }).error(function () {
            	$scope.isLoading=false;
                ngDialog.open({
                    template: '<br/><p>数据错误</p><br/>',
                    plain: true
                });
            });
        }).error(function () {
        	$scope.isLoading=false;
            ngDialog.open({
                template: '<br/><p>数据错误</p><br/>',
                plain: true
            });
        });
    }
    //保存HDFS修改
    $scope.dataHDFSBaseSave=function(obj){
        console.log(obj);
        var params={
            pid:currentNode.parentId,
            id:currentNode.id,
            type:8,
            flag:currentNode.databaseId,
            name:obj.databaseDesc,
        };
        //后台保存交互
        $http({
            method:'POST',
            url:jdgpPath.finalRoot+'/savenode',
            params:params
        }).success(function (data) {
            $scope.isDataBaseModify=false;
            $http({
                method:'POST',
                url:jdgpPath.finalRoot+'/savedatasource',
                params:obj
            }).success(function (data) {
                console.log("成功")
                $scope.loadNodeData($scope.refreshCurrentNode,true,null,1);
            }).error(function () {
            	$scope.isLoading=false;
                ngDialog.open({
                    template: '<br/><p>数据错误</p><br/>',
                    plain: true
                });
            });
        }).error(function () {
        	$scope.isLoading=false;
            ngDialog.open({
                template: '<br/><p>数据错误</p><br/>',
                plain: true
            });
        });
    }
    //取消数据库修改
    $scope.cancleBaseModified=function(){
        $scope.isDataBaseModify=false;
    }

    //弹出导入数据库或者表弹框
    $scope.importData=function(type){
        if(type=="0"){
            //表导入
            alert("表导入！");
        }
        else{
            //数据库导入
            $scope.importMetaDataObj={};
            ngDialog.open({
                template:jdgpPath.finalRoot+'/html/dataGovern/metadataGovern/metadataMap_ImportBase.html',
                className:'',
                scope:$scope
            });
            /*去掉弹框一个插件的多余样式*/
            var a=$timeout(function(){
                angular.element("div[class='ngdialog-close']").hide();
            },100);
        }

    }

    //弹出新增数据库或者表弹框
    $scope.addData=function(node){
        if(node.dataType=="2"){
            //表新增
            $scope.addDataType="0";
            $scope.addMetaTableObj={};
        }
        else if (node.dataType=="1"){
            //数据库新增
            $scope.addDataType="1";
            $scope.addMetaDataObj={};
            //新增数据库   另外一个值是FTP服务
            $scope.type="1";
        }
        $scope.currentNode = node;
        // ngDialog.open({
        //   //根据数据源信息查询表字段，并配置每个字段的校验规则
        //   /*去掉弹框一个插件的多余样式*/
        //   template:jdgpPath.finalRoot+'/html/dataGovern/metadataGovern/metadataMap_add.html',
        //   className:'',
        //   scope:$scope
        // });
        /*去掉弹框一个插件的多余样式*/
        // var a=$timeout(function(){
        //       angular.element("div[class='ngdialog-close']").hide();
        // },100);
    };

    //弹出增加主题类别
    $scope.addZTLB=function(){
        $scope.isDataBase="主题类别";
    };
    //弹出增加数据源
    $scope.addSJK=function(){
        $scope.isDataBase="数据源";
    };
    //弹出增加数据表
    $scope.addSJB=function(){
    	$scope.isLoading=true;
    	var tableName= angular.element('.inputNameDiv input').val();
    	console.log('tableName'+tableName);
    	if(tableName == undefined ){
    		tableName='';
    	}
    	console.log('tableName'+tableName);
        var arr=currentNode.databaseId.split('|');
        $scope.currentPage1 = 1;
        $scope.pageSize = 10;
        var params={
        	dbType:arr[0],
            ip:arr[1],
            ds:arr[2],
            tableName:tableName
        };
        $http({
            method:"POST",
            url:jdgpPath.finalRoot +'/dataSource/unselectedtables/'+$scope.currentPage1+'/'+$scope.pageSize,
            params:params
        }).success(function (objeck) {
        	$scope.isLoading=false;
            $scope.pageCount = parseInt(objeck.pagecount); //总页数
            $scope.metaTabeName=objeck.rowslist;
        }).error(function () {
        	$scope.isLoading=false;
            ngDialog.open({
                template: '<br/><p>数据错误</p><br/>',
                plain: true
            });
        });
        $scope.isDataBase="数据表";
    };
    
    //弹出增加字段
    $scope.addZD=function(){
    	$scope.isLoading=true;
        var params={
            id:currentNode.id,
        };
        $http({
            method:"POST",
            url:jdgpPath.finalRoot +'/dataSource/table/unselectedcolumns',
            params:params
        }).success(function (data) {
        	$scope.isLoading=false;
            console.log(data);
            $scope.metafiledName=data;
        }).error(function () {
        	$scope.isLoading=false;
            ngDialog.open({
                template: '<br/><p>数据错误</p><br/>',
                plain: true
            });
        });
        $scope.isDataBase="字段";
    };
    //弹出增加FTP服务器
    $scope.addFTPFWQ=function(){
        $scope.isDataBase="FTP服务器";
    };
    //弹出增加HDFS服务器
    $scope.addHDFSFWQ=function(){
    	$scope.isDataBase="HDFS服务器";
    };
    //弹出增加FTP文件夹
    $scope.addFTPWJJ=function(){
        $scope.isDataBase="FTP文件夹";
        var params={
            treeId:currentNode.id,
        };
        $http({
            method:"POST",
            url:jdgpPath.finalRoot +'/getUnselectedSubDir',
            params:params
        }).success(function (data) {
            console.log(data);
            $scope.metaFTPName=data;
        }).error(function () {
        	$scope.isLoading=false;
            ngDialog.open({
                template: '<br/><p>数据错误</p><br/>',
                plain: true
            });
        });
    };
    //弹出增加sheet
    $scope.addFTPSheet=function () {
        $scope.isDataBase="sheet";
        var arr=currentNode.databaseId.split('|');
        var params={
            ip:arr[0],
            port:arr[1],
            username:arr[2],
            password:arr[3],
            ftpPath:arr[4]
        };
        $http({
            method:"POST",
            url:jdgpPath.finalRoot +'/getSheetInfos?fileName='+arr[5],
            params:params
        }).success(function (data) {
            console.log(data);
            $scope.metaFTPSheet=data;
        }).error(function () {
        	$scope.isLoading=false;
            ngDialog.open({
                template: '<br/><p>数据错误</p><br/>',
                plain: true
            });
        });
    };
    //增加Excel字段
    $scope.addExcelZD=function () {
        $scope.isDataBase='ExcelZD';
        var arr=currentNode.databaseId.split('|');
        var params={
            ip:arr[0],
            port:arr[1],
            username:arr[2],
            password:arr[3],
            ftpPath:arr[4]
        };
        $http({
            method:"POST",
            url:jdgpPath.finalRoot +'/getExcelHeaders?fileName='+arr[5],
            params:params
        }).success(function (data) {
            console.log(data);
            $scope.metaExcelZD=data;
        }).error(function () {
        	$scope.isLoading=false;
            ngDialog.open({
                template: '<br/><p>数据错误</p><br/>',
                plain: true
            });
        });
    }
    //弹出删除框
    $scope.deleteData=function(){
        $scope.deleteMetaDataObj=currentNode;
        $scope.parentObjNode = parentNode;
        if(currentNode.dataType==2){
            //数据库删除
            $scope.deleteMetaDataObj.type="2";
        }else if (currentNode.dataType ==3){
            //表删除
            $scope.deleteMetaDataObj.type="3";
        }else if (currentNode.dataType ==1){
            //类别删除
            $scope.deleteMetaDataObj.type="1";
        }else if (currentNode.dataType ==4){
            //字段删除
            $scope.deleteMetaDataObj.type="4";
        }
        else if (currentNode.dataType ==5){
            //字段删除
            $scope.deleteMetaDataObj.type="5";
        }
        ngDialog.open({
            template:jdgpPath.finalRoot+'/html/dataGovern/metadataGovern/metadataMap_delete.html',
            className:'',
            scope:$scope
        });
        /*去掉弹框一个插件的多余样式*/
        var a=$timeout(function(){
            angular.element("div[class='ngdialog-close']").hide();
        },100);
    };


   /*********************************数据新增*********************************/
   /*****
      新增主题类别
    *****/
   $scope.saveAddThemeData=function(obj) {
       //确认类别新增
       var flag = $scope.themeDataBaseFrom(obj);
       if (flag){
           return;
       }
       obj.flag = currentNode.databaseId;
       obj.pid  = currentNode.id;
       obj.type = 1;
       $scope.addMetaDataObj = obj;
       console.log(obj);
       $http({
           method:'POST',
           url:jdgpPath.finalRoot+'/savenode',
           params:$scope.addMetaDataObj
       }).success(function (data) {
           angular.element(".vSpilt_left").scope().loadNodeData(currentNode,true,null,1);//加载展开的子节点数据
           var num=0;
           for(var key in angular.element(".vSpilt_left").scope().expanded_Nodes){
               num++;
           }
           //angular.element(".vSpilt_left").scope().expanded_Nodes[num+1]=$scope.currentNode;//将节点加入展开节点中
           $scope.isDataBase="";
           $scope.addMetaDataObj.name="";
       }).error(function () {
    	   $scope.isLoading=false;
           ngDialog.open({
               template: '<br/><p>数据错误</p><br/>',
               plain: true
           });
       });

       $(".ngdialog-close").click();
   };

   /****
      新增数据库
    *****/
   $scope.saveAddDataBase=function(obj){
       var flag=$scope.validateDatabaseForm(obj);
       if(flag){
           return;
       }
       var params={
           pid:currentNode.id,
           type:2,
           flag:obj.type+'|'+obj.ip+'|'+obj.instance+'.'+obj.username,
           name:obj.databaseDesc,
           defname:obj.instance
       };
       $http({
           method:'POST',
           url:jdgpPath.finalRoot+'/savenode',
           params:params
       }).success(function (data) {
           console.log(data);
           var param={
               treeId:data.id,
               dbType:obj.type,
               ip:obj.ip,
               port:obj.port,
               instance:obj.instance,
               datasourceName:obj.datasourceName,
               username:obj.username,
               password:obj.password,
               databaseDesc:obj.databaseDesc,
           };
           $http({
               method:'POST',
               url:jdgpPath.finalRoot+'/savedatasource',
               params:param
           }).success(function (data) {
               angular.element(".vSpilt_left").scope().loadNodeData(currentNode,true,null,1);//加载展开的子节点数据
               var num=0;
               for(var key in angular.element(".vSpilt_left").scope().expanded_Nodes){
                   num++;
               }
              // angular.element(".vSpilt_left").scope().expanded_Nodes[num+1]=$scope.currentNode;//将节点加入展开节点中
               // angular.element(".vSpilt_left").scope().refreshCurrentNode=$scope.currentNode;//新增后自动获取新增节点的父节点
               $scope.isDataBase="";
               $scope.addMetaDataObj="";
           }).error(function () {
           });
       }).error(function () {
    	   $scope.isLoading=false;
           ngDialog.open({
               template: '<br/><p>数据错误</p><br/>',
               plain: true
           });
       });

   };

   /****
      新增数据表
    * ****/
   $scope.saveAddTableData=function(obj){
       var node=angular.element('#jobTaskTableD .tableTr').find("td input[type='checkbox']:checked")
       var arr=[];
       var name='';
       for(var i=0;i<node.length;i++){
           if(node[i].name){
               var chinaName = node[i].name
               name=chinaName
               defname=node[i].value
           }else {
               name=node[i].value;
               defname=node[i].value
           }
           arr.push({
               name:name,
               defname:defname,
               flag:currentNode.databaseId+'.'+node[i].value,
               pid:currentNode.id,
               type:3
           })
       }
       console.log(arr);
       var info=JSON.stringify(arr);
       $http({
           method:'POST',
           url:jdgpPath.finalRoot+'/savenodes',
           data:info,
           headers: {
               'Content-Type': 'application/json'
           }
       }).success(function (data) {
           angular.element(".vSpilt_left").scope().loadNodeData(currentNode,true,null,1);//加载展开的子节点数据
           var num=0;
           for(var key in angular.element(".vSpilt_left").scope().expanded_Nodes){
               num++;
           }
           //angular.element(".vSpilt_left").scope().expanded_Nodes[num+1]=$scope.currentNode;//将节点加入展开节点中
           $scope.isDataBase="";
       }).error(function(){
    	   $scope.isLoading=false;
           ngDialog.open({
               template: '<br/><p>数据错误</p><br/>',
               plain: true
           });
       })
   };
   /****
      新增字段
    * *****/
   $scope.saveAddFiledData=function(obj){
	   debugger;
       var node=angular.element('#jobTaskFiled .tableTr').find("td input[type='checkbox']:checked");
       var arr=[];
       var name='';
       for(var i=0;i<node.length;i++){
           if(node[i].name){
               var chinaName = node[i].name
               name=chinaName
               defname=node[i].value
           }else {
               name=node[i].value;
               defname=node[i].value
           }
           var status = angular.element(node[i]).parent().next().next().find("input[type='radio']:checked")[0].value;
           arr.push({
               name:name,
               defname:defname,
               flag:currentNode.databaseId+'.'+node[i].value,
               pid:currentNode.id,
               type:4,
               showStatus:status
           })
       }
       var info=JSON.stringify(arr);
       $http({
           method:'POST',
           url:jdgpPath.finalRoot+'/savenodes',
           data:info,
           headers: {
               'Content-Type': 'application/json'
           }
       }).success(function (data) {
           angular.element(".vSpilt_left").scope().loadNodeData(currentNode,true,null,1);//加载展开的子节点数据
           var num=0;
           for(var key in angular.element(".vSpilt_left").scope().expanded_Nodes){
               num++;
           }
           //angular.element(".vSpilt_left").scope().expanded_Nodes[num+1]=$scope.currentNode;//将节点加入展开节点中
           $scope.isDataBase="";
       }).error(function(){
    	   $scope.isLoading=false;
           ngDialog.open({
               template: '<br/><p>数据错误</p><br/>',
               plain: true
           });
       })
   };

    /***
     *新增FTP服务器
     * ***/
    $scope.saveAddFTPData=function (obj) {
        var flag = $scope.validateFtpForm(obj)
        if(flag){
            return;
        };
        var params={
            pid:currentNode.id,
            type:5,
            name:obj.datasourceName,
            flag:obj.ip+'|'+obj.port+'|'+obj.username+'|'+obj.password+'|'+obj.ftpPath
        };
        $http({
            method:'POST',
            url:jdgpPath.finalRoot+'/savenode',
            params:params
        }).success(function (data) {
            console.log(data);
            var param={
                treeId:data.id,
                dbType:'ftp',
                ip:obj.ip,
                port:obj.port,
                datasourceName:obj.datasourceName,
                username:obj.username,
                password:obj.password,
                databaseDesc:obj.databaseDesc,
                ftpPath:obj.ftpPath
            };
            $http({
                method:'POST',
                url:jdgpPath.finalRoot+'/savedatasource',
                params:param
            }).success(function (data) {
                angular.element(".vSpilt_left").scope().loadNodeData(currentNode,true,null,1);//加载展开的子节点数据
                var num=0;
                for(var key in angular.element(".vSpilt_left").scope().expanded_Nodes){
                    num++;
                }
                //angular.element(".vSpilt_left").scope().expanded_Nodes[num+1]=$scope.currentNode;//将节点加入展开节点中
                $scope.isDataBase="";
                $scope.addMetaDataObj="";
            }).error(function () {
            });
        }).error(function () {
        	$scope.isLoading=false;
            ngDialog.open({
                template: '<br/><p>数据错误</p><br/>',
                plain: true
            });
        });
    }

    /****
     新增FTP文件夹
     * *****/
    $scope.saveAddFTPWJData=function(obj){
        var node=angular.element('#jobTaskFTPwjj .tableTr').find("td input[type='checkbox']:checked");
        var arr=[];
        var name='';
        console.log(currentNode);
        for(var i=0;i<node.length;i++){
            if(node[i].name){
                var chinaName = node[i].name
                name=chinaName
                defname=node[i].value
            }else {
                name=node[i].value;
                defname=node[i].value
            }
            arr.push({
                name:name,
                defname:defname,
                flag:currentNode.databaseId+'|'+defname,
                pid:currentNode.id,
                type:6
            })
        }
        var info=JSON.stringify(arr);
        $http({
            method:'POST',
            url:jdgpPath.finalRoot+'/savenodes',
            data:info,
            headers: {
                'Content-Type': 'application/json'
            }
        }).success(function (data) {
            angular.element(".vSpilt_left").scope().loadNodeData(currentNode,true,null,1);//加载展开的子节点数据
            var num=0;
            for(var key in angular.element(".vSpilt_left").scope().expanded_Nodes){
                num++;
            }
            //angular.element(".vSpilt_left").scope().expanded_Nodes[num+1]=$scope.currentNode;//将节点加入展开节点中
            $scope.isDataBase="";
        }).error(function(){
        	$scope.isLoading=false;
            ngDialog.open({
                template: '<br/><p>数据错误</p><br/>',
                plain: true
            });
        })
    };

    /****
     *新增sheet
     * *****/
    $scope.saveAddFTPsheetData=function () {
        var node=angular.element('#jobTaskFTPSheet .tableTr').find("td input[type='checkbox']:checked");
        var arr=[];
        var name='';
        console.log(currentNode);
        for(var i=0;i<node.length;i++){
            if(node[i].name){
                var chinaName = node[i].name
                name=chinaName
                defname=node[i].value
            }else {
                name=node[i].value;
                defname=node[i].value
            }
            arr.push({
                name:name,
                defname:defname,
                flag:currentNode.databaseId+'|'+defname,
                pid:currentNode.id,
                type:7
            })
        }
        var info=JSON.stringify(arr);
        $http({
            method:'POST',
            url:jdgpPath.finalRoot+'/savenodes',
            data:info,
            headers: {
                'Content-Type': 'application/json'
            }
        }).success(function (data) {
            angular.element(".vSpilt_left").scope().loadNodeData(currentNode,true,null,1);//加载展开的子节点数据
            var num=0;
            for(var key in angular.element(".vSpilt_left").scope().expanded_Nodes){
                num++;
            }
            //angular.element(".vSpilt_left").scope().expanded_Nodes[num+1]=$scope.currentNode;//将节点加入展开节点中
            $scope.isDataBase="";
        }).error(function(){
        	$scope.isLoading=false;
            ngDialog.open({
                template: '<br/><p>数据错误</p><br/>',
                plain: true
            });
        })
    };

    /***
      新增Excel字段
     * ****/
    $scope.saveAddExcelZDData=function (obj) {
        var node=angular.element('#jobTaskExcelZD .tableTr').find("td input[type='checkbox']:checked");
        var arr=[];
        var name='';
        console.log(currentNode);
        for(var i=0;i<node.length;i++){
            if(node[i].name){
                var chinaName = node[i].name
                name=chinaName
                defname=node[i].value
            }else {
                name=node[i].value;
                defname=node[i].value
            }
            arr.push({
            	defname:name,
                name:defname,
                flag:currentNode.databaseId+'|'+defname,
                pid:currentNode.id,
                type:8
            })
        }
        var info=JSON.stringify(arr);
        $http({
            method:'POST',
            url:jdgpPath.finalRoot+'/savenodes',
            data:info,
            headers: {
                'Content-Type': 'application/json'
            }
        }).success(function (data) {
            angular.element(".vSpilt_left").scope().loadNodeData(currentNode,true,null,1);//加载展开的子节点数据
            var num=0;
            for(var key in angular.element(".vSpilt_left").scope().expanded_Nodes){
                num++;
            }
            //angular.element(".vSpilt_left").scope().expanded_Nodes[num+1]=$scope.currentNode;//将节点加入展开节点中
            $scope.isDataBase="";
        }).error(function(){
        	$scope.isLoading=false;
            ngDialog.open({
                template: '<br/><p>数据错误</p><br/>',
                plain: true
            });
        })
    }
    
    /***
     *新增HDFS服务器
     * ***/
    $scope.saveAddHDFSData=function (obj) {
        var flag = $scope.validateHdfsForm(obj)
        if(flag){
            return;
        };
        var params={
            pid:currentNode.id,
            type:8,
            name:obj.datasourceName,
            flag:obj.ip+'|'+obj.port+'|'+obj.username+'|'+obj.userGroup+'|'+obj.hdfsPath
        };
        $http({
            method:'POST',
            url:jdgpPath.finalRoot+'/savenode',
            params:params
        }).success(function (data) {
            console.log(data);
            var param={
                treeId:data.id,
                dbType:'hdfs',
                ip:obj.ip,
                port:obj.port,
                datasourceName:obj.datasourceName,
                username:obj.username,
                userGroup:obj.userGroup,
                databaseDesc:obj.databaseDesc,
                hdfsPath:obj.hdfsPath
            };
            $http({
                method:'POST',
                url:jdgpPath.finalRoot+'/savedatasource',
                params:param
            }).success(function (data) {
                angular.element(".vSpilt_left").scope().loadNodeData(currentNode,true,null,1);
                var num=0;
                for(var key in angular.element(".vSpilt_left").scope().expanded_Nodes){
                    num++;
                }
                $scope.isDataBase="";
                $scope.addMetaDataObj="";
            }).error(function () {
            });
        }).error(function () {
        	$scope.isLoading=false;
            ngDialog.open({
                template: '<br/><p>数据错误</p><br/>',
                plain: true
            });
        });
    }

    /********************************添加数据信息验证*********************************/
    //添加主题类别时表单校验
    $scope.themeDataBaseFrom=function(obj){
        $scope.initDatabaseFormStatus();
        var flag = false;
        if(obj.name=="" || obj.name==undefined){
            $scope.datathemeNameShow=true;
            flag = true;
        }
        return flag;
    };

    //FTP添加或者修改时表单校验
    $scope.validateFtpForm = function(obj){
        $scope.initFTPFormStatus();
        var flag = false;
        if (obj.datasourceName =="" || obj.datasourceName==undefined){
            $scope.datasourceNameShow = true;
            flag = true;
        }
        if (obj.ip =="" || obj.ip==undefined){
            $scope.ipShow = true;
            flag = true;
        }else{
            if (!(/^\d+\.\d+\.\d+\.\d+$/.test(obj.ip))){
                $scope.isIpShow = true;
                flag = true;
            }
        }
        if (obj.port=="" || obj.port==undefined){
            $scope.portShow = true;
            flag = true;
        }else{
            if (!(/^[1-9]\d*$/.test(obj.port))){
                $scope.isPortShow = true;
                flag = true;
            }
        }
        if (obj.username==""||obj.username==undefined){
            $scope.usernameShow = true;
            flag = true;
        }
        if (obj.password==""||obj.password==undefined){
            $scope.passwordShow = true;
            flag = true;
        }
        return flag;
    }

    //数据库添加或者修改时的校验
    $scope.validateDatabaseForm = function(obj){
        $scope.initDatabaseFormStatus();
        var flag = false;
        if (obj.datasourceName =="" || obj.datasourceName==undefined){
            $scope.datasourceNameShow = true;
            flag = true;
        }
        if (obj.databaseDesc =="" || obj.databaseDesc==undefined){
            $scope.databaseDescShow = true;
            flag = true;
        }
        if (obj.instance ==""||obj.instance==undefined){
            $scope.instanceShow = true;
            flag = true;
        }
        if (obj.type ==""||obj.type==undefined){
            $scope.typeShow = true;
            flag = true;
        }
        if (obj.ip =="" || obj.ip==undefined){
            $scope.ipShow = true;
            flag = true;
        }else{
            if (!(/^\d+\.\d+\.\d+\.\d+$/.test(obj.ip))){
                $scope.isIpShow = true;
                flag = true;
            }
        }
        if (obj.port=="" || obj.port==undefined){
            $scope.portShow = true;
            flag = true;
        }else{
            if (!(/^[1-9]\d*$/.test(obj.port))){
                $scope.isPortShow = true;
                flag = true;
            }
        }
        if (obj.username==""||obj.username==undefined){
            $scope.usernameShow = true;
            flag = true;
        }
        if (obj.password==""||obj.password==undefined){
            $scope.passwordShow = true;
            flag = true;
        }
        return flag;
    };

  //HDFS添加或者修改时表单校验
    $scope.validateHdfsForm = function(obj){
        $scope.initHDFSFormStatus();
        var flag = false;
        if (obj.datasourceName =="" || obj.datasourceName==undefined){
            $scope.datasourceNameShow = true;
            flag = true;
        }
        if (obj.ip =="" || obj.ip==undefined){
            $scope.ipShow = true;
            flag = true;
        }else{
            if (!(/^\d+\.\d+\.\d+\.\d+$/.test(obj.ip))){
                $scope.isIpShow = true;
                flag = true;
            }
        }
        if (obj.port=="" || obj.port==undefined){
            $scope.portShow = true;
            flag = true;
        }else{
            if (!(/^[1-9]\d*$/.test(obj.port))){
                $scope.isPortShow = true;
                flag = true;
            }
        }
        if (obj.username==""||obj.username==undefined){
            $scope.usernameShow = true;
            flag = true;
        }
//        if (obj.password==""||obj.password==undefined){
//            $scope.passwordShow = true;
//            flag = true;
//        }
        return flag;
    }

    /********************************提示按钮初始状态*********************************/
    //切换按钮时初始化状态
    $scope.initFTPFormStatus = function(){
        $scope.datasourceNameShow = false;
        $scope.ipShow = false;
        $scope.isIpShow = false;
        $scope.portShow = false;
        $scope.isPortShow = false;
        $scope.usernameShow = false;
        $scope.passwordShow = false;
    }
    
  //切换按钮时初始化状态
    $scope.initHDFSFormStatus = function(){
        $scope.datasourceNameShow = false;
        $scope.ipShow = false;
        $scope.isIpShow = false;
        $scope.portShow = false;
        $scope.isPortShow = false;
        $scope.usernameShow = false;
        //$scope.passwordShow = false;
    }

    //切换按钮时初始化状态
    $scope.initDatabaseFormStatus = function(){
        $scope.datatableNameShow=false;
        $scope.datathemeNameShow=false;
        $scope.datasourceNameShow = false;
        $scope.databaseDescShow = false;
        $scope.instanceShow = false;
        $scope.typeShow = false;
        $scope.ipShow = false;
        $scope.isIpShow = false;
        $scope.portShow = false;
        $scope.isPortShow = false;
        $scope.usernameShow = false;
        $scope.passwordShow = false;
    }
}]);

/*数据地图--数据删除*/
dataGovernApp.controller('metadataMapDeleteController',['$scope','$http','jdgpPath',function($scope,$http,jdgpPath){
    //确认新增数据库或者表
    $scope.sureDeleteData=function(type){
        //删除
        console.log("type:"+type+"#######"+"id:"+$scope.deleteMetaDataObj.id+"++++++");
        console.log($scope.parentObjNode);//当前节点的父节点
        // console.log("DELETE*****$scope.deleteMetaDataObj="+JSON.stringify($scope.deleteMetaDataObj));
        //获取已选择的数据信息
        $http({
            method:'POST',
            url:jdgpPath.finalRoot+'/deletenode',
            params:{
                id:$scope.deleteMetaDataObj.id
            }
        }).success(function () {
            $scope.loadNodeData($scope.parentObjNode,true,null,1);//加载父节点的子节点数据
            if(angular.element(".vSpilt_left").scope().metaDataObj!=undefined && angular.element(".vSpilt_left").scope().metaDataObj!=null){
                angular.element(".vSpilt_left").scope().metaDataObj=undefined;
                angular.element(".vSpilt_left").scope().isDataBaseModify=undefined;
                angular.element(".vSpilt_left").scope().isDataBase=undefined;
            }

        }).error(function () {

        });
        $(".ngdialog-close").click();
    }

    //关闭
    $scope.cancelData=function(){
        $(".ngdialog-close").click();
    }

}]);

dataGovernApp.directive('clearStandardNameCheck',function(){
    return{
        scope:false,
        restrict:'A',
        link:function(scope,element,attr){
            element.on('click',function(){
                if(element.attr("value")=="tableItems"){
                    element.parent("th").find("a").css("display","none");
                    element.nextAll("select").css("display","");
                    element.nextAll(".checkCancle").css("display","");
                    element.nextAll("input").css("display","none");
                }
                else{
                    element.parent("th").find("a").css("display","none");
                    element.nextAll("input").css("display","");
                    element.nextAll(".checkCancle").css("display","");
                    element.nextAll("select").css("display","none");
                }
            });
        }
    }
});

dataGovernApp.directive('clearStandardNameCancle',function(){
    return{
        scope:false,
        restrict:'A',
        link:function(scope,element,attr){
            element.on('click',function(){
                    element.prevAll("select").css("display","none");
                    element.prevAll("input").css("display","none");
                    element.css("display","none");
                    element.prevAll("a").css("display","");

            });
        }
    }
});

dataGovernApp.directive('clearStandardAdd',['$compile',function($compile){
    return{
        scope:false,
        restrict:'A',
        link:function(scope,element,attr){
            element.on('click',function(){
                element.parents("tr").after( $compile(element.parents("tr").clone())(scope));
                element.parents("tr").next("tr").find("a[class!='checkCancle']").css("display","");
                element.parents("tr").next("tr").find(".checkCancle").css("display","none");
                element.parents("tr").next("tr").find("select").css("display","none");
                element.parents("tr").next("tr").find("input").css("display","none");

            });
        }
    }
}]);

dataGovernApp.directive('clearStandardDelete',['$compile',function($compile){
    return{
        scope:false,
        restrict:'A',
        link:function(scope,element,attr){
            element.on('click',function(){
                if(element.parents("table").children("tr[class='jdgpTableItems']").length<2){
                    element.parents("tr").find("a[class!='checkCancle']").css("display","");
                    element.parents("tr").find(".checkCancle").css("display","none");
                    element.parents("tr").find("select").css("display","none");
                    element.parents("tr").find("input").css("display","none");
                }
                else{
                    element.parents("tr").remove();
                }


            });
        }
    }
}]);