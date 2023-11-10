/**
 * Created by tuyadi on 2016/7/5.
 */
angular.module('xtgl.cdglTree', [])
    // declare our naïve directive
    .directive('cdglTreeBlank', function($document,$compile) {
        return {
            scope:{
                sort:'=ngModel'
            },
            restrict:'E',
            link:function(scope,element,attr){
                $document.ready(function(){
                    debugger;
                    //生成一个数组，用来计算空格数
                    if(scope.sort>1){
                        for(var j=2;j<=scope.sort;j++){
                            element.append($compile("<i class='childrenIndent'></i>")(scope));
                        }
                    }


                })



            }
        }
    })
    .directive('cdglTreeCheck', function($document,$compile) {
        return {
            scope:false,
            restrict:'A',
            link:function(scope,element,attr){
                element.on("click", function () {

                    if ($(this).attr("name") == "all") {
                        var isChecked = false;
                        if ($(this).prop("checked")) {
                            // alert("addAttr");
                            isChecked = $(this).prop("checked");
                            $(this).parents("thead").next("tbody").find("input").each(function () {
                                $(this).prop("checked", isChecked);
                            });
                            return true;
                        }
                        else {
                            // alert("removeAttr");
                            $(this).removeAttr("checked");
                            $(this).parents("thead").next("tbody").find("input").each(function () {
                                $(this).removeAttr("checked");
                            });
                            return true;
                        }
                    }
                    else {
                        var isCheckedB = false;
                        var thisName = $(this).attr("name").replace("_final","");//当前name属性
                        debugger;
                        if ($(this).prop("checked")) {
                            isCheckedB = $(this).prop("checked");
                            debugger;
                            $(this).parents("tbody").find("input").each(function () {
                                var obj1 = $(this).attr("name");
                                debugger;
                                if(obj1.indexOf(thisName) >= 0){
                                    debugger;
                                    $(this).prop("checked", isCheckedB);
                                }
                            });
                            return true;
                        }
                        else {
                            debugger;
                            $(this).removeAttr("checked");
                            $(this).parents("tbody").find("input").each(function () {
                                var obj2 = $(this).attr("name");
                                debugger;
                                if(obj2.indexOf(thisName) >= 0){
                                    debugger;
                                    $(this).removeAttr("checked");
                                }
                            });
                            return true;
                        }
                    }


                });
            }
        }
    })
    .directive('cdglTreeToggle', function($document,$compile) {
        return {
            scope: false,
            restrict: 'A',
            link: function (scope, element, attr) {
                  element.on("click",function(){
                      console.log("cdglTreeToggle");
                      var thisName = $(this).parent("tr").find("input").attr("name").replace("_final","");//去掉final后的当前name属性
                      debugger;
                      var isSuccess = false;
                      var isOpen = false;
                      //判断菜单是需要开的还是关
                      $(this).parent("tr").find("td").each(function (index) {
                          if (index == 1) {
                              $(this).find("i").each(function () {
                                  if ($(this).attr("class").indexOf("fa-caret-square-o-up") > 0) {
                                      isOpen = true;
                                      debugger;
                                  }
                                  if ($(this).attr("class").indexOf("fa-caret-square-o-down") > 0) {
                                      isOpen = false;
                                      debugger;
                                  }
                              });
                          }
                      })

                      //子菜单是需要开还是要关**************************************************
                      //关闭树************************有问题
                      if (isOpen == false) {
                          $(this).parent("tr").nextAll("tr").each(function () {
                              var obj1 = $(this).find("input").attr("name").replace("_final","");//去掉final后的当前name属性;
                              debugger;
                              if (obj1.indexOf(thisName) >= 0) {
                                  isSuccess = true;
                                  debugger;
                                  //改变图标
                                  debugger;
                                  $(this).find("i").each(function (index) {
                                      debugger;
                                      console.log("i.class="+$(this).attr("class"));
                                      debugger;
                                      if($(this).attr("class")){
                                          debugger;
                                          if($(this).attr("class").indexOf("fa-caret-square-o-down") >= 0){
                                              debugger;
                                              $(this).removeClass("fa-caret-square-o-down");
                                              $(this).addClass("fa-caret-square-o-up");
                                          }
                                          if($(this).attr("class").indexOf("fa-folder-open-o") >= 0){
                                              debugger;
                                              $(this).removeClass("fa-folder-open-o");
                                              $(this).addClass("fa-folder-o");
                                          }
                                      }

                                  })
                                  debugger;
                                  $(this).slideUp(1000);
                              }

                          });

                          //菜单是需要关
                          if (isSuccess == true) {
                              $(this).parent("tr").find("i").each(function (index) {
                                  if ($(this).attr("class").indexOf("fa-caret-square-o-down") >= 0) {
                                      debugger;
                                      $(this).removeClass("fa-caret-square-o-down");
                                      $(this).addClass("fa-caret-square-o-up");
                                  }
                                  if ($(this).attr("class").indexOf("fa-folder-open-o") >= 0) {
                                      debugger;
                                      $(this).removeClass("fa-folder-open-o");
                                      $(this).addClass("fa-folder-o");
                                  }
                              })
                          }
                      }
                      //关闭树结束****************************
                      //打开树***************************
                      if (isOpen == true) {
                          var thisNum = $(this).parent("tr").find("input").attr("name").replace("_final","").split("_").length;//当前菜单级数
                          var thisObj = $(this).parent("tr").find("input").attr("name").replace("_final","").split("_");//name切割字符串

                          $(this).parent("tr").nextAll("tr").each(function () {
                              var obj = $(this).find("input").attr("name").replace("_final","").split("_");
                              debugger;
                              if (obj.length - 1 == thisNum) {
                                  for (var i = 0; i < thisNum; i++) {
                                      if (i == thisNum - 1) {
                                          if (thisObj[i] == obj[i]) {
                                              isSuccess = true;
                                              debugger;
                                              $(this).slideDown(1000);
                                          }
                                          else {
                                              break;
                                          }
                                      }
                                      if (i < thisNum - 1) {
                                          if (thisObj[i] == obj[i]) {
                                              continue;
                                          }
                                          else {
                                              break;
                                          }
                                      }

                                  }
                              }

                          });

                          //菜单是需要开
                          if (isSuccess == true) {
                              $(this).parent("tr").find("i").each(function (index) {
                                  if($(this).attr("class")){
                                      if ($(this).attr("class").indexOf("fa-caret-square-o-up") >= 0) {
                                          debugger
                                          $(this).removeClass("fa-caret-square-o-up");
                                          $(this).addClass("fa-caret-square-o-down");
                                      }
                                      if ($(this).attr("class").indexOf("fa-folder-o") >= 0) {
                                          debugger
                                          $(this).removeClass("fa-folder-o");
                                          $(this).addClass("fa-folder-open-o");
                                      }
                                  }

                              })
                          }
                      }
                      //打开树结束***************************
                  });

            }
        }
    })

