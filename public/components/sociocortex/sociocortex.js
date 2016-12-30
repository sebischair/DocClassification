'use strict';

var scApp = angular.module('cApp.sc', ['ngRoute']);

scApp.config(['$routeProvider', function($routeProvider) {
    $routeProvider.when('/linkSC', {
            controller: 'SCCtrl',
            controllerAs: 'vm'
        });
}]);