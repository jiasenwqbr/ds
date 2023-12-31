/**
 * UI Component For Creating Cron Job Syntax To Send To Server
 * @version v3.2.0 - 2016-09-20 * @link https://github.com/jacobscarter/angular-cron-jobs
 * @author Jacob Carter <jc@jacobcarter.com>
 * @license MIT License, http://www.opensource.org/licenses/MIT
 */
angular.module('templates-angularcronjobs', ['cronselection.html']);

angular.module("cronselection.html", []).run(["$templateCache", function($templateCache) {
  $templateCache.put("cronselection.html",
    "<div class=\"cron-wrap\">\n" +
    "    <span>每: </span>\n" +
    "    <div class=\"cron-select-wrap\">\n" +
    "        <select class=\"cron-select\" ng-model=\"myFrequency.base\" ng-options=\"item.value as item.label for item in frequency\"></select>\n" +
    "    </div>\n" +
    "    <div class=\"select-options\">\n" +
    "        <span ng-show=\"myFrequency.base == 4\">周 </span>\n" +
    "        <div ng-show=\"myFrequency.base == 4\" class=\"cron-select-wrap\">\n" +
    "            <!-- If Multiple is Enabled -->\n" +
    "            <select class=\"cron-select day-value\"\n" +
    "                    ng-model=\"myFrequency.dayValues\"\n" +
    "                    ng-if=\"allowMultiple\" multiple\n" +
    "                    ng-options=\"value as (value | cronDayName: cronStyle) for value in dayValues\">\n" +
    "            </select>\n" +
    "            <!-- If Multiple is not Enabled -->\n" +
    "            <select class=\"cron-select day-value\"\n" +
    "                    ng-model=\"myFrequency.dayValues\"\n" +
    "                    ng-if=\"!allowMultiple\"\n" +
    "                    ng-options=\"value as (value | cronDayName: cronStyle) for value in dayValues\">\n" +
    "            </select>\n" +
    "        </div>\n" +
    "        <span ng-show=\"myFrequency.base == 6\">月 </span>\n" +
    "        <!-- If Multiple is Enabled -->\n" +
    "        <div ng-show=\"myFrequency.base == 6\" class=\"cron-select-wrap\">\n" +
    "            <select class=\"cron-select month-value\"\n" +
    "                    ng-model=\"myFrequency.monthValues\"\n" +
    "                    ng-if=\"allowMultiple\" multiple\n" +
    "                    ng-options=\"value as (value | cronMonthName) for value in monthValues\">\n" +
    "            </select>\n" +
    "            <!-- If Multiple is not Enabled -->\n" +
    "            <select class=\"cron-select month-value\"\n" +
    "                    ng-model=\"myFrequency.monthValues\"\n" +
    "                    ng-if=\"!allowMultiple\"\n" +
    "                    ng-options=\"value as (value | cronMonthName) for value in monthValues\">\n" +
    "            </select>\n" +
    "        </div>\n" +
    "        <span ng-show=\"myFrequency.base >= 5\">日 </span>\n" +
    "        <div ng-show=\"myFrequency.base >= 5\" class=\"cron-select-wrap\">\n" +
    "            <!-- If Multiple is Enabled -->\n" +
    "            <select class=\"cron-select day-of-month-value\"\n" +
    "                    ng-model=\"myFrequency.dayOfMonthValues\"\n" +
    "                    ng-if=\"allowMultiple\" multiple\n" +
    "                    ng-options=\"value as (value | cronNumeral) for value in dayOfMonthValues\">\n" +
    "            </select>\n" +
    "            <!-- If Multiple is not Enabled -->\n" +
    "            <select class=\"cron-select day-of-month-value\"\n" +
    "                    ng-model=\"myFrequency.dayOfMonthValues\"\n" +
    "                    ng-if=\"!allowMultiple\"\n" +
    "                    ng-options=\"value as (value | cronNumeral) for value in dayOfMonthValues\">\n" +
    "            </select>\n" +
    "        </div>\n" +
    "        <span ng-show=\"myFrequency.base >= 2\">{{myFrequency.base==2?'分': ''}} </span>\n" +
    "        <!-- If Multiple is Enabled -->\n" +
    "        <div ng-show=\"myFrequency.base >= 3\" class=\"cron-select-wrap\">\n" +
    "            <select class=\"cron-select hour-value\"\n" +
    "                    ng-model=\"myFrequency.hourValues\"\n" +
    "                    ng-if=\"allowMultiple\" multiple\n" +
    "                    ng-options=\"value as value for value in hourValues\">\n" +
    "            </select>\n" +
    "            <!-- If Multiple is not Enabled -->\n" +
    "            <select class=\"cron-select hour-value\"\n" +
    "                    ng-model=\"myFrequency.hourValues\"\n" +
    "                    ng-if=\"!allowMultiple\"\n" +
    "                    ng-options=\"value as value for value in hourValues\">\n" +
    "            </select>\n" +
    "        </div>\n" +
    "        <span ng-show=\"myFrequency.base >= 3\"> : </span>\n" +
    "        <!-- If Multiple is Enabled -->\n" +
    "        <div ng-show=\"myFrequency.base >= 2\" class=\"cron-select-wrap\">\n" +
    "            <select class=\"cron-select minute-value\"\n" +
    "                    ng-model=\"myFrequency.minuteValues\"\n" +
    "                    ng-if=\"allowMultiple\" multiple\n" +
    "                    ng-options=\"value as value for value in minuteValues\">\n" +
    "            </select>\n" +
    "            <!-- If Multiple is not Enabled -->\n" +
    "            <select class=\"cron-select minute-value\"\n" +
    "                    ng-model=\"myFrequency.minuteValues\"\n" +
    "                    ng-if=\"!allowMultiple\"\n" +
    "                    ng-options=\"value as value for value in minuteValues\">\n" +
    "            </select>\n" +
    "        </div>\n" +
    "        <span ng-show=\"myFrequency.base == 2\"></span>\n" +
    "    </div>\n" +
    "</div>");
}]);

