(function() {
    function AccountCreationViewModelFactory(AccountCreation) {
        /*
        * Encapsulates the Office account proper to be created. 
        */

        var AccountCreationViewModel = function() {
            this.AvailableOutChannels = ['FlashSMS', 'USSD', 'SIMSme'];

            this.name = '';
            this.mmaId = null;
            this.allowedOutChannels = [];

            this.subaccounts = new AccountCreationViewModel.Subaccounts(this);
        };

        AccountCreationViewModel.prototype = {
            constructor: AccountCreationViewModel,

            save: function(onSuccess, onError) {
                var accountCreationResource = new AccountCreation();
                accountCreationResource.name = this.name;
                accountCreationResource.mmaId = this.mmaId;
                accountCreationResource.allowedOutChannels = this.allowedOutChannels;
                if (this.subaccounts.createsAccountRefOfType('SIMSme')) {
                    accountCreationResource.simsmeAccountRefModification = this.subaccounts.simsme;
                }

                accountCreationResource.$save(onSuccess, onError);
            },
        };

        /*
        * Collection of references to nested accounts that
        * may potentially be created.
        */

        AccountCreationViewModel.Subaccounts = function(parent) {
            this.parent = parent;

            var _creations = {
                simsme: {
                    none: { action: 'none' },
                    createNew: {
                        action: 'createNew',
                        name: null,
                        imageBase64Jpeg: null,
                        usesCustomName: function() { return this.name !== null; },
                        useCustomName: function() { this.name = ( this.name !== null ? this.name : '' ); }
                    },
                    referenceExisting: {
                        action: 'referenceExisting',
                        existingSimsmeGuid: null
                    }
                }
            };

            this.switchAccountRef = function(accountType, action) {
                this[accountType.toLowerCase()] = _creations[accountType.toLowerCase()][action];
            };

            this.simsme = _creations.simsme.none;
        };

        AccountCreationViewModel.Subaccounts.prototype = {
            constructor: AccountCreationViewModel.Subaccounts,

            requiresAccountRefOfType: function(accountType) {
                return this.parent.allowedOutChannels.map(function(item) { return item.toLowerCase(); }).indexOf(accountType.toLowerCase()) > -1;
            },

            createsAccountRefOfType: function(accountType) {
                return this[accountType.toLowerCase()] && this[accountType.toLowerCase()].action !== 'none';
            },

            createsAccountRefOfTypeWithAction: function(accountType, action) {
                return this[accountType.toLowerCase()] && this[accountType.toLowerCase()].action === action;
            }
        };

        return AccountCreationViewModel;
    }

    angular.module('viewmodels.accountCreation', ['resources.accountCreation'])

    .factory('AccountCreationViewModel', ['AccountCreation', AccountCreationViewModelFactory]);
})();
