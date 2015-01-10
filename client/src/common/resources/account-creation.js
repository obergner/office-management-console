angular.module('resources.accountCreation', ['ngResource'])

.factory('AccountCreation', ['$resource', function($resource){

    var AccountCreation = $resource('/accountcreations/:uuid', { uuid: '@uuid' } , {
        save: {
            method: 'POST',
            url: '/accountcreations'
        }
    });

    return AccountCreation;
}]);