"use strict";

angular.module("angular-cron-jobs", ["templates-angularcronjobs"]);

angular.module("angular-cron-jobs").directive("cronSelection", ["cronService", "baseFrequency", function(cronService, baseFrequency) {
    return {
        restrict: "EA",
        replace: true,
        transclude: true,
        require: "ngModel",
        scope: {
            cronStyle:"=",
            ngModel: "=",
            config: "=",
            myFrequency: "=?frequency"
        },
        templateUrl: function(element, attributes) {
            return attributes.template || "cronselection.html";
        },
        link: function($scope, $el, $attr, $ngModel) {
            var modelChanged = false;
            $scope.cronStyle="quartz";// by hht default quartz
            $scope.baseFrequency = baseFrequency;
            $scope.frequency = [{
                value: 1,
                label: "分钟"
            }, {
                value: 2,
                label: "小时"
            }, {
                value: 3,
                label: "天"
            }, {
                value: 4,
                label: "周"
            }, {
                value: 5,
                label: "月"
            }, {
                value: 6,
                label: "年"
            }];

            $scope.$watch("ngModel", function (newValue) {
                if (angular.isDefined(newValue) && newValue) {
                    modelChanged = true;
                    $scope.myFrequency = cronService.fromCron(newValue, $scope.allowMultiple, $scope.cronStyle);
                } else if (newValue === "") {
                    $scope.myFrequency = undefined;
                }
            });

            if (typeof $scope.config === "object" && !$scope.config.length) {
                if (typeof $scope.config.options === "object") {
                    var optionsKeyArray = Object.keys($scope.config.options);
                    for (var i in optionsKeyArray) {
                        var currentKey = optionsKeyArray[i].replace(/^allow/, "");
                        var originalKey = optionsKeyArray[i];
                        if (!$scope.config.options[originalKey]) {
                            for (var b in $scope.frequency) {
                                if ($scope.frequency[b].label === currentKey) {
                                    $scope.frequency.splice(b, 1);
                                }
                            }
                        }
                    }
                }
                if (angular.isDefined($scope.config.allowMultiple)) {
                    $scope.allowMultiple = $scope.config.allowMultiple;
                } else {
                    $scope.allowMultiple = false;
                }

                if (angular.isDefined($scope.config.quartz) && $scope.config.quartz) {
                    $scope.cronStyle = "quartz";
                } else {
                    $scope.cronStyle = "quartz";
//                    $scope.cronStyle = "default";
                }
            }

            $scope.minuteValues = [0, 5, 10, 15, 20, 25, 30, 35, 40, 45, 50, 55];
            $scope.hourValues = [0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23];
            $scope.dayOfMonthValues = [1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31];
            $scope.dayValues = [0, 1, 2, 3, 4, 5, 6];
            $scope.monthValues = [1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12];

			if($scope.cronStyle === "quartz") {
                $scope.dayValues = [1, 2, 3, 4, 5, 6, 7];
            }

            $scope.$watch("myFrequency", function (n, o) {
                if (n !== undefined) {
                    if (n && n.base && (!o || n.base !== o.base) && !modelChanged) {
                        setInitialValuesForBase(n);
                    } else if (n && n.base && o && o.base) {
                        modelChanged = false;
                    }
                    
                    var newVal = cronService.setCron(n, $scope.cronStyle);
                    $ngModel.$setViewValue(newVal);
                }
            }, true);

            function setInitialValuesForBase(freq) {
                freq.base = parseInt(freq.base);

                if (freq.base >= baseFrequency.hour) {
                    freq.minuteValues = $scope.minuteValues[0];
                }

                if (freq.base >= baseFrequency.day) {
                    freq.hourValues = $scope.hourValues[0];
                }

                if (freq.base === baseFrequency.week) {
                    freq.dayValues = $scope.dayValues[0];
                }

                if (freq.base >= baseFrequency.month) {
                    freq.dayOfMonthValues = $scope.dayOfMonthValues[0];
                }

                if (freq.base === baseFrequency.year) {
                    freq.monthValues = $scope.monthValues[0];
                }
            }
        }
    };
}]).filter("cronNumeral", function() {
    return function(input) {
        switch (input) {
            case 1:
                return "1";
            case 2:
                return "2";
            case 3:
                return "3";
            case 21:
                return "21";
            case 22:
                return "22";
            case 23:
                return "23";
            case 31:
                return "31";
            case null:
                return null;
            default:
                return input + "";
        }
    };
}).filter("cronMonthName", function() {
    return function(input) {
        var months = {
            1: "一月",
            2: "二月",
            3: "三月",
            4: "四月",
            5: "五月",
            6: "六月",
            7: "七月",
            8: "八月",
            9: "九月",
            10: "十月",
            11: "十一月",
            12: "十二月"
        };

        if (input !== null && angular.isDefined(months[input])) {
            return months[input];
        } else {
            return null;
        }
    };
}).filter("cronDayName", function() {
    return function(input, cronType) {
        var days;
        cronType="quartz";
        if(cronType === "quartz") {
            days = {
                1: "日",
                2: "一",
                3: "二",
                4: "三",
                5: "四",
                6: "五",
                7: "六",
            };
        } else {
            days = {
                0: "日",
                1: "一",
                2: "二",
                3: "三",
                4: "四",
                5: "五",
                6: "六",
            };
        }
        

        if (input !== null && angular.isDefined(days[input])) {
            return days[input];
        } else {
            return null;
        }
    };
}).directive("ngMultiple", function() {
    return {
        restrict: "A",
        scope: {
            ngMultiple: "="
        },
        link: function (scope, element) {
            var unwatch = scope.$watch("ngMultiple", function(newValue) {
                if (newValue) {
                    element.attr("multiple", "multiple");
                } else {
                    element.removeAttr("multiple");
                }
            });
        }
    };
});
"use strict";

