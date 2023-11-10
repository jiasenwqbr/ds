/**
 * Created by tuyadi on 2016/10/27.
 */
//浏览器本地存储数据===================================
jdgpApp.service('locals', ['$window', function ($window) {
    return {
        //存储单个属性
        set: function (key, value) {
            $window.localStorage[key] = value;
        },
        //读取单个属性
        get: function (key) {
            return $window.localStorage[key] || '';
        },
        //存储对象，以JSON格式存储
        setObject: function (key, value) {
            $window.localStorage[key] = JSON.stringify(value);
        },
        //读取对象
        getObject: function (key) {
//            return JSON.parse($window.localStorage[key] || '{}');
            return JSON.parse($window.localStorage.getItem(key) || '{}');
        }
    }
}]);

//项目根路径地址
jdgpApp.service('jdgpPath', function ($http, $q, locals) {
    //获取当前网址，如： http://localhost:8083/uimcardprj/share/meun.jsp
    var curWwwPath = window.document.location.href;
    //获取主机地址之后的目录，如： uimcardprj/share/meun.jsp
    // var pathName = window.document.location.pathname;
    var pos = curWwwPath.indexOf("#");
    //获取主机地址，如： http://localhost:8083
    var localhostPath = curWwwPath.substring(0, pos - 1);
    //获取带"/"的项目名，如：/uimcardprj
    // var projectName = pathName.split("/")[1];
    var finalRoot0 = localhostPath;
    var modulePath = {};
    console.log("finalRoot:" + finalRoot0);

    $http.get(finalRoot0 + '/hosts').success(function (data) {
        locals.setObject("hosts", data);
    }).error(function () {
//        alert("数据错误！");
    });
    return {
        finalRoot: finalRoot0
    };
});

/*字符串超出显示...*/
angular.module('truncate', [])
    .filter('characters', function () {
        return function (input, chars, breakOnWord) {
            if (isNaN(chars)) return input;
            if (chars <= 0) return '';
            if (input && input.length > chars) {
                input = input.substring(0, chars);

                if (!breakOnWord) {
                    var lastspace = input.lastIndexOf(' ');
                    //get last space
                    if (lastspace !== -1) {
                        input = input.substr(0, lastspace);
                    }
                } else {
                    while (input.charAt(input.length - 1) === ' ') {
                        input = input.substr(0, input.length - 1);
                    }
                }
                return input + '…';
            }
            return input;
        };
    })
    .filter('splitcharacters', function () {
        return function (input, chars) {
            if (isNaN(chars)) return input;
            if (chars <= 0) return '';
            if (input && input.length > chars) {
                var prefix = input.substring(0, chars / 2);
                var postfix = input.substring(input.length - chars / 2, input.length);
                return prefix + '...' + postfix;
            }
            return input;
        };
    })
    .filter('words', function () {
        return function (input, words) {
            if (isNaN(words)) return input;
            if (words <= 0) return '';
            if (input) {
                var inputWords = input.split(/\s+/);
                if (inputWords.length > words) {
                    input = inputWords.slice(0, words).join(' ') + '…';
                }
            }
            return input;
        };
    });
/*
 angular.module("truncate", []).filter('cut', function () {
 return function (value, wordwise, max, tail) {
 if (!value) return '';

 max = parseInt(max, 10);
 if (!max) return value;
 if (value.length <= max) return value;

 value = value.substr(0, max);
 if (wordwise) {
 var lastspace = value.lastIndexOf(' ');
 if (lastspace != -1) {
 value = value.substr(0, lastspace);
 }
 }

 return value + (tail || ' …');
 };
 });*/
