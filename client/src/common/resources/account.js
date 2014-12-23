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
            url: '/accounts/creations'
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

    angular.extend(Account.prototype, {

        simsmeAccountRefCreation: {
            action: 'none'
        },

        requireSimsmeSubaccount: function() {
            this.simsmeAccountRefCreation = {
                action: 'createNew',
                name: null, 
                imageBase64Jpeg: null
            };
        },

        unrequireSimsmeSubaccount: function() {
            this.simsmeAccountRefCreation = {
                action: 'none'
            };
        },

        requiresSimsmeSubaccount: function() {
            return (this.allowedOutChannels && this.allowedOutChannels.indexOf('SIMSme') > -1);
        }
    });

    return Account;
}]);
