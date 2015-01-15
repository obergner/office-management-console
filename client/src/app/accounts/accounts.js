(function() {
    angular.module('accounts', [
        'accounts.list',
        'accounts.new',
        'accounts.edit',
        'accounts.delete',
        'accounts.accountModificationForm',
        'accounts.accountModificationViewModel',
        'accounts.accountResource',
        'services.localizedMessages',
        'services.apiErrorHandler',
        'ui.router'
    ])

    .constant('AccountSettings', {
        outChannels: ['SIMSme', 'USSD', 'FlashSMS']
    })

    .config(['$stateProvider', function ($stateProvider) {
        $stateProvider
        .state('accounts', {
            url: '/accounts',
            views: {
                'mainContentPane': {
                    templateUrl:'accounts/accounts-list.tpl.html',
                    controller:'AccountsListController',
                    resolve:{
                        accounts:['Account', function (Account) {
                            return Account.query();
                        }]
                    }
                },
                'actionBar': {
                    templateUrl: 'accounts/account-action-bar.tpl.html'
                }
            }
        })
        .state('accounts.new', {
            parent: 'accounts',
            url: '/new',
            onEnter: ['$modal', function($modal) {
                $modal.open({
                    templateUrl: "accounts/account-new.tpl.html",
                    size: 'lg',
                    backdrop: false,
                    resolve: {
                        newAccount: ['AccountModificationViewModel', function (AccountModificationViewModel) {
                            return new AccountModificationViewModel();
                        }]
                    },
                    controller: 'NewAccountController'
                });
            }]
        })
        .state('accounts.edit', {
            parent: 'accounts',
            url: '/edit/:uuid',
            onEnter: ['$modal', '$stateParams', function($modal, $stateParams) {
                $modal.open({
                    templateUrl: "accounts/account-edit.tpl.html",
                    size: 'lg',
                    backdrop: false,
                    resolve: {
                        accountToUpdate: ['Account', function (Account) {
                            return Account.get({uuid: $stateParams.uuid});
                        }]
                    },
                    controller: 'EditAccountController'
                });
            }]
        })
        .state('accounts.delete', {
            parent: 'accounts',
            url: '/delete/:uuid',
            onEnter: ['$modal', '$stateParams', function($modal, $stateParams) {
                $modal.open({
                    templateUrl: "accounts/account-delete.tpl.html",
                    size: 'lg',
                    backdrop: false,
                    resolve: {
                        accountToDelete: ['Account', function (Account) {
                            return Account.get({uuid: $stateParams.uuid});
                        }]
                    },
                    controller: 'DeleteAccountController'
                });
            }]
        });
    }]);
})();

/*
* Common base object for controllers manipulating accounts.
*/

function BaseAccountController($scope, account, accountFormName, apiErrorHandler) {
    $scope.account = account;
    $scope.apiErrors = apiErrorHandler;

    $scope.onOutChannelSelected = function(outChannel, allOutChannels) {
        if (outChannel === 'SIMSme') {
            $scope.account.subaccounts.switchAccountRef('SIMSme', 'createNew');
        }
    };

    $scope.onOutChannelDeselected = function(outChannel, allOutChannels) {
        if (outChannel === 'SIMSme') {
            $scope.account.subaccounts.switchAccountRef('SIMSme', 'none');
        }
    };

    $scope.isValidInput = function() {
        return ($scope[accountFormName].$valid && (!$scope.account.subaccounts.requiresAccountRefOfType('SIMSme') ? true : ($scope.account.subaccounts.createsAccountRefOfTypeWithAction('SIMSme', 'createNew') ? $scope.createNewSimsmeSubaccountForm.$valid : $scope.referenceExistingSimsmeSubaccountForm.$valid)));
    };

    $scope.onSimsmeAccountRefCreationActionChanged = function(action) {
        switch(action) {
            case 'createNew':
                $scope[accountFormName].$removeControl($scope.referenceExistingSimsmeSubaccountForm);
                if (!($scope.createNewSimsmeSubaccountForm.$name in $scope[accountFormName])) {
                    $scope[accountFormName].$addControl($scope.createNewSimsmeSubaccountForm);
                }
                $scope.account.subaccounts.switchAccountRef('SIMSme', 'createNew');
                break;
            case 'referenceExisting':
                $scope[accountFormName].$removeControl($scope.createNewSimsmeSubaccountForm);
                if (!($scope.referenceExistingSimsmeSubaccountForm.$name in $scope[accountFormName])) {
                    $scope[accountFormName].$addControl($scope.referenceExistingSimsmeSubaccountForm);
                }
                $scope.account.subaccounts.switchAccountRef('SIMSme', 'referenceExisting');
                break;
            default:
                throw new Error('Unknown action: ' + action);
        }
    };
}
