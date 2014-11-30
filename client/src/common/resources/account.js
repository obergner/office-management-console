angular.module('resources.account', ['ngResource'])

.factory('Account', ['$resource', 'BACKEND', function($resource, backend){

    return $resource(backend.host + '/accounts/uuid/:uuid', { uuid: '@uuid' } , {
        query: {
            method: 'GET',
            url: backend.host + '/accounts',
            isArray: true
        },
        save: {
            method: 'POST',
            url: backend.host + '/accounts'
        },
        update: {
            method: 'PUT',
            params: { uuid: '@uuid' }
        }
    });
}]);
