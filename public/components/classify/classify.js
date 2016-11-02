'use strict';

var classifyApp = angular.module('cApp.classify', ['ngRoute']);

classifyApp.config(['$routeProvider', function($routeProvider) {
    $routeProvider.when('/classifyDocuments', {
        templateUrl: '/assets/components/classify/classify.html',
        controller: 'ClassifyCtrl',
        controllerAs: 'vm',
        resolve: {
            pipelines: function(PipelineDataService) {
                return PipelineDataService.getPipelines();
            }
        }
    })
    .when('/executePipeline/:param', {
        templateUrl: '/assets/components/classify/executePipeline.html',
        controller: 'ExecutePipelineCtrl',
        controllerAs: 'vm',
        resolve: {
            pipeline: function(PipelineDataService, $route) {
                return PipelineDataService.getPipeline($route.current.params.param);
            }
        }
    });
}]);

classifyApp.controller('ClassifyCtrl', ['pipelines', '$http', function(pipelines, $http) {
    var self = this;
    self.pipelines = JSON.parse(pipelines.result);
}]);

classifyApp.controller('ExecutePipelineCtrl', ['pipeline', '$http', function(pipeline, $http) {
    var self = this;
    self.pipeline = JSON.parse(pipeline.result);
    self.showResults = false;

    self.classifyDocuments = function() {
        if(self.documentsPath === undefined || self.documentsPath.length == 0) {
            self.message = "Please provide the path to a directory!"
        } else {
            var data = {};
            data.pipelineName = self.pipeline.name;
            data.documentsPath = self.documentsPath;

            $http.post('/pipeline/predict', data).
            then(function(response) {
                console.log(response.data);
                self.results = response.data.result;
                self.showResults = true;
            });
        }
    }

}]);