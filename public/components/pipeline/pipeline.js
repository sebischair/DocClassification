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
    var getClassifiers = function() {
        return $http.get('/classifiers')
            .then(function (response) {
                return response.data;
            });
    };
    return {
        getPipelines: getPipelines,
        getPipeline: getPipeline,
        getClassifiers: getClassifiers
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
            }, classifiers: function(PipelineDataService) {
                return PipelineDataService.getClassifiers();
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

pipelineApp.controller('ConfigurePipelineCtrl', ['scAuth', 'scData', 'scModel', 'pipeline', 'classifiers', '$http', function(scAuth, scData, scModel, pipeline, classifiers, $http) {
    var self = this;
    self.pipeline = JSON.parse(pipeline.result);
    self.classifiers = JSON.parse(classifiers.result);
    self.createLabelFlag = true;
    self.workspace = "";
    self.classifier = "";
    self.isTraining = false;
    self.fileName = "";
    self.typeCheckBox = null;
    self.pageCheckBox = null;
    self.uploadedDocument = false;

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
        } /*else if (self.labelPath === undefined || self.labelPath.length == 0) {
            self.message = "Please provide the path to a directory!"
        }*/ else {
            for(var i in self.pipeline.labels) {
                if(self.pipeline.labels[i].name === self.labelName) {
                    self.message = "Please use a different name! Label already exists";
                    return;
                }
            }

            var data = {};
            data.pipelineName = self.pipeline.name;
            data.labelName = self.labelName;
            data.labelPath = self.labelPath;
            data.labelId = self.labelName;
            data.labelType = "customType";
            //add a new label
            $http.post('/label/create', data).
            then(function(response) {
                self.pipeline = JSON.parse(response.data.result);
                self.labelName = "";
                self.labelPath = "";
            });
        }
    };

    self.removeLabel = function(label) {
        //remove a label
        var data = {};
        data.pipelineName = self.pipeline.name;
        data.labelName = label.name;
        data.labelPath = label.path;
        data.labelId = label.labelId;
        data.labelType = label.type;

        $http.post('/label/remove', data).
        then(function(response) {
            self.pipeline = JSON.parse(response.data.result);
        });
    };

    self.trainDocuments = function() {
        self.isTraining = true;
        $http.get('/pipeline/train?name=' + self.pipeline.name).
        then(function(response) {
            self.result = response.data.result;
            self.showResults = true;
            self.isTraining = false;
        });
    };

    self.updateClassifier = function() {
        var data = {};
        data.pipelineName = self.pipeline.name;
        data.classifierName = self.classifier;
        $http.post('/pipeline/classifier', data);
    };

    self.linkSC = function() {
        scData.Workspace.query(function (workspaces) {
            self.workspaces = workspaces;
        });
    };

    self.getPages = function() {
        self.pages = [];
        self.attributeType = "Page";
        scData.Workspace.get({ id: self.workspace}, function (workspace) {
            scData.Entity.get({id: workspace.rootEntity.id}, function(entity) {
                entity.children.forEach(function(subpage) {
                    if(subpage.name.indexOf(".") < 0) {
                        self.pages.push(subpage);
                    }
                });
            });
        });

        self.types= [];
        scData.Workspace.getEntityTypes({ id: self.workspace}, function (types) {
            self.types = types;
        });
    };

    self.getAttributes = function(type) {
        self.attributes= [];
        scModel.EntityType.getAttributeDefinitions({ id: type.id}, function (attributes) {
            self.attributes = attributes;
        });
    };

    self.getAttributeValues = function(attributeId) {
        self.values = [];
        self.attributeType = null;
        scModel.AttributeDefinition.get({id: attributeId}, function(attribute) {
            self.label = attribute.name;
            if(attribute.attributeType === "Boolean") {
                self.attributeType = "Boolean";
                self.values = [0, 1];
            } else if(attribute.attributeType === "Link"){
                self.attributeType = "Link";
                var linkedId = attribute.options.entityType.id;
                scModel.EntityType.getEntities({id: linkedId}, function(entities) {
                    entities.forEach(function(entity) {
                       self.values.push(entity);
                    });
                })
            }
        });
    }

    self.updateLabels = function() {
        if(self.attributeType === "Page") {
            self.selectedPages.forEach(function(page) {
                var data = {};
                data.pipelineName = self.pipeline.name;
                data.labelName = page.name;
                data.labelPath = page.href;
                data.labelId = page.id;
                data.labelType = "Page";

                //add a new label
                $http.post('/label/create', data).
                then(function(response) {
                    self.pipeline = JSON.parse(response.data.result);
                });
            });
        } else {
            self.selectedValues.forEach(function(value) {
                var data = {};
                data.pipelineName = self.pipeline.name;
                data.label = self.label;

                var miningAttr = [];
                self.selectedAttributesForMining.forEach(function(attr) {
                   miningAttr.push(attr.name);
                });
                data.miningAttributes = miningAttr;

                if(self.attributeType === "Boolean") {
                    data.labelName = value;
                    data.labelPath = self.selectedTypes[0].href;
                    data.labelId = self.selectedTypes[0].id;
                    data.labelType = "Boolean";
                } else {
                    data.labelName = value.name;
                    data.labelPath = self.selectedTypes[0].href;
                    data.labelId = self.selectedTypes[0].id;
                    data.labelType = "Link";
                }
                //add a new label
                $http.post('/label/create', data).
                then(function(response) {
                    self.pipeline = JSON.parse(response.data.result);
                });
            });
        }
    };

    self.updateSelection = function(position, entities) {
        angular.forEach(entities, function(subscription, index) {
            if (position != index)
                subscription.checked = false;
        });
    };

    self.uploadDataSetFile = function () {
        var fd = new FormData();
        fd.append("file", self.file);
        fd.append("pipelineName", self.pipeline.name);
        $http.post("/pipeline/dataset/upload", fd, {
            headers: {'Content-Type': undefined}
        }).then(function (response) {
            self.dataset = response.data.results.path;
            self.uploadedDocument = true;
            self.updateFilePath();
        });
    };

    self.updateFilePath = function() {
        var data = {};
        data.pipelineName = self.pipeline.name;
        data.fileName = self.fileName;
        $http.post('/pipeline/updatePipeline', data);
    }

    self.onFileChange = function (ele) {
        var files = ele.files;
        self.file = files[0];
        self.fileName = self.file.name;
    };

}]);