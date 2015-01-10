angular.module('viewmodels.accountCreation', ['resources.accountCreation'])

.factory('AccountCreationViewModel', ['AccountCreation', function(AccountCreation){

    var AccountCreationViewModel = function() {
        this.AvailableOutChannels = ['FlashSMS', 'USSD', 'SIMSme'];

        this.name = '';
        this.mmaId = null;
        this.allowedOutChannels = [];

        this.subaccounts = new AccountCreationViewModel.Subaccounts(this);
    };

    AccountCreationViewModel.prototype.save = function(onSuccess, onError) {
        var accountCreationResource = new AccountCreation();
        accountCreationResource.name = this.name;
        accountCreationResource.mmaId = this.mmaId;
        accountCreationResource.allowedOutChannels = this.allowedOutChannels;
        if (this.subaccounts.createsSimsmeAccountRef()) {
            accountCreationResource.simsmeAccountRefCreation = this.subaccounts.simsme;
        }

        accountCreationResource.$save(onSuccess, onError);
    };

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

        this.simsme = _creations.simsme.none;
        this.simsmeSwitchTo = function(action) {
            this.simsme = _creations.simsme[action];
        };
    };

    AccountCreationViewModel.Subaccounts.prototype.requiresSimsmeSubaccount = function() {
        return this.parent.allowedOutChannels.indexOf('SIMSme') > -1;
    };

    AccountCreationViewModel.Subaccounts.prototype.createsSimsmeAccountRef = function() {
        return this.simsme.action !== 'none';
    };

    AccountCreationViewModel.Subaccounts.prototype.createsNewSimsmeAccount = function() {
        return this.simsme.action === 'createNew';
    };

    AccountCreationViewModel.Subaccounts.prototype.referencesExistingSimsmeAccount = function() {
        return this.simsme.action === 'referenceExisting';
    };

    return AccountCreationViewModel;
}]);
