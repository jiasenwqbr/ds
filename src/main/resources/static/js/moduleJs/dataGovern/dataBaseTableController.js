/**
 * Created by hht on 2016/11/24.
 * 查看表数据
 */
homeApp.controller('dataBaseTableController',['$scope','ngDialog','$timeout','$http','jdgpPath',function($scope,ngDialog,$timeout,$http,jdgpPath) {
    $scope.showRT = "none"
    $scope.pageSize = 10;//每页显示条数
    $scope.pageCount=1; //定义总页数
    $scope.currentPage=1;//当前页数
    $scope.queryTableData=function(){
       _initData();
    };//当前页数改变方法
    $scope.onPageChange=function(){
        _pageChange();
    };//当前页数改变方法

    $scope.columns = [];
    $scope.values = [];
    //监控树节点改变事件，数据由父controller广播
    $scope.$on("tree_selectedNode",function (event, node) {
        console.log(node);
        $scope.selectedNode = node;
        //数据请求连接
        $scope.dataSourceParam = node.parentNode.dbParam;
        $scope.dataSourceParam.tableName = node.id;
        _initData();//重新初始化
    });


    //动态表格初始化
    _initData = function()
    {
        //添加Loading
        angular.element(".vSpilt_left").scope().isLoading=true;
        $scope.currentPage = 0;
        $scope.pageSize = 10;
        $scope.columns=[];
        $scope.values=[];
        /*过滤字段*/
        $scope.dataSourceParam.columnName=$scope.columnName;
        $scope.dataSourceParam.columnValue=$scope.columnValue;
        /*http请求*/
        $http({
            method:'POST',
            url:jdgpPath.finalRoot+'/system/db/queryTableData/'+$scope.currentPage+'/'+$scope.pageSize,//分页参数
            params:$scope.dataSourceParam
            }).success(function (object) {
                debugger;
                $scope.selectTableName = $scope.dataSourceParam.tableName;//选择的表
                var data = object.rowslist;
                $scope.currentPage=1;
                $scope.pageCount = parseInt(object.pagecount);
                if(data!=null&&data!="" && data.length>0){
                    $scope.columns = data[0];
                    $scope.values = data.slice(1,data.length);//获取除第一条之外的其它数据
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
            angular.element(".vSpilt_left").scope().isLoading=false;
            }).error(function () {
                ngDialog.open({
                    template: '<br/><p>数据错误</p><br/>',
                    plain: true
                });
            });
    };
    _pageChange = function()
    {
        //添加Loading
        angular.element(".vSpilt_left").scope().isLoading=true;
        $scope.selectTableName = $scope.dataSourceParam.tableName;//选择的表
        /*过滤字段*/
        $scope.dataSourceParam.columnName=$scope.columnName;
        $scope.dataSourceParam.columnValue=$scope.columnValue;
        /*http请求*/
        $http({
            method:'POST',
            url:jdgpPath.finalRoot+'/system/db/queryTableData/'+$scope.currentPage+'/'+$scope.pageSize,//分页参数
            params:$scope.dataSourceParam
        }).success(function (object) {
            var data = object.rowslist;
           // alert(JSON.stringify(data));
            if(data!=null&&data!="" && data.length>0){
                $scope.pageCount = parseInt(object.pagecount);
                $scope.columns = data[0];
                $scope.values = data.slice(1,data.length);//获取除第一条之外的其它数据
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
            angular.element(".vSpilt_left").scope().isLoading=false;
        }).error(function () {
            ngDialog.open({
                template: '<br/><p>数据错误</p><br/>',
                plain: true
            });
        });
    }
}]);
homeApp.directive('jdgpDataGrid',['jdgpPath',function(jdgpPath){
    return {
        restrict:'EA',
        replace:false,
        scope:{
            columns:'=columns',//双向数据绑定
            values:'=values',
            c1:'=currentPage',
            c2:'=pageSize'
        },
        templateUrl:jdgpPath.finalRoot+'/html/dataGovern/dataBaseTable/dataBaseTable.html'
    };
}]);
