/**
 * Created by tuyadi on 2016/10/27.
 */
jdgpApp.directive('checkBoxDiy',function(){
    return{
        scope:false,
        restrict:'E',
        template: "<button class='checkBoxDIY checkBoxDIYNotC' check-box-diy-c=''></button>",
        replace: false
    }
});

jdgpApp.directive('checkBoxDiyA',function(){
    return{
        scope:false,
        restrict:'E',
        template: "<button class='checkBoxDIY checkBoxDIYNotC_all' check-box-diy-c-all=''></button>",
        replace: false
    }
});

jdgpApp.directive('checkBoxDiyC',function(){
    return{
        scope:false,
        restrict:'A',
        link:function(scope,element,attrs){
            element.on("click",function(){
                if(element.attr("class").indexOf("checkBoxDIYNotC")>=0){
                    element.removeClass("checkBoxDIYNotC");
                    element.addClass("checkBoxDIYC");
                }
                else{
                    element.addClass("checkBoxDIYNotC");
                    element.removeClass("checkBoxDIYC");
                }

            });
        }
    }
});

jdgpApp.directive('checkBoxDiyCAll',function(){
    return{
        scope:false,
        restrict:'A',
        link:function(scope,element,attrs){
            element.on("click",function(){
                if(element.attr("class").indexOf("checkBoxDIYNotC_all")>=0){
                    element.removeClass("checkBoxDIYNotC_all");
                    element.addClass("checkBoxDIYC_all");

                    //改变其他的勾选状态
                    element.parents("table").find("button[class*='checkBoxDIY']:not('button[class*='_all']')").removeClass("checkBoxDIYNotC");
                    element.parents("table").find("button[class*='checkBoxDIY']:not('button[class*='_all']')").addClass("checkBoxDIYC");
                }
                else{
                    element.addClass("checkBoxDIYNotC_all");
                    element.removeClass("checkBoxDIYC_all");

                    //改变其他的勾选状态
                    element.parents("table").find("button[class*='checkBoxDIY']:not('button[class*='_all']')").addClass("checkBoxDIYNotC");
                    element.parents("table").find("button[class*='checkBoxDIY']:not('button[class*='_all']')").removeClass("checkBoxDIYC");
                }
            });
        }
    }

});

jdgpApp.directive('loading',function(){
    return{
        scope:{
            isLoading:'='
        },
        restrict:'E',
        template:'<div style="display:none;width: 100%;height:100%;position: absolute;top:0px;left: 0px;background: rgba(102, 102, 102, 0.4);text-align: center;z-index:9999999999999">'+
        '<img src="img/loading.gif" style="width: 48px;height: 48px;margin-top:20%;">'+
        '</div>',
        link:function(scope,element,attrs){
            element.attr("id","LoadingDiv");
            element.find("div").css("height",parseInt(document.getElementById("container").scrollHeight));
            scope.$watch('isLoading',function(newValue){
                if(newValue==true){
                    element.find("div").css("display","");
                }
                if(newValue==false){
                    element.find("div").css("display","none");
                }
            });

        },
        replace: false
    }
});
