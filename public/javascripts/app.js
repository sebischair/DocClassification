'use strict';

angular.module('cApp', [
    'sociocortex',
    'ngRoute',
    'ngStorage',
    'ui.bootstrap',
    'cApp.pipeline',
    'cApp.classify',
    'checklist-model'
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