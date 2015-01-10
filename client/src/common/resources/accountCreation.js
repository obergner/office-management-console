angular.module('resources.accountCreation', ['ngResource'])

.factory('AccountCreation', ['$resource', function($resource){

    var AccountCreation = $resource('/accountcreations/:uuid', { uuid: '@uuid' } , {
        save: {
            method: 'POST',
            url: '/accountcreations'
        }
    });

    angular.extend(AccountCreation.prototype, {

        requireSimsmeSubaccount: function(action) {
            switch (action) {
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
                    throw new Error('Illegal action: ' + action);
            }
        },

        unrequireSimsmeSubaccount: function() {
            this.simsmeAccountRefCreation = { action: 'none' };
        },

        requiresSimsmeSubaccount: function() {
            return (this.allowedOutChannels && this.allowedOutChannels.indexOf('SIMSme') > -1);
        },

        createsNewSimsmeSubaccount: function() {
            return (this.simsmeAccountRefCreation !== undefined && this.simsmeAccountRefCreation.action === 'createNew');
        },

        createsNewSimsmeSubaccountWithCustomName: function() {
            return (this.createsNewSimsmeSubaccount() && this.simsmeAccountRefCreation.usesCustomName());
        },

        referencesExistingSimsmeSubaccount: function() {
            return (this.simsmeAccountRefCreation !== undefined && this.simsmeAccountRefCreation.action === 'referenceExisting');
        }
    });

    return AccountCreation;
}]);
