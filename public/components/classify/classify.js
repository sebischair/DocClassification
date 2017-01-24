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
    self.workspace = "";
    self.rootEntity = null;
    self.waitForSc = false;
    self.resultMap = [];

    self.linkWorkspace = function() {
        self.waitForSc = true;
        if(self.workspace != "") {
            scData.Workspace.get({id: self.workspace}, function (workspace) {
                self.rootEntity = workspace.rootEntity.href;
                self.waitForSc = false;
            });
        }
    };

    self.classifyDocuments = function() {
        if((self.documentsPath === undefined || self.documentsPath.length == 0) && self.workspace == "") {
            self.message = "Please provide the path to a directory or link a workspace!"
        } else {
            var data = {};
            data.pipelineName = self.pipeline.name;
            if(self.workspace == "") {
                data.documentsPath = self.documentsPath;
            } else {
                data.documentsPath = self.rootEntity;
            }
            $http.post('/pipeline/predict', data).
            then(function(response) {
                response.data.result.forEach(function(result) {
                    if(result.text.indexOf("api/v1/") > 0) {
                        var r = result.text.split(" ---- ");
                        result.text = r[1];
                        var entityId = self.pipeline.labels[result.prediction].path.split("entities/")[1];
                        var fileId = r[0].split("files/")[1];
                        self.resultMap.push({key: fileId, value: entityId})
                    }
                });
                self.results = response.data.result;
                self.showResults = true;
            });
        }
    };

    self.linkSC = function() {
        scAuth.login("manoj5864@gmail.com", "@Sebis5864");
        scData.Workspace.query(function (workspaces) {
            self.workspaces = workspaces;
        });
    };

    self.updateComplete = false;
    self.updateFilesInSC = function() {
        var p = self.resultMap.forEach(function(result) {
            scData.File.update({
                id: result.key,
                entity: {
                    id: result.value
                }
            });
        });
        $q.all(p).then(function() {
            self.updateComplete = true;
        });
    }
}]);