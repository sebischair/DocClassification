'use strict';

angular.module('cApp', [
    'ngRoute',
    'ui.bootstrap',
    'cApp.pipeline',
    'cApp.classify'
])
.config(['$routeProvider', '$locationProvider', function($routeProvider, $locationProvider) {
    $routeProvider
        .when('/', {
            templateUrl: '/assets/components/home.html'
        });
        // Enable html5Mode in order to disable hashbanging
        $locationProvider.html5Mode({
            enabled: true,
            requireBase: false
        });
}]);