(function() {
    function AccountModificationViewModelFactory(AccountCreation) {
        /*
        * Encapsulates the Office account proper to be created. 
        */

        var AccountModificationViewModel = function() {
            this.AvailableOutChannels = ['FlashSMS', 'USSD', 'SIMSme'];

            this.name = '';
            this.mmaId = null;
            this.allowedOutChannels = [];

            this.subaccounts = new AccountModificationViewModel.Subaccounts(this);
        };

        AccountModificationViewModel.prototype = {
            constructor: AccountModificationViewModel,

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

        AccountModificationViewModel.Subaccounts = function(parent) {
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

        AccountModificationViewModel.Subaccounts.prototype = {
            constructor: AccountModificationViewModel.Subaccounts,

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

        return AccountModificationViewModel;
    }

    angular.module('accounts.accountModificationViewModel', ['accounts.accountCreationResource'])

    .factory('AccountModificationViewModel', ['AccountCreation', AccountModificationViewModelFactory]);
})();