angular.module('angular-cron-jobs')
.value('baseFrequency', {
    minute: 1,
    hour: 2,
    day: 3,
    week: 4,
    month: 5,
    year: 6
})
.factory('cronService', ['baseFrequency', function(baseFrequency) {
    var service = {};

    service.setCron = function(n, cronType) {
        cronType="quartz";
        if(cronType === "quartz") {
            return this.setQuartzCron(n);
        } else {
            return this.setDefaultCron(n);
        }
    };

    service.setQuartzCron = function(n){
        var cron = ["0", "*", "*",  "*",  "*", "?"];
        if(n && n.base && n.base >= baseFrequency.hour) {
            cron[1] = typeof n.minuteValues !== "undefined" ? n.minuteValues : "0";
        }

        if(n && n.base && n.base >= baseFrequency.day) {
            cron[2] = typeof n.hourValues !== "undefined" ? n.hourValues  : "*";
        }

        if(n && n.base && n.base === baseFrequency.week) {
            cron[3] = "?";
            cron[5] = n.dayValues;
        }

        if(n && n.base && n.base >= baseFrequency.month) {
            cron[3] = typeof n.dayOfMonthValues !== "undefined" ? n.dayOfMonthValues : "?";
        }

        if(n && n.base && n.base === baseFrequency.year) {
            cron[4] = typeof n.monthValues !== "undefined" ? n.monthValues : "*";
        }
        
        return cron.join(" ");
    };

    service.setDefaultCron = function(n){
        var cron = ["*", "*", "*", "*", "*"];

        if (n && n.base && n.base >= baseFrequency.hour) {
            cron[0] = typeof n.minuteValues !== "undefined" ? n.minuteValues : "*";
        }

        if (n && n.base && n.base >= baseFrequency.day) {
            cron[1] = typeof n.hourValues !== "undefined" ? n.hourValues : "*";
        }

        if (n && n.base && n.base === baseFrequency.week) {
            cron[4] = n.dayValues;
        }

        if (n && n.base && n.base >= baseFrequency.month) {
            cron[2] = typeof n.dayOfMonthValues !== "undefined" ? n.dayOfMonthValues : "*";
        }

        if (n && n.base && n.base === baseFrequency.year) {
            cron[3] = typeof n.monthValues !== "undefined" ? n.monthValues : "*";
        }
        return cron.join(" ");
    };

	service.fromCron = function(value, allowMultiple, cronType) {
        cronType="quartz";
        if(cronType === "quartz") {
            return this.fromQuartzCron(value, allowMultiple);
        } else {
            return this.fromDefaultCron(value, allowMultiple);
        }
    };

    service.fromDefaultCron = function(value, allowMultiple) {
        var cron = value.replace(/\s+/g, " ").split(" ");
        var frequency = { base: "1" }; // default: every minute
        var tempArray = [];

        if (cron[0] === "*" && cron[1] === "*" && cron[2] === "*" && cron[3] === "*" && cron[4] === "*") {
            frequency.base = baseFrequency.minute; // every minute
        } else if (cron[1] === "*" && cron[2] === "*" && cron[3] === "*" && cron[4] === "*") {
            frequency.base = baseFrequency.hour; // every hour
        } else if (cron[2] === "*" && cron[3] === "*" && cron[4] === "*") {
            frequency.base = baseFrequency.day; // every day
        } else if (cron[2] === "*" && cron[3] === "*") {
            frequency.base = baseFrequency.week; // every week
        } else if (cron[3] === "*" && cron[4] === "*") {
            frequency.base = baseFrequency.month; // every month
        } else if (cron[4] === "*") {
            frequency.base = baseFrequency.year; // every year
        }

        if (cron[0] !== "*") {
            //preparing to handle multiple minutes
            if (allowMultiple) {
                tempArray = cron[0].split(',');
                for (var i = 0; i < tempArray.length; i++) { tempArray[i] = +tempArray[i]; }
                frequency.minuteValues = tempArray;
            } else {
                frequency.minuteValues = parseInt(cron[0]);
            }
        }
        if (cron[1] !== "*") {
            //preparing to handle multiple hours
            if (allowMultiple) {
                tempArray = cron[1].split(",");
                for (var i = 0; i < tempArray.length; i++) { tempArray[i] = +tempArray[i]; }
                frequency.hourValues = tempArray;
            } else {
                frequency.hourValues = parseInt(cron[1]);
            }
        }
        if (cron[2] !== "*") {
            //preparing to handle multiple days of the month
            if (allowMultiple) {
                tempArray = cron[2].split(",");
                for (var i = 0; i < tempArray.length; i++) { tempArray[i] = +tempArray[i]; }
                frequency.dayOfMonthValues = tempArray;
            } else {
                frequency.dayOfMonthValues = parseInt(cron[2]);
            }
        }
        if (cron[3] !== "*") {
            //preparing to handle multiple months
            if (allowMultiple) {
                tempArray = cron[3].split(",");
                for (var i = 0; i < tempArray.length; i++) { tempArray[i] = +tempArray[i]; }
                frequency.monthValues = tempArray;
            } else {
                frequency.monthValues = parseInt(cron[3]);
            }
        }
        if (cron[4] !== "*") {
            //preparing to handle multiple days of the week
            if (allowMultiple) {
                tempArray = cron[4].split(",");
                for (var i = 0; i < tempArray.length; i++) { tempArray[i] = +tempArray[i]; }
                frequency.dayValues = tempArray;
            } else {
                frequency.dayValues = parseInt(cron[4]);
            }
        }
        return frequency;
    };

    service.fromQuartzCron = function(value, allowMultiple) {
        var cron = value.replace(/\s+/g, " ").split(" ");
        var frequency = {base: "1"}; // default: every minute
        var tempArray = [];
        
        if(cron[1] === "*" && cron[2] === "*" && cron[3] === "*"  && cron[4] === "*" && cron[5] === "?") {
            frequency.base = 1; // every minute
        } else if(cron[2] === "*" && cron[3] === "*"  && cron[4] === "*" && cron[5] === "?") {
            frequency.base = 2; // every hour
        } else if(cron[3] === "*"  && cron[4] === "*" && cron[5] === "?") {
            frequency.base = 3; // every day
        } else if(cron[3] === "?") {
            frequency.base = 4; // every week
        } else if(cron[4] === "*" && cron[5] === "?") {
            frequency.base = 5; // every month
        } else if(cron[5] === "?") {
            frequency.base = 6; // every year
        }

        if (cron[1] !== "*") {
            //preparing to handle multiple minutes
            if (allowMultiple) {
                tempArray = cron[1].split(",");
                for (var i = 0; i < tempArray.length; i++) { tempArray[i] = +tempArray[i]; }
                frequency.minuteValues = tempArray;
            } else {
                frequency.minuteValues = parseInt(cron[1]);
            }
        }
        if (cron[2] !== "*") {
            //preparing to handle multiple hours
            if (allowMultiple) {
                tempArray = cron[2].split(",");
                for (var i = 0; i < tempArray.length; i++) { tempArray[i] = +tempArray[i]; }
                frequency.hourValues = tempArray;
            } else {
                frequency.hourValues = parseInt(cron[2]);
            }
        }
        if (cron[3] !== "*" && cron[3] !== "?") {
            //preparing to handle multiple days of the month
            if (allowMultiple) {
                tempArray = cron[3].split(",");
                for (var i = 0; i < tempArray.length; i++) { tempArray[i] = +tempArray[i]; }
                frequency.dayOfMonthValues = tempArray;
            } else {
                frequency.dayOfMonthValues = parseInt(cron[3]);
            }
        }
        if (cron[4] !== "*") {
            //preparing to handle multiple months
            if (allowMultiple) {
                tempArray = cron[4].split(",");
                for (var i = 0; i < tempArray.length; i++) { tempArray[i] = +tempArray[i]; }
                frequency.monthValues = tempArray;
            } else {
                frequency.monthValues = parseInt(cron[4]);
            }
        }
        if (cron[5] !== "*" && cron[5] !== "?") {
            //preparing to handle multiple days of the week
            if (allowMultiple) {
                tempArray = cron[5].split(",");
                for (var i = 0; i < tempArray.length; i++) { tempArray[i] = +tempArray[i]; }
                frequency.dayValues = tempArray;
            } else {
                frequency.dayValues = parseInt(cron[5]);
            }
        }

        return frequency;
    };

    return service;
}]);