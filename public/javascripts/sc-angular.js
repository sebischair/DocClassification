/**
 * @license sc-angular v0.9.0
 * (c) 2016 Sebis
 * License: Sebis Proprietary
 * https://bitbucket.org/sebischair/sc-angular
 */
(function () {
    angular.module('sociocortex', ['ngStorage', 'ngResource']);

    var SC_DEFAULT_URI = 'https://server.sociocortex.com';
    var SC_MODELER_URI = 'https://modeler.sociocortex.com';

    // Initiate with default value
    angular.module('sociocortex').value('scConnection', {
        baseUri: SC_DEFAULT_URI,
        apiVersion: 'v1',
        authenticationMethod: 'basic' // possible values: basic, jwt
    });

    angular.module('sociocortex').value('scConfig', {
        contentManagerUrlPattern: SC_DEFAULT_URI + '/pages/',
        modelerUrlPattern: SC_DEFAULT_URI + '/pages/',
        dateFormat: 'DD.MM.YYYY',
        numberFormat: '0.000'
    });

    angular.module('sociocortex').config(['$sceDelegateProvider', function ($sceDelegateProvider) {
        $sceDelegateProvider.resourceUrlWhitelist(['self', SC_DEFAULT_URI + '/api/**']);
    }]);
})();

(function () {

    const JWT = 'jwt';
    const BASIC = 'basic';
    const COOKIE = 'scjwt';

    function getBasicAuthenticationHeader(user, password) {
        return window.btoa('' + user + ':' + password)
    }


    function setAuthorizationHeaderTemplate($localStorage, scConnection, $httpDefaults) {
        if ($localStorage.scStore && $localStorage.scStore.token) {
            $httpDefaults.headers['Access-Control-Allow-Headers'] = JWT;

            if ((!scConnection.authenticationMethod || scConnection.authenticationMethod === BASIC) && $localStorage.scStore.type === BASIC) {
                $httpDefaults.headers.common.Authorization = 'Basic ' + $localStorage.scStore.token;
            } else if (scConnection.authenticationMethod === JWT && $localStorage.scStore.type === JWT) {
                $httpDefaults.headers.common.Authorization = 'Bearer ' + $localStorage.scStore.token;
            }
        }
        else {
            delete $localStorage.scStore;
            delete $httpDefaults.headers.common.Authorization;
        }
    }

    angular.module('sociocortex')
        .service('scAuth', ['$localStorage', '$q', '$http', 'scUtil', 'scConnection', function scAuthentication($localStorage, $q, $http, scUtil, scConnection) {
            return {
                getUser: getUserDetails,
                login: login,
                logout: logout,
                isAuthenticated: isAuthenticated
            };

            function getUserDetails() {
                return $localStorage.scStore.user;
            }

            function login(user, password, callback, error) {
                if (!scConnection.authenticationMethod || scConnection.authenticationMethod.toLowerCase() === BASIC) {
                    return loginBasic(user, password, callback, error);
                } else if (scConnection.authenticationMethod.toLowerCase() === JWT) {
                    return loginJWT(user, password, callback, error);
                }
            }

            function loginBasic(user, password, callback, error) {
                $localStorage.scStore = {
                    token: getBasicAuthenticationHeader(user, password),
                    type: BASIC
                };

                setAuthorizationHeader();

                var deferred = $q.defer();


                $http({
                    url: scUtil.getFullUrl(scUtil.paths.usersMe),
                    method: 'GET'
                }).then(function (res) {
                    $localStorage.scStore.user =
                        {
                            id: res.data.id,
                            name: res.data.name,
                            href: res.data.href
                        };

                    if (callback && angular.isFunction(callback)) {
                        res = callback(res.data);
                    } else {
                        res = res.data;
                    }

                    deferred.resolve(res);
                }, function (err) {
                    delete $localStorage.scStore;

                    if (error && angular.isFunction(error)) {
                        err = error(err);
                    }

                    deferred.reject(err);
                });

                return deferred.promise;
            }

            function loginJWT(user, password, callback, error) {
                var deferred = $q.defer();

                $localStorage.scStore = {
                    type: JWT
                };

                $http({
                    headers: {
                        Authorization: 'Basic ' + getBasicAuthenticationHeader(user, password)
                    },
                    url: scUtil.getFullUrl(scUtil.paths.jwt),
                    method: 'GET'
                }).then(function (res) {
                    $localStorage.scStore.token = res.data.token;

                    $localStorage.scStore.user = {
                        id: res.data.payload.sub,
                        name: res.data.payload.name,
                        href: scUtil.generateHref(scUtil.paths.users, res.data.payload.sub)
                    };


                    setAuthorizationHeader();

                    if (callback && angular.isFunction(callback)) {
                        res = callback($localStorage.scStore.user);
                    } else {
                        res = $localStorage.scStore.user;
                    }

                    deferred.resolve(res);
                }, function (err) {
                    delete $localStorage.scStore;

                    if (error && angular.isFunction(error)) {
                        err = error(err);
                    }

                    deferred.reject(err);
                });

                return deferred.promise;
            }


            function logout() {
                delete $localStorage.scStore;
                setAuthorizationHeader();
            }

            function isAuthenticated() {
                deleteExpiredToken();
                return !!$localStorage.scStore;
            }

            function deleteExpiredToken() {
                if (scConnection.authenticationMethod === JWT && $localStorage.scStore && $localStorage.scStore.type === JWT && isJWTExpired($localStorage.scStore.token)) {
                    logout();
                }
            }

            function setAuthorizationHeader() {
                setAuthorizationHeaderTemplate($localStorage, scConnection, $http.defaults)
            }

            function isJWTExpired(token) {
                var parts = token.split('.');

                if (parts.length !== 3) {
                    return true;
                }

                var decoded = window.atob(parts[1]);

                if (!decoded) {
                    return true;
                }

                var decodedToken = angular.fromJson(decoded);

                if (!decodedToken.exp) {
                    return true;
                }

                var expirationTime = new Date(0);
                expirationTime.setUTCSeconds(decodedToken.exp);
                return !(expirationTime.valueOf() > (new Date().valueOf()));
            }
        }]);

    angular.module('sociocortex').run(function ($http, scConnection, $localStorage) {
        function appendTransform(defaults, transform) {
            defaults = angular.isArray(defaults) ? defaults : [defaults];
            return defaults.concat(transform);
        }


        function setAuthorizationHeader() {
            setAuthorizationHeaderTemplate($localStorage, scConnection, $http.defaults)
        }

        setAuthorizationHeader();

        $http.defaults.transformResponse = appendTransform($http.defaults.transformResponse, function (data, headersGetter, status) {
            var headers = headersGetter();

            var refreshedJWT = headers[JWT];
            if (refreshedJWT && scConnection.authenticationMethod === JWT) {
                $localStorage.scStore.token = refreshedJWT;

                setAuthorizationHeader();

            }
            return data;
        });
    });

})();

(function () {
    angular.module('sociocortex').service('scData', ['$resource', '$q', '$http', 'scUtil', 'scAuth', function scCrudService($resource, $q, $http, scUtil, scAuth) {
        var Entity = $resource(scUtil.getFullUrl(scUtil.paths.entities + "/:id"),
            {
                id: "@id"
            },
            {
                update: {
                    method: "PUT"
                },
                queryByEntityType: {
                    method: "GET",
                    url: scUtil.getFullUrl(scUtil.paths.entityTypes + "/:id/" + scUtil.paths.entities),
                    isArray: true
                },
                queryByWorkspace: {
                    method: "GET",
                    url: scUtil.getFullUrl(scUtil.paths.workspaces + "/:id/" + scUtil.paths.entities),
                    isArray: true
                },
                getAttributes: {
                    method: "GET",
                    url: scUtil.getFullUrl(scUtil.paths.entities + "/:id/" + scUtil.paths.attributes),
                    isArray: true
                },
                getFiles: {
                    method: "GET",
                    url: scUtil.getFullUrl(scUtil.paths.entities + "/:id/" + scUtil.paths.files),
                    isArray: true
                }
            });

        // turns this: '{ "attributes": [{ "values": [ 18.4 ], "name": "Price", "type": "number" }]}'
        // into this:  '{ "attributes": { "Price": 18.4 }}' 
        Entity.objectifyAttributes = function (x) {
            if (angular.isArray(x)) {
                var unwrappedEntities = [];
                for (var i = 0; i < x.length; i++) {
                    unwrappedEntities.push(unwrapEntity(x[i]));
                }
                return unwrappedEntities;
            } else {
                return unwrapEntity(x);
            }

            function unwrapEntity(entity) {

                if (!entity.attributes) {
                    entity.attributes = {};
                }

                if (angular.isArray(entity.attributes)) {
                    var unwrappedAttributes = {};
                    angular.forEach(entity.attributes, function (currAttr) {
                        unwrappedAttributes[currAttr.name] = currAttr.values.length === 1 ? currAttr.values[0] : currAttr.values;
                    });
                    entity.attributes = unwrappedAttributes;
                }

                if (!entity.derivedAttributes) {
                    entity.derivedAttributes = {};
                }

                if (angular.isArray(entity.derivedAttributes)) {
                    var unwrappedAttributes = {};
                    angular.forEach(entity.derivedAttributes, function (currAttr) {
                        unwrappedAttributes[currAttr.name] = currAttr.values.length === 1 ? currAttr.values[0] : currAttr.values;
                    });
                    entity.derivedAttributes = unwrappedAttributes;
                }

                return entity;
            }
        };

        // turns this:  '{ "attributes": { "Price": 18.4 }}' 
        // into this: '{ "attributes": [{ "values": [ 18.4 ], "name": "Price"}]}'
        Entity.arrayifyAttributes = function (x) {
            if (angular.isArray(x)) {
                var wrappedEntities = [];
                for (var i = 0; i < x.length; i++) {
                    wrappedEntities.push(wrapEntity(x[i]));
                }
                return wrappedEntities;
            } else {
                return wrapEntity(x);
            }

            function wrapEntity(entity) {

                if (!entity.attributes) {
                    entity.attributes = [];
                }

                if (angular.isObject(entity.attributes)) {
                    var wrappedAttributes = [];
                    angular.forEach(entity.attributes, function (value, name) {
                        if (angular.isUndefined(value) || value == null) {
                            wrappedAttributes.push({ name: name, values: [] });
                        } else if (angular.isArray(value)) {
                            wrappedAttributes.push({ name: name, values: value });
                        } else {
                            wrappedAttributes.push({ name: name, values: [value] });
                        }
                    });

                    entity.attributes = wrappedAttributes;
                }

                if (!entity.derivedAttributes) {
                    entity.derivedAttributes = [];
                }

                if (angular.isObject(entity.derivedAttributes)) {
                    var wrappedAttributes = [];
                    angular.forEach(entity.derivedAttributes, function (value, name) {
                        if (angular.isUndefined(value) || value == null) {
                            wrappedAttributes.push({ name: name, values: [] });
                        } else if (angular.isArray(value)) {
                            wrappedAttributes.push({ name: name, values: value });
                        } else {
                            wrappedAttributes.push({ name: name, values: [value] });
                        }
                    });

                    entity.derivedAttributes = wrappedAttributes;
                }

                return entity;
            }
        };

        delete Entity.query;

        var Workspace = $resource(scUtil.getFullUrl(scUtil.paths.workspaces + "/:id"),
            {
                id: "@id"
            },
            {
                update: {
                    method: "PUT"
                },
                getEntities: {
                    method: "GET",
                    url: scUtil.getFullUrl(scUtil.paths.workspaces + "/:id/" + scUtil.paths.entities),
                    isArray: true
                },
                getEntityTypes: {
                    method: "GET",
                    url: scUtil.getFullUrl(scUtil.paths.workspaces + "/:id/" + scUtil.paths.entityTypes),
                    isArray: true
                }
            });

        var Attribute = $resource(scUtil.getFullUrl(scUtil.paths.attributes + "/:id"),
            {
                id: "@id"
            },
            {
                update: {
                    method: "PUT"
                },
                queryByEntity: {
                    method: "GET",
                    url: scUtil.getFullUrl(scUtil.paths.entities + "/:id/" + scUtil.paths.attributes),
                    isArray: true
                },
                getValues: {
                    method: "GET",
                    url: scUtil.getFullUrl(scUtil.paths.attributes + "/:id/" + scUtil.paths.attributeValues),
                    isArray: true
                },
                addValue: {
                    method: "POST",
                    url: scUtil.getFullUrl(scUtil.paths.attributes + "/:id/" + scUtil.paths.attributeValues)
                },
                getValue: {
                    method: "GET",
                    url: scUtil.getFullUrl(scUtil.paths.attributes + "/:id/" + scUtil.paths.attributeValues + "/:index")
                },
                updateValue: {
                    method: "PUT",
                    url: scUtil.getFullUrl(scUtil.paths.attributes + "/:id/" + scUtil.paths.attributeValues + "/:index")
                },
                deleteValue: {
                    method: "DELETE",
                    url: scUtil.getFullUrl(scUtil.paths.attributes + "/:id/" + scUtil.paths.attributeValues + "/:index")
                },
                deleteValues: {
                    method: "DELETE",
                    url: scUtil.getFullUrl(scUtil.paths.attributes + "/:id/" + scUtil.paths.attributeValues)
                }
            });

        delete Attribute.query;

        var File = $resource(scUtil.getFullUrl(scUtil.paths.files + "/:id"),
            {
                id: "@id"
            },
            {
                update: {
                    method: "PUT"
                },
                queryByEntity: {
                    method: "GET",
                    url: scUtil.getFullUrl(scUtil.paths.entities + "/:id/" + scUtil.paths.files),
                    isArray: true
                }
            });

        delete File.query;

        File.download = function (file, successCallback, errorCallback) {
            var fileId = angular.isObject(file) ? file.id : file;
            var url = scUtil.getFullUrl(scUtil.paths.files + "/" + fileId + "/content");

            var deferred = $q.defer();

            $http({
                httpMethod: 'GET',
                url: url,
                responseType: 'blob'
            }).then(function (res) {
                if (successCallback && angular.isFunction(successCallback)) {
                    res = successCallback(res);
                }

                deferred.resolve(res);
            }, function (err) {
                if (errorCallback && angular.isFunction(errorCallback)) {
                    err = errorCallback(err);
                }
                deferred.reject(err);
            });
            return deferred.promise;
        };

        var Task = $resource(scUtil.getFullUrl(scUtil.paths.tasks + "/:id"),
            {
                id: "@id"
            },
            {
                update: {
                    method: "PUT"
                },
                getAttributes: {
                    method: "GET",
                    url: scUtil.getFullUrl(scUtil.paths.tasks + "/:id/" + scUtil.paths.attributes),
                    isArray: true
                }
            });

        var Expertise = $resource(scUtil.getFullUrl(scUtil.paths.expertises + "/:id"),
            {
                id: "@id"
            },
            {
                update: {
                    method: "PUT"
                }
            });

        var Event =
            $resource(scUtil.getFullUrl(scUtil.paths.events));

        var ChangeSet =
            $resource(scUtil.getFullUrl(scUtil.paths.changesets + '/:id'),
                {
                    id: '@id'
                },
                {
                    postComment: {
                        method: 'POST',
                        url: scUtil.getFullUrl(scUtil.paths.changesets + '/:id/' + scUtil.paths.comments)
                    },
                    getComments: {
                        method: 'GET',
                        url: scUtil.getFullUrl(scUtil.paths.changesets + '/:id/' + scUtil.paths.comments),
                        isArray: true
                    },
                    like: {
                        method: 'POST',
                        url: scUtil.getFullUrl(scUtil.paths.changesets + '/:id/' + scUtil.paths.like)
                    },
                    unlike: {
                        method: 'DELETE',
                        url: scUtil.getFullUrl(scUtil.paths.changesets + '/:id/' + scUtil.paths.like)
                    }
                }
            );

        var Comment = $resource(scUtil.getFullUrl(scUtil.paths.comments + '/:id'),
            {
                id: "@id"
            },
            {
                delete: {
                    method: "DELETE"
                },
                put: {
                    method: "PUT"
                }
            }
        );

        return {
            Entity: Entity,
            File: File,
            Workspace: Workspace,
            Attribute: Attribute,
            Task: Task,
            Expertise: Expertise,
            Event: Event,
            ChangeSet: ChangeSet,
            Comment: Comment
        };
    }]);
})();

(function () {
    angular.module('sociocortex').service('scModel', ['$resource', 'scUtil', 'scAuth', function scCrudService($resource, scUtil, scAuth) {
        var EntityType = $resource(scUtil.getFullUrl(scUtil.paths.entityTypes + "/:id"),
            {
                id: "@id"
            },
            {
                update: {
                    method: "PUT"
                },
                queryByWorkspace: {
                    method: "GET",
                    url: scUtil.getFullUrl(scUtil.paths.workspaces + "/:id/" + scUtil.paths.entityTypes),
                    isArray: true
                },
                getAttributeDefinitions: {
                    method: "GET",
                    url: scUtil.getFullUrl(scUtil.paths.entityTypes + "/:id/" + scUtil.paths.attributeDefinitions),
                    isArray: true
                },
                getEntities: {
                    method: "GET",
                    url: scUtil.getFullUrl(scUtil.paths.entityTypes + "/:id/" + scUtil.paths.entities),
                    isArray: true
                },
            });

        delete EntityType.query;

        var AttributeDefinition = $resource(scUtil.getFullUrl(scUtil.paths.attributeDefinitions + "/:id"),
            {
                id: "@id"
            },
            {
                update: {
                    method: "PUT"
                },
                queryByEntityType: {
                    method: "GET",
                    url: scUtil.getFullUrl(scUtil.paths.entityTypes + "/:id/" + scUtil.paths.attributeDefinitions),
                    isArray: true
                }
            });

        delete AttributeDefinition.query;

        var DerivedAttributeDefinition = $resource(scUtil.getFullUrl(scUtil.paths.derivedAttributeDefinitions + "/:id"),
            {
                id: "@id"
            },
            {
                update: {
                    method: "PUT"
                },
                queryByEntityType: {
                    method: "GET",
                    url: scUtil.getFullUrl(scUtil.paths.entityTypes + "/:id/" + scUtil.paths.derivedAttributeDefinitions),
                    isArray: true
                }
            });

        delete DerivedAttributeDefinition.query;

        var CustomFunction = $resource(scUtil.getFullUrl(scUtil.paths.customFunctions + "/:id"),
            {
                id: "@id"
            },
            {
                update: {
                    method: "PUT"
                },
                queryByEntityType: {
                    method: "GET",
                    url: scUtil.getFullUrl(scUtil.paths.entityTypes + "/:id/" + scUtil.paths.customFunctions),
                    isArray: true
                },
                queryByWorkspace: {
                    method: "GET",
                    url: scUtil.getFullUrl(scUtil.paths.workspaces + "/:id/" + scUtil.paths.customFunctions),
                    isArray: true
                }
            });

        var TaskDefinition = $resource(scUtil.getFullUrl(scUtil.paths.taskDefinitions + "/:id"),
            {
                id: "@id"
            },
            {
                update: {
                    method: "PUT"
                },
                queryByEntityType: {
                    method: "GET",
                    url: scUtil.getFullUrl(scUtil.paths.entityTypes + "/:id/" + scUtil.paths.taskDefinitions),
                    isArray: true
                },
                addAttributeDefinition: {
                    method: "PUT",
                    url: scUtil.getFullUrl(scUtil.paths.taskDefinitions + "/:taskDefinitionId/" + scUtil.paths.attributeDefinitions + "/:attributeDefinitionId"),
                    params: {
                        attributeDefinitionId: "@attributeDefinition.id",
                        taskDefinitionId: "@taskDefinition.id"
                    }
                },
                removeAttributeDefinition: {
                    method: "DELETE",
                    url: scUtil.getFullUrl(scUtil.paths.taskDefinitions + "/:taskDefinitionId/" + scUtil.paths.attributeDefinitions + "/:attributeDefinitionId"),
                    params: {
                        attributeDefinitionId: "@attributeDefinition.id",
                        taskDefinitionId: "@taskDefinition.id"
                    }
                }
            });

        var Stage = $resource(scUtil.getFullUrl(scUtil.paths.stages + "/:id"),
            {
                id: "@id"
            },
            {
                update: {
                    method: "PUT"
                },
                queryByEntityType: {
                    method: "GET",
                    url: scUtil.getFullUrl(scUtil.paths.entityTypes + "/:id/" + scUtil.paths.stages),
                    isArray: true
                }
            });

        var Sentry = $resource(scUtil.getFullUrl(scUtil.paths.sentries + "/:id"),
            {
                id: "@id"
            },
            {
                update: {
                    method: "PUT"
                },
                addCriteria: {
                    method: "PUT",
                    url: scUtil.getFullUrl(scUtil.paths.sentries + "/:sentryId/criteria/:processId"),
                    params: {
                        sentryId: "@sentry.id",
                        processId: "@process.id"
                    }
                },
                removeCriteria: {
                    method: "DELETE",
                    url: scUtil.getFullUrl(scUtil.paths.sentries + "/:sentryId/criteria/:processId"),
                    params: {
                        sentryId: "@sentry.id",
                        processId: "@process.id"
                    }
                }
            });

        return {
            EntityType: EntityType,
            AttributeDefinition: AttributeDefinition,
            DerivedAttributeDefinition: DerivedAttributeDefinition,
            CustomFunction: CustomFunction,
            TaskDefinition: TaskDefinition,
            Stage: Stage,
            Sentry: Sentry
        };
    }]);
})();

(function () {
    angular.module('sociocortex').service('scMxl', ['$cacheFactory', '$q', '$http', 'scAuth', 'scUtil', function scMxlService($cacheFactory, $q, $http, scAuth, scUtil) {
        var autoCompleteCache = $cacheFactory('mxlAutoCompleteCache');
        
        return {
            autoComplete: autoComplete,
            query: query,
            validate: validate
        };

        function autoComplete(p1, p2, p3, p4) {
            var context, restriction, callback, error;

            if (angular.isObject(p1)) {
                context = p1;

                if (angular.isString(p2)) {
                    restriction = p2;
                    callback = p3;
                    error = p4;
                }
                else {
                    callback = p2;
                    error = p3;
                }

            } else {
                if (angular.isString(p1)) {
                    restriciton = p1;
                    callback = p2;
                    error = p3;
                } else {
                    callback = p1;
                    error = p2;
                }
            }

            var cacheKey = "hints";

            if (context) {
                cacheKey += "#" + JSON.stringify(context);
            }

            if (restriction) {
                cacheKey += "#" + restriction;
            }

            var cachedHints = autoCompleteCache.get(cacheKey);

            var deferred = $q.defer();

            if (cachedHints) {
                deferred.resolve(cachedHints);
                return deferred.promise;
            }

            mxlRequest({
                httpMethod: 'GET',
                context: context,
                mxlMethod: 'mxlAutoCompletionHints',
                params: { restrict: restriction }
            }, function (res) {
                autoCompleteCache.put(cacheKey, res);

                if (callback && angular.isFunction(callback)) {
                    res = callback(res);
                }

                deferred.resolve(res);
            }, function (err) {
                if (error && angular.isFunction(error)) {
                    err = error(err);
                }
                deferred.reject(err);
            });
            return deferred.promise;
        }

        function query(p1, p2, p3, p4) {
            var context, data, callback, error;

            if (angular.isObject(p2)) {
                context = p1;
                data = p2;
                callback = p3;
                error = p4;
            } else if (angular.isObject(p1)) {
                data = p1;
                callback = p2;
                error = p3;
            } else {
                callback = p1;
                error = p2;
            }

            var deferred = $q.defer();

            mxlRequest({
                httpMethod: 'POST',
                context: context,
                mxlMethod: 'mxlQuery',
                data: data
            }, function (res) {
                if (callback && angular.isFunction(callback)) {
                    res = callback(res);
                }

                deferred.resolve(res);
            }, function (err) {
                if (error && angular.isFunction(error)) {
                    err = error(err);
                }
                deferred.reject(err);
            });
            return deferred.promise;
        }

        function validate(p1, p2, p3, p4) {
            var context, data, callback, error;

            if (angular.isObject(p2)) {
                context = p1;
                data = p2;
                callback = p3;
                error = p4;
            } else if (angular.isObject(p1)) {
                data = p1;
                callback = p2;
                error = p3;
            } else {
                callback = p1;
                error = p2;
            }

            var deferred = $q.defer();

            mxlRequest({
                httpMethod: 'POST',
                context: context,
                mxlMethod: 'mxlValidation',
                data: data
            }, function (res) {
                if (callback && angular.isFunction(callback)) {
                    res = callback(res);
                }

                deferred.resolve(res);
            }, function (err) {
                if (error && angular.isFunction(error)) {
                    err = error(err);
                }
                deferred.reject(err);
            });
            return deferred.promise;
        }

        function mxlRequest(options, callback, error) {
            var path = '';

            if (options.context) {
                if (options.context.entity) {
                    path = scUtil.paths.entities + '/' + options.context.entity.id + '/';
                } else if (options.context.entityType) {
                    path = scUtil.paths.entityTypes + '/' + options.context.entityType.id + '/';
                } else if (options.context.workspace) {
                    path = scUtil.paths.workspaces + '/' + options.context.workspace.id + '/';
                }
            }

            path = scUtil.getFullUrl(path + options.mxlMethod);

            return $http({
                method: options.httpMethod,
                url: path,
                data: options.data,
                params: options.params
            }).then(function (res) {
                return callback(res.data);
            }, function (res) {
                if (error) {
                    return error(res.data);
                }
            });
        }
    }]);


})();

(function () {
    angular.module('sociocortex').service('scPrincipal', ['$resource', 'scUtil', 'scAuth', function scCrudService($resource, scUtil, scAuth) {
        var User = $resource(scUtil.getFullUrl(scUtil.paths.users + "/:id"),
           {
               id: "@id"
           },
           {
               update:
                   {
                       method: "PUT"
                   },
               picture: {
                   method: "GET",
                   url: scUtil.getFullUrl(scUtil.paths.users + "/:id/picture")
               },
               me: {
                   method: "GET",
                   url: scUtil.getFullUrl(scUtil.paths.usersMe)
               },
               myPicture: {
                   method: "GET",
                   url: scUtil.getFullUrl(scUtil.paths.usersMe + "/picture")
               }
           });

        User.getPictureUrl = function (user) {
            if (angular.isString(user)) {
                return scUtil.getFullUrl(scUtil.paths.users + "/" + user + "/picture");
            } else if (angular.isObject(user) && user.id) {
                return scUtil.getFullUrl(scUtil.paths.users + "/" + user.id + "/picture");
            } else {
                return null;
            }
        }

        var Group = $resource(scUtil.getFullUrl(scUtil.paths.groups + "/:id"),
           {
               id: "@id"
           },
           {
               update:
                    {
                        method: "PUT"
                    }
           });

        return {
            User: User,
            Group: Group
        };
    }]);
})();

(function() {
    angular.module('sociocortex').service('scRoute', scRoute);

    function scRoute(scConnection, scConfig) {

        function getEntityUrlForContentManager(entity) {
            return scConfig.contentManagerUrlPattern + entity.id
        }

        function getEntityUrlForModeler(entity) {
            return scConfig.modelerUrlPattern + entity.id
        }

        return {
            getEntityUrlForContentManager: getEntityUrlForContentManager,
            getEntityUrlForModeler: getEntityUrlForModeler,
        };

    }
})();

(function () {
    angular.module('sociocortex').service('scSearch', ['$q', '$http', 'scUtil', function scSearchService( $q, $http, scUtil) {
        return {
            hints: function (filter, callback, error) {
                return searchRequest(scUtil.paths.searchHints,filter,callback,error);
            },
            results: function (filter, callback, error) {
                return searchRequest(scUtil.paths.searchResults,filter,callback, error);
            }
        };

        function searchRequest(url, filter, callback, error) {
            var deferred = $q.defer();

            $http({
                method: 'GET',
                url: scUtil.getFullUrl(url),
                params: getParams(filter)
            }).then(function (res) {
                res = res.data;
                if (callback && angular.isFunction(callback)) {
                    res = callback(res);
                }

                deferred.resolve(res);
            }, function (err) {
                if (error && angular.isFunction(error)) {
                    err = error(err);
                }
                deferred.reject(err);
            });

            return deferred;
        }

        function getParams(filter) {
            if(!filter){
                return undefined;
            }

            var filterParams = {};

            if (filter.workspace) {
                filterParams.workspace = filter.workspace.id;
            }

            if (filter.entityType) {
                filterParams.entityType = filter.entityType.id;
            }

            if (filter.resourceType) {
                filterParams.resourceType = filter.resourceType;
            }

            if (filter.invalidValues) {
                filterParams.invalidValues = true;
            }

            if (filter.invalidLinks) {
                filterParams.invalidLinks = true;
            }

            if (filter.text) {
                filterParams.text = filter.text;
            }

            if (filter.n) {
                filterParams.n = filter.n;
            }

            if (filter.page) {
                filterParams.page = filter.page;
            }

            if (filter.orderBy) {
                filterParams.orderBy = filter.orderBy;
            }

            if(filter.attributes) {
                filterParams.attributes = filter.attributes;
            }

            if(filter.meta) {
                filterParams.meta = filter.meta;
            }

            if(filter.orderByAttribute) {
                filterParams.orderByAttribute = filter.orderByAttribute;
            }

            if(filter.descending) {
                filterParams.descending = filter.descending;
            }

            if(filter.hasAttributes) {
                filterParams.hasAttributes = filter.hasAttributes;
            }

            if(filter.hasAttributeValues) {
              filterParams.hasAttributeValues = filter.hasAttributeValues;
            }

            return filterParams;
        }
    }]);



})();

(function () {
    angular.module('sociocortex').service('scUtil', scUtil);

    function scUtil(scConnection, scConfig) {

        var paths = getPaths();

        return {
            getFullUrl: getFullUrl,
            getRelativeUrl: getRelativeUrl,
            getEntityUrlForContentManager: getEntityUrlForContentManager,
            generateHref: generateHref,
            paths: paths,
            isEntity: isOfType(paths.entities),
            isTask: isOfType(paths.tasks),
            isAttribute: isOfType(paths.attributes),
            isFile: isOfType(paths.files),
            isEntityType: isOfType(paths.entityTypes),
            isTaskDefinition: isOfType(paths.taskDefinitions),
            isStage: isOfType(paths.stages),
            isAttributeDefinition: isOfType(paths.attributeDefinitions),
            isWorkspace: isOfType(paths.workspaces),
            isUser: isOfType(paths.users),
            isGroup: isOfType(paths.groups),
            isDerivedAttributeDefinition: isOfType(paths.derivedAttributeDefinitions),
            isCustomFunction: isOfType(paths.customFunctions)
        };

        function isOfType(type) {
            return function isOfTypeFunction(obj) {
                return angular.isObject(obj) && obj.href && obj.href.indexOf('/' + type + '/') > 0;
            }
        }

        function combinePaths(str1, str2) {
            if (str1.charAt(str1.length - 1) === '/') {
                str1 = str1.substr(0, str1.length - 1);
            }

            if (str2.charAt(0) === '/') {
                str2 = str2.substr(1, scConnection.baseUri.length - 1);
            }

            return str1 + '/' + str2;
        }

        function generateHref(resourceType, resourceId) {
            return getFullUrl(resourceType + "/" + resourceId);
        }

        function getRelativeUrl(link) {
            if (isLinkObject(link)) {
                link = link.href;
            } else if (!angular.isString(link)) {
                throw new TypeError("The parameter must be a of type 'link object' or 'string'");
            }

            var prefix = getFullUrl('');
            if (link.indexOf(prefix) !== 0) {
                throw new Error('The given URL does not start with the proper prefix of "' + prefix + "'");
            } else {
                // if 1 is subtracted the returned url will contain a prefixed slash
                return link.substr(prefix.length - 1);
            }
        }

        function isLinkObject(link) {
            return angular.isObject(link)
                && !!link.id
                && !!link.href
                && !!link.name;
        }

        function getFullUrl(urlPart) {
            return combinePaths(combinePaths(scConnection.baseUri, 'api/' + scConnection.apiVersion), urlPart);
        }

        function getEntityUrlForContentManager(entity) {
            return scConfig.contentManagerUrlPattern + entity.id
        }

        function getPaths() {
            return {
                entities: 'entities',
                attributes: 'attributes',
                files: 'files',
                entityTypes: 'entityTypes',
                attributeDefinitions: 'attributeDefinitions',
                workspaces: 'workspaces',
                users: 'users',
                usersMe: 'users/me',
                groups: 'groups',
                derivedAttributeDefinitions: 'derivedAttributeDefinitions',
                customFunctions: 'customFunctions',
                tasks: 'tasks',
                taskDefinitions: 'taskDefinitions',
                expertises: 'expertises',
                attributeValues: 'values',
                events: "events",
                changesets: "changesets",
                comments: "comments",
                like: "like",
                jwt: "jwt",
                searchHints: "searchHints",
                searchResults: "searchResults",
                stages: "stages",
                sentries: "sentries"
            };
        }
    }
})();

(function () {
    angular.module('sociocortex').directive('scHref', function (scRoute) {
        return {
            restrict: 'A',
            link: function link(scope, element, attrs) {
                attrs.$set('href', scRoute.getEntityUrlForContentManager({ id: attrs.scHref }));
            }
        };
    });
})();

(function () {
    angular.module('sociocortex').directive('scSrc', function (scData) {
        return {
            restrict: 'A',
            link: function link(scope, element, attrs) {
                scData.File.download({ id: attrs.scSrc }, function (file) {
                    attrs.$set('src', URL.createObjectURL(file.data));
                });
            }
        };
    });
})();
