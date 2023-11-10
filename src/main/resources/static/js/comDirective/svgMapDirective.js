/**
 * Created by tuyadi on 2016/11/22.
 */
(function(angular) {
    'use strict';//JS代码进入严格模式
    var svgMap=angular.module('svgMap',[]);//定义module

    //设置控件的全局变量
    svgMap.constant('svgMapConfig',{
        fontSize:13//字体大小
    });

    //控件自定义指令
    svgMap.directive('svgPicture',['svgMapConfig','$timeout','ngDialog',function (svgMapConfig,$timeout,ngDialog) {
        return{
            link:function(scope,element,attrs){
                //定义function里的公共变量
                scope.fontSize = angular.isDefined(attrs.fontSize) ? attrs.fontSize : svgMapConfig.fontSize;
                var s = Snap("#svg");
                var rectHPadding = 5;//方框的上下内边距
                var rectPadding = 15;//方框的左右内边距
                var fontSize = angular.isDefined(attrs.fontSize) ? attrs.fontSize : svgMapConfig.fontSize;
                var rectHeight = 2 * rectHPadding + fontSize;
                var circleR = 15;
                var sHeight = element.parent("div").height();
                var sWidth = element.parent("div").width();
                var y0 = 5;
                var textFirst=null;//开始svg没有图，说明的文字。

                //设置画布大小
                s.attr({
                    height: sHeight,
                    width: sWidth
                });

                //找曲线中间点方法
                var a = function (du, x, y, type) {
                    var line_0_2 = Math.sqrt(y * y + x * x);
                    var line_0_1 = line_0_2 / 2 / Math.cos(du);

                    //上曲线
                    if (type == 1) {
                        var e = Math.atan(y / x) - du;
                        var p1y = line_0_1 * Math.sin(e);
                        var p1x = line_0_1 * Math.cos(e);

                        if (x < 0) {
                            p1x = -p1x;
                            p1y = -p1y;
                        }
                    }
                    //下曲线
                    else {
                        var e = Math.atan(x / y) - du;
                        var p1x = line_0_1 * Math.sin(e);
                        var p1y = line_0_1 * Math.cos(e);

                        if (y < 0) {
                            p1x = -p1x;
                            p1y = -p1y;
                        }
                    }

                    return [p1x, p1y]
                };

                //拼接曲线方法
                var curve = function (p0x, p0y, p1x, p1y, p2x, p2y, direction) {
                    var du = 20 * 2 * Math.PI / 360;
                    var x1 = p1x - p0x;
                    var y1 = p1y - p0y;
                    var x2 = p2x - p1x;
                    var y2 = p2y - p1y;
                    var s = 'M' + p0x + ',' + p0y;
                    var s1r, s2r, s1, s2;

                    if (y1 == 0 || x1 == 0) {
                        s1r = ' L' + p1x + ',' + p1y;
                        s2r = ' L' + p2x + ',' + p2y;
                    } else {
                        if (y1 > 0) {
                            s1 = a(du, x1, y1, 1);
                            s2 = a(du, x2, y2, 2);
                        } else {
                            s1 = a(du, x1, y1, 2);
                            s2 = a(du, x2, y2, 1);
                        }
                        s1r = ' q' + s1[0] + ',' + s1[1] + ' ' + x1 + ',' + y1;
                        s2r = ' q' + s2[0] + ',' + s2[1] + ' ' + x2 + ',' + y2
                    }

                    s += s1r + s2r
                    return s;
                };

                //左右两边源、目标库公共画图方法
                var dataShow=function(dataObj,dataType,x2,g,gL){
                    if(dataObj.length>0){
                        for (var k = 0; k < dataObj.length; k++) {
                            var rectWidth = 150;
                            var restT,textT,x0;//分别为，数据库长方形框，数据库名称，x0为x轴初始位置。
                            //连线起点和终点
                            var x1,y1;
                            if(dataType=="dataBaseS"){//源数据库，在左侧
                                x0=20;
                                x1=20 + rectWidth;
                            }
                            else{//目标数据库，在右侧
                                x0=sWidth - 20 - rectWidth;
                                x1=sWidth - 20 - rectWidth;
                            }
                            if(dataObj.length>6){//以每组数据库个数均匀分布排列间隔不等
                                y1 = y0 + i * (sHeight - 2 * y0-rectHeight) /(dataObj.length-1) + rectHeight/2;
                            }
                            else{//每个数据库或表的间隔固定为100
                                y1 = y0+(sHeight-2*y0-100*(dataObj.length-1)-rectHeight*dataObj.length)/2 + k *100 + rectHeight/2;
                            }
                            var y2 = (sHeight - 2 * y0) / 2;

                            //第一个四等分点
                            var x3 = (x2 + 3 * x1) / 4;
                            var y3 = y1;

                            /*数据库长方形框*/
                            var text=dataObj[k].node_name;
                            var isHover=false;
                            var gRect=s.g();
                            if(/.*[\u4e00-\u9fa5]+.*$/.test(dataObj[k].node_name) && dataObj[k].node_name.length>6){
                                text=dataObj[k].node_name.substr(0,6)+"...";//名称为汉字
                                isHover=true;
                            }

                            if(/^[a-zA-Z]/.test(dataObj[k].node_name.node_name)&& dataObj[k].node_name.length>15){
                                text=dataObj[k].node_name.substr(0,15)+"...";//名称为英文
                                isHover=true;
                            }
                            restT=s.rect(x0, y1-rectHeight/2, rectWidth, rectHeight, rectHeight / 2, rectHeight)
                                .attr({
                                    fill: "rgb(63, 170, 248)"
                                });
                            textT=s.text(x0 + rectPadding, y1+fontSize/ 2  - 2, text)
                                .attr({
                                    fill: "#ffffff",
                                    id:dataObj[k].node_name
                                });/*在此插件里，文字的起始点为左下角*/
                            gRect.add(
                                restT,
                                textT
                            );
                            g.add(gRect);

                            /*如果该数据库名称过长，需要hover来显示全称*/
                            if(isHover==true){
                                var textMHoverTipM;
                                gRect.hover(function(e){
                                    this.select("text").attr({
                                        class:"pointer"
                                    });
                                    this.select("rect").attr({
                                        class:"pointer"
                                    });
                                    var textAll=this.select("text").attr("id");
                                    textMHoverTipM=s.g();
                                    var mouseX = e.pageX-$("#svg").offset().left;
                                    var mouseY = e.pageY-$("#svg").offset().top-20;
                                    var tipBox=s.rect(mouseX,mouseY, 2*5+7* textAll.length, 20, 0,0)
                                        .attr({
                                            fill: "#ffffff",
                                            stroke:"#666666",
                                            strokeWidth:1,
                                            opacity:0.8
                                        });
                                    var tipText=s.text(mouseX+5,mouseY+14,textAll)
                                        .attr({
                                            fill: "#666666",
                                            opacity:0.8,
                                            style:"font-size:12px"
                                        });
                                    textMHoverTipM.add(tipBox,tipText);
                                },function(){
                                    textMHoverTipM.remove();
                                });

                            }


                            /*设置连线*/
                            //判断类型，是上直线还是下直线
                            if (Math.abs(y1 - y2) > 15) {
                                if(scope.pictureData.dataBaseS[k].node_type=="table"){
                                    var _trans = scope.pictureData.dataBaseS[k].transRules;
                                    var _clean =scope.pictureData.dataBaseS[k].cleanRules;
                                    //上直线三段
                                    //第一段
                                    var l1=s.line(x1, y1, x3, y3).attr({
                                        fill: "none",
                                        stroke: "rgb(63, 170, 248)",
                                        strokeWidth: 1
                                    });
                                    //第二段
                                    var l2;
                                    if(x1<x2){
                                        l2=s.path(curve(x3, y3, (2 * x3 + x2) / 3, (2 * y3 + y2) / 3, x2, y2)).attr({
                                            fill: "none",
                                            stroke: "rgb(63, 170, 248)",
                                            strokeWidth: 1
                                        });
                                    }
                                    else{
                                        l2=s.path(curve(x2, y2, (2 * x3 + x2) / 3, (2 * y3 + y2) / 3, x3, y3)).attr({
                                            fill: "none",
                                            stroke: "rgb(63, 170, 248)",
                                            strokeWidth: 1
                                        });
                                    }

                                    var gLE=s.g();
                                    var gLT=s.g();
                                    //给(x3,y3),((x3+x2)/2,(y3+y2)/2)标记圆圈
                                    var circleE=s.circle(x3, y3, circleR).attr({
                                        class:"pointer",
                                        fill: "#49d441"
                                    });

                                    var textE=s.text(x3 - 5, y3 + fontSize / 2 - 1, "E").attr({
                                        class:"pointer",
                                        fill: "#ffffff"
                                    });

                                    var circleT=s.circle((2 * x3 + x2) / 3, (2 * y3 + y2) / 3, circleR).attr({
                                        class:"pointer",
                                        fill: "#ffaf1d"
                                    });
                                    var textT=s.text((2 * x3 + x2) / 3 - fontSize / 2 + 3, (2 * y3 + y2) / 3 + fontSize / 2 - 1, "T").attr({
                                        class:"pointer",
                                        fill: "#ffffff"
                                    });

                                    //hover时，在svg后面添加E弹框
                                    gLE.add(circleE,textE).hover(function() {
                                        var x1=parseInt(this.select("circle").attr("cx"));
                                        var y1=parseInt(this.select("circle").attr("cy"));
                                        // console.log("typeof x1***********"+typeof x1);
                                        $("#svg").after("<div id='hover_gLE_angel' style='position:absolute;top:"+(y1 + fontSize)+"px;left:"+(x1 - 5)+"px'></div>");
                                        $("#svg").after("<div id='hover_gLE_box' style='position:absolute;top:"+(y1 + fontSize+8)+"px;left:"+(x1 - 5)+"px'><p>清洗规则：</p>"+_clean+"</div>");
                                    },function(){
                                        $("#svg").nextAll("div[id*='hover_gLE']").remove();
                                    });

                                    gLT.add(circleT,textT).hover(function() {
                                        var x1=parseInt(this.select("circle").attr("cx"));
                                        var y1=parseInt(this.select("circle").attr("cy"));
                                        // console.log("typeof x1***********"+typeof x1);
                                        $("#svg").after("<div id='hover_gLT_angel' style='position:absolute;top:"+(y1 + fontSize)+"px;left:"+(x1 - 5)+"px'></div>");
                                        $("#svg").after("<div id='hover_gLT_box' style='position:absolute;top:"+(y1 + fontSize+8)+"px;left:"+(x1 - 5)+"px'><p>转换规则：</p>"+_trans+"</div>");
                                    },function(){
                                        $("#svg").nextAll("div[id*='hover_gLT']").remove();
                                    });

                                    gL.add(l1,l2,gLE,gLT);
                                }
                                if(scope.pictureData.dataBaseS[k].node_type=="database"){
                                    var l;
                                    if(x1<x2) {
                                        l = s.path(curve(x1, y1, (x1 + x2) / 2, (y1 + y2) / 2, x2, y2)).attr({
                                            fill: "none",
                                            stroke: "rgb(63, 170, 248)",
                                            strokeWidth: 1
                                        });
                                    }
                                    else{
                                        l = s.path(curve(x2, y2, (x1 + x2) / 2, (y1 + y2) / 2, x1, y1)).attr({
                                            fill: "none",
                                            stroke: "rgb(63, 170, 248)",
                                            strokeWidth: 1
                                        });
                                    }
                                    gL.add(l);
                                }
                            }
                            else {
                                //水平直线
                                var l3=s.line(x1, y1, x2, y2).attr({
                                    fill: "none",
                                    stroke: "rgb(63, 170, 248)",
                                    strokeWidth: 1
                                });
                                var circleE2,circleT2,textE2,textT2;

                                if(scope.pictureData.dataBaseS[k].node_type=="table"){
                                    var _trans = scope.pictureData.dataBaseS[k].transRules;
                                    var _clean =scope.pictureData.dataBaseS[k].cleanRules;
                                    var gLT2=s.g();
                                    var gLE2=s.g();
                                    //给(x3,y3),((x3+x2)/2,(y3+y2)/2)标记圆圈
                                    circleE2=s.circle(x3 + 2, y3 + 2, circleR).attr({
                                        class:"pointer",
                                        fill: "#49d441"
                                    });

                                    circleT2=s.circle((2 * x3 + x2) / 3 + 2, (2 * y3 + y2) / 3 + 2, circleR).attr({
                                        class:"pointer",
                                        fill: "#ffaf1d"
                                    });
                                    textE2=s.text(x3 - 5, y3 + fontSize / 2 - 1, "E").attr({
                                        class:"pointer",
                                        fill: "#ffffff"
                                    });
                                    textT2=s.text((2 * x3 + x2) / 3 - fontSize / 2 + 3, (2 * y3 + y2) / 3 + fontSize / 2 - 1, "T").attr({
                                        class:"pointer",
                                        fill: "#ffffff"
                                    });

                                    //hover时，在svg后面添加E弹框
                                    gLE2.add(circleE2,textE2).hover(function() {
                                        var x2=parseInt(this.select("circle").attr("cx"));
                                        var y2=parseInt(this.select("circle").attr("cy"));
                                        console.log("typeof x2***********"+typeof x2);
                                        $("#svg").after("<div id='hover_gLE_angel' style='position:absolute;top:"+(y2 + fontSize)+"px;left:"+(x2 - 5)+"px'></div>");
                                        $("#svg").after("<div id='hover_gLE_box' style='position:absolute;top:"+(y2 + fontSize+8)+"px;left:"+(x2 - 5)+"px'><p>清洗规则：</p>"+_clean+"</div>");
                                    },function(){
                                        $("#svg").nextAll("div[id*='hover_gLE']").remove();
                                    });

                                    gLT2.add(circleT2,textT2).hover(function() {
                                        var x1=parseInt(this.select("circle").attr("cx"));
                                        var y1=parseInt(this.select("circle").attr("cy"));
                                        // console.log("typeof x1***********"+typeof x1);
                                        $("#svg").after("<div id='hover_gLT_angel' style='position:absolute;top:"+(y1 + fontSize)+"px;left:"+(x1 - 5)+"px'></div>");
                                        $("#svg").after("<div id='hover_gLT_box' style='position:absolute;top:"+(y1 + fontSize+8)+"px;left:"+(x1 - 5)+"px'><p>转换规则：</p>"+_trans+"</div>");
                                    },function(){
                                        $("#svg").nextAll("div[id*='hover_gLT']").remove();
                                    });

                                    gL.add(l3,gLE2,gLT2);
                                }
                                else{
                                    gL.add(l3);
                                }




                            }

                        }
                    }
                };

                scope.$watch('pictureData',function(newValue, oldValue) {
                    if (newValue != oldValue){
                        if (scope.pictureData != null && scope.pictureData.dataBaseM != undefined && scope.pictureData.dataBaseM.node_name) {
                            $("#svg").empty();//清空画布
                            /**********摆放中间数据库*************/
                                //var numberHM = scope.pictureData.dataBaseM.match(/[\u4E00-\u9FA5]/g).length;//汉字个数
                                //var numberEM = scope.pictureData.dataBaseM.length - numberHM;//除了汉字以外的字符个数
                                // var rectWidthM = rectPadding * 2 + fontSize * (numberHM + numberEM);
                            var rectWidthM = 150;
                            var textM=scope.pictureData.dataBaseM.node_name;
                            var textMHoverTipM;
                            var isHover=false;
                            if(/.*[\u4e00-\u9fa5]+.*$/.test(scope.pictureData.dataBaseM.node_name) && scope.pictureData.dataBaseM.node_name.length>6){
                                textM=scope.pictureData.dataBaseM.node_name.substr(0,6)+"...";//名称为汉字
                                isHover=true;
                            }

                            if(/^[a-zA-Z]/.test(scope.pictureData.dataBaseM.node_name)&& scope.pictureData.dataBaseM.node_name.length>15){
                                textM=scope.pictureData.dataBaseM.node_name.substr(0,15)+"...";//名称为英文
                                isHover=true;
                            }
                            //动画1
                            $timeout(function(){
                                var gM=s.g();
                                var gMRect=s.rect(sWidth / 2 - rectWidthM / 2, (sHeight - 2 * y0) / 2 - rectHeight / 2, rectWidthM, rectHeight, rectHeight / 2, rectHeight)
                                    .attr({
                                        fill: "rgb(63, 170, 248)",
                                        opacity:0
                                    })
                                    .animate({
                                        opacity:1
                                    },800);

                                var gMText=s.text(sWidth / 2 - rectWidthM / 2 + rectPadding, (sHeight - 2 * y0) / 2 + rectHeight / 2 - fontSize / 2, textM)
                                    .attr({
                                        fill: "#ffffff",
                                        opacity:0
                                    })
                                    .animate({
                                        opacity:1
                                    },800);
                                gM.add(gMRect,gMText);
                                //是否有弹框
                                if(isHover==true){
                                    gM.hover(function(e){
                                        //alert(this.attr("class"));
                                        this.select("text").attr({
                                            class:"pointer"
                                        });
                                        this.select("rect").attr({
                                            class:"pointer"
                                        });
                                        textMHoverTipM=s.g();
                                        var mouseX = e.pageX-$("#svg").offset().left;
                                        var mouseY = e.pageY-$("#svg").offset().top-20;
                                        var tipBox=s.rect(mouseX,mouseY, 2*5+7* scope.pictureData.dataBaseM.node_name.length, 20, 0,0)
                                            .attr({
                                                fill: "#ffffff",
                                                stroke:"#666666",
                                                strokeWidth:1,
                                                opacity:0.8
                                            });
                                        var tipText=s.text(mouseX+5,mouseY+14, scope.pictureData.dataBaseM.node_name)
                                            .attr({
                                                fill: "#666666",
                                                opacity:0.8,
                                                style:"font-size:12px"
                                            });
                                        textMHoverTipM.add(tipBox,tipText);
                                    },function(){
                                        textMHoverTipM.remove();
                                    });
                                }

                                /*中间数据库结束线*/
                                s.line(sWidth / 2, (sHeight - 2 * y0) / 2 + rectHeight / 2, sWidth / 2, (sHeight - 2 * y0) / 2 + 150)
                                    .attr({
                                        fill: "none",
                                        stroke: "#ff0000",
                                        strokeWidth: 2,
                                        opacity:0
                                    }).animate({
                                    opacity:1
                                },800);
                                s.circle(sWidth / 2, (sHeight - 2 * y0) / 2 + rectHeight / 2 + 60, circleR)
                                    .attr({
                                        fill: "#ff0000",
                                        opacity:0
                                    }).animate({
                                    opacity:1
                                },800);
                                s.text(sWidth / 2 - 4, (sHeight - 2 * y0) / 2 + rectHeight / 2 + 60 + fontSize / 2 - 1, "R")
                                    .attr({
                                        fill: "#ffffff",
                                        opacity:0
                                    }).animate({
                                    opacity:1
                                },800);
                                s.text(sWidth / 2 - fontSize, (sHeight - 2 * y0) / 2 + rectHeight / 2 + 160, "结束")
                                    .attr({
                                        fill: "#ff0000",
                                        opacity:0
                                    }).animate({
                                    opacity:1
                                },800);
                            },1);
                            /***********摆放中间数据库结束***********/

                            /************摆放源数据库长方形以及连线***********/
                            var gSource=s.g().attr({
                                opacity:0
                            });
                            var gSourceL= s.g().attr({
                                opacity:0
                            });
                            dataShow(scope.pictureData.dataBaseS,"dataBaseS",sWidth / 2 - rectWidthM / 2,gSource,gSourceL);

                            /*动画2*/
                            $timeout(function(){
                                gSource.animate({
                                    opacity:1
                                },800);
                            },810);
                            /*动画3*/
                            $timeout(function(){
                                gSourceL.animate({
                                    opacity:1
                                },800);
                            },1620);
                            /************摆放源数据库长方形以及连线结束***********/


                            /**************摆放目标数据库长方形*****************/
                            var gTarget=s.g().attr({
                                opacity:0
                            });
                            var gTargetL= s.g().attr({
                                opacity:0
                            });

                            dataShow(scope.pictureData.dataBaseT,"dataBaseT",sWidth / 2 + rectWidthM / 2,gTarget,gTargetL);

                            /*动画4*/
                            $timeout(function(){
                                gTargetL.animate({
                                    opacity:1
                                },800);
                            },2430);
                            /*动画5*/
                            $timeout(function(){
                                gTarget.animate({
                                    opacity:1
                                },800);
                            },3240);
                            /**************摆放目标数据库长方形结束*****************/
                        }
                        else{
                            $("#svg").empty();//清空画布
                            var text="无数据，请单击左侧树进行选择。";
                            textFirst=s.paper.text(sWidth/2-(text.length/2)*fontSize,50,text);
                        }
                    }
                });

                if(scope.pictureData == null || scope.pictureData.dataBaseM == undefined){
                    var text="无数据，请单击左侧树进行选择。";
                    textFirst=s.paper.text(sWidth/2-(text.length/2)*fontSize,50,text);
                }

            },
            restrict:'E',
            scope:{
                pictureData:'=',
                fontSize:'=?'
            },
            replace:true,
            template:'<svg id="svg"  xmlns="http://www.w3.org/2000/svg" version="1.0" width="100%" style="font-size:{{fontSize}}px"></svg>'

        }
    }]);
})(angular);
