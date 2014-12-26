angular.module('resources.account', ['ngResource'])

.factory('Account', ['$resource', function($resource){

    var Account = $resource('/accounts/uuid/:uuid', { uuid: '@uuid' } , {
        query: {
            method: 'GET',
            url: '/accounts',
            isArray: true
        },
        save: {
            method: 'POST',
            url: '/accounts'
        },
        update: {
            method: 'PUT',
            params: { uuid: '@uuid' }
        },
        get: {
            method: 'GET',
            url: '/accounts/uuid/:uuid'
        } 
    });

    return Account;
}]);
