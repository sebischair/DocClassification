'use strict';

var pipelineApp = angular.module('cApp.pipeline', ['ngRoute']);

pipelineApp.factory('PipelineDataService', ['$http', function PipelineDataService($http) {
    var getPipelines = function() {
        return $http.get('/pipeline/getAll')
            .then(function (response) {
                return response.data;
            });
    };
    var getPipeline = function(name) {
        return $http.get('/pipeline/get?name='+name)
            .then(function (response) {
                return response.data;
            });
    };
    return {
        getPipelines: getPipelines,
        getPipeline: getPipeline
    };
}]);

pipelineApp.config(['$routeProvider', function($routeProvider) {
    $routeProvider.when('/pipelines', {
        templateUrl: '/assets/components/pipeline/pipeline.html',
        controller: 'PipelineCtrl',
        controllerAs: 'vm',
        resolve: {
            pipelines: function(PipelineDataService) {
                return PipelineDataService.getPipelines();
            }
        }
    })
    .when('/configurePipeline/:param', {
        templateUrl: '/assets/components/pipeline/configurePipeline.html',
        controller: 'ConfigurePipelineCtrl',
        controllerAs: 'vm',
        resolve: {
            pipeline: function(PipelineDataService, $route) {
                return PipelineDataService.getPipeline($route.current.params.param);
            }
        }
    });
}]);

pipelineApp.controller('PipelineCtrl', ['pipelines', '$http', '$location', function(pipelines, $http, $location) {
    var self = this;
    self.pipelines = JSON.parse(pipelines.result);

    self.createPipeline = function() {
        if(self.pipelineName === undefined || self.pipelineName.length == 0) {
            self.message = "Please provide a name!"
        } else {
            for(var i in self.pipelines) {
                if(self.pipelines[i].name === self.pipelineName) {
                    self.message = "Please use a different name! Pipeline already exists";
                    return;
                }
            }

            //create a new pipeline
            $http.get('/pipeline/create?name=' + self.pipelineName).
            then(function(response) {
                self.pipelines = JSON.parse(response.data.result);
            });
        }
    };

    self.removePipeline = function(pipelineName) {
        //remove a pipeline
        $http.get('/pipeline/remove?name=' + pipelineName).
        then(function(response) {
            self.pipelines = JSON.parse(response.data.result);
        });
    }

}]);

pipelineApp.controller('ConfigurePipelineCtrl', ['pipeline', '$http', function(pipeline, $http) {
    var self = this;
    self.pipeline = JSON.parse(pipeline.result);
    self.createLabelFlag = false;

    self.newLabel = function() {
        self.createLabelFlag = true;
    };

    self.cancelCreateLabel = function() {
        self.labelName = "";
        self.labelPath = "";
        self.createLabelFlag = false;
    }

    self.createLabel = function() {
        if(self.labelName === undefined || self.labelName.length == 0) {
            self.message = "Please provide a name!"
        } else if (self.labelPath === undefined || self.labelPath.length == 0) {
            self.message = "Please provide the path to a directory!"
        } else {
            for(var i in self.pipeline.labels) {
                if(self.pipeline.labels[i].name === self.labelName) {
                    self.message = "Please use a different name! Pipeline already exists";
                    return;
                }
            }

            var data = {};
            data.pipelineName = self.pipeline.name;
            data.labelName = self.labelName;
            data.labelPath = self.labelPath;

            //add a new label
            $http.post('/label/create', data).
            then(function(response) {
                self.pipeline = JSON.parse(response.data.result);
                self.labelName = "";
                self.labelPath = "";
                self.createLabelFlag = false;
            });
        }
    };

    self.removeLabel = function(labelName, labelPath) {
        //remove a label
        var data = {};
        data.pipelineName = self.pipeline.name;
        data.labelName = labelName;
        data.labelPath = labelPath;

        $http.post('/label/remove', data).
        then(function(response) {
            self.pipeline = JSON.parse(response.data.result);
        });
    };

    self.trainDocuments = function() {
        $http.get('/pipeline/train?name=' + self.pipeline.name).
        then(function(response) {
            self.results = response.data.result;

            var noLabels = self.pipeline.labels.length;
            var matrix = self.results.confusion_matrix;

            self.cmatrix = {};
            for(var i=0; i<noLabels; i++) {
                var arr = [];
                var count = i;
                while(count  < noLabels * noLabels){
                    arr.push(matrix[count]);
                    count = count + noLabels;
                }
                self.cmatrix[self.pipeline.labels[i].name] = arr;
            }
            self.showResults = true;
        });
    }

}]);