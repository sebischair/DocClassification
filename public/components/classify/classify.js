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

classifyApp.controller('ExecutePipelineCtrl', ['scAuth', 'scData', 'scModel', 'pipeline', '$http', '$q', function(scAuth, scData, scModel, pipeline, $http, $q) {
    var self = this;
    self.pipeline = JSON.parse(pipeline.result);
    self.showResults = false;
    self.textToClassify = "";
    self.isPredicting = false;

    self.classifyDocuments = function() {
        if(self.textToClassify === "") {
            self.message = "Please provide the text to classify!"
        } else {
            self.isPredicting = true;
            var data = {};
            data.pipelineName = self.pipeline.name;
            data.textToClassify = self.textToClassify;
            $http.post('/pipeline/predict', data).
            then(function(response) {
                self.result = response.data.result;
                self.showResults = true;
                self.isPredicting = false;
            });
        }
    };
}]);