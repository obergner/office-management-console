(function() {
    function AccountModificationViewModelFactory(AccountCreation, AccountUpdate) {

        /*
         * Encapsulates the Office account proper to be created.
         */
        var AccountModificationViewModel = function(accountToUpdate) {
            this.AvailableOutChannels = ['FlashSMS', 'USSD', 'SIMSme'];

            if (accountToUpdate !== undefined) {
                this.accountToUpdate = accountToUpdate;
                this.uuid = accountToUpdate.uuid;
            }

            this.name = (accountToUpdate !== undefined ? accountToUpdate.name : '');
            this.mmaId = (accountToUpdate !== undefined ? accountToUpdate.mmaId : null);
            this.allowedOutChannels = (accountToUpdate !== undefined ? accountToUpdate.allowedOutChannels : []);

            this.subaccounts = new AccountModificationViewModel.Subaccounts(this, accountToUpdate);
        };

        AccountModificationViewModel.prototype = {
            constructor: AccountModificationViewModel,

            isUpdate: function() {
                return (this.accountToUpdate !== undefined);
            },

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

            update: function(onSuccess, onError) {
                var accountUpdateResource = new AccountUpdate();
                accountUpdateResource.uuid = this.uuid;
                accountUpdateResource.name = this.name;
                accountUpdateResource.mmaId = this.mmaId;
                accountUpdateResource.allowedOutChannels = this.allowedOutChannels;
                if (this.subaccounts.createsAccountRefOfType('SIMSme')) {
                    accountUpdateResource.simsmeAccountRefModification = this.subaccounts.simsme;
                }

                accountUpdateResource.$save(onSuccess, onError);
            }
        };

        /*
         * Collection of references to nested accounts that
         * may potentially be created.
         */
        AccountModificationViewModel.Subaccounts = function(parent, accountToUpdate) {
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
                        existingSimsmeGuid: ((accountToUpdate !== undefined) && (accountToUpdate.simsmeAccountRef !== undefined) ? accountToUpdate.simsmeAccountRef.simsmeGuid : null)
                    }
                }
            };

            this.switchAccountRef = function(accountType, action) {
                this[accountType.toLowerCase()] = _creations[accountType.toLowerCase()][action];
            };

            this.simsme = ((accountToUpdate !== undefined) && (accountToUpdate.simsmeAccountRef !== undefined) && (accountToUpdate.simsmeAccountRef.simsmeGuid !== undefined) ? _creations.simsme.referenceExisting : _creations.simsme.none);
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
            },

            supportsRemovingAccountRefOfType: function(accountType) {
                return this.parent.isUpdate();
            }
        };

        return AccountModificationViewModel;
    }

    angular
    .module('accounts.accountModificationViewModel', ['accounts.accountCreationResource', 'accounts.accountUpdateResource'])
    .factory('AccountModificationViewModel', ['AccountCreation', 'AccountUpdate', AccountModificationViewModelFactory]);
})();
