angular.module('resources.account', ['ngResource'])

.factory('Account', ['$resource', function($resource){

    return $resource('/accounts/uuid/:uuid', { uuid: '@uuid' } , {
        query: {
            method: 'GET',
            url: '/accounts',
            isArray: true
        },
        save: {
            method: 'POST',
            url: '/accounts/creations'
        },
        update: {
            method: 'PUT',
            params: { uuid: '@uuid' }
        }
    });
}]);
