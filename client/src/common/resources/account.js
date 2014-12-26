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

        requireSimsmeSubaccount: function(action) {
            var act = action || 'createNew';
            switch (act) {
                case 'none':
                    this.simsmeAccountRefCreation = { action: 'none' };
                    break;
                case 'createNew':
                    this.$$$__createNewSimsmeAccountRefCreation = (this.$$$__createNewSimsmeAccountRefCreation || {
                        action: 'createNew',
                        name: null,
                        imageBase64Jpeg: null,
                        usesCustomName: function() { return this.name !== null; },
                        useCustomName: function() { this.name = ( this.name !== null ? this.name : '' ); }
                    });
                    this.simsmeAccountRefCreation = this.$$$__createNewSimsmeAccountRefCreation;
                    break;
                case 'referenceExisting':
                    this.$$$__referenceExistingSimsmeAccountRefCreation = (this.$$$__referenceExistingSimsmeAccountRefCreation || {
                        action: 'referenceExisting',
                        existingSimsmeGuid: null
                    });
                    this.simsmeAccountRefCreation = this.$$$__referenceExistingSimsmeAccountRefCreation;
                    break;
                default:
                    throw new Error('Illegal action: ' + act);
            }
        },

        unrequireSimsmeSubaccount: function() {
            this.simsmeAccountRefCreation = { action: 'none' };
        },

        requiresSimsmeSubaccount: function() {
            return (this.allowedOutChannels && this.allowedOutChannels.indexOf('SIMSme') > -1);
        },

        existingSimsmeGuidPattern: function() {
            var self = this;
            var simsmeGuidPattern = /^\d{1,3}:\{[0-9A-F]{8}-[0-9A-F]{4}-[0-9A-F]{4}-[0-9A-F]{4}-[0-9A-F]{12}\}$/;
            return {
                test: function(value) {
                    if (self.simsmeAccountRefCreation === undefined || self.simsmeAccountRefCreation.action !== 'referenceExisting') {
                        return true;
                    }
                    return simsmeGuidPattern.test(value);
                }
            };
        }
    });

    return Account;
}]);
