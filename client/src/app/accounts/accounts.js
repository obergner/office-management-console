angular.module('accounts', [
    'services.localizedMessages',
    'services.apiErrorHandler',
    'resources.account',
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
                controller:'AccountsViewCtrl',
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
                    newAccount: ['Account', function (Account) {
                        return new Account();
                    }]
                },
                controller: 'CreateAccountCtrl'
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
                controller: 'UpdateAccountCtrl'
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
                controller: 'DeleteAccountCtrl'
            });
        }]
    });
}])

.controller('AccountsViewCtrl', ['$scope', '$state', 'accounts', function ($scope, $state, accounts) {
    $scope.accounts = accounts;

    $scope.editAccount = function (account) {
        $state.go('accounts.edit', { uuid: account.uuid });
    };

    $scope.deleteAccount = function (account) {
        $state.go('accounts.delete', { uuid: account.uuid });
    };
}])

.controller('CreateAccountCtrl', ['$scope', '$modalInstance', '$state', 'localizedMessages', 'apiErrorHandler', 'growl', 'AccountSettings', 'newAccount', 
    function ($scope, $modalInstance, $state, localizedMessages, apiErrorHandler, growl, AccountSettings, newAccount) {

        $scope.newAccount = newAccount;
        $scope.alerts = [];
        $scope.availableOutChannels = AccountSettings.outChannels;

        $scope.dismissAlert = function() {
            $scope.alerts.length = 0;
        };

        $scope.ok = function () {
            $scope.dismissAlert();
            $scope.newAccount.$save(
                function(createdAccount) {
                    $modalInstance.close(createdAccount);
                    $state.go('accounts', {}, { reload: true }).then(function() {
                        growl.success(localizedMessages.get('crud.account.create.success', {account: createdAccount}), {title: 'Account created'});
                    });
                },
                function(httpResponse) {
                    var alert = apiErrorHandler.mapToAlert(httpResponse);
                    $scope.alerts.push(alert);
                });
        };

        $scope.cancel = function () {
            $modalInstance.dismiss('cancel');
            $state.go('accounts');
        };
    }
])

.controller('UpdateAccountCtrl', ['$scope', '$modalInstance', '$state', 'localizedMessages', 'apiErrorHandler', 'growl', 'AccountSettings', 'accountToUpdate', 
    function ($scope, $modalInstance, $state, localizedMessages, apiErrorHandler, growl, AccountSettings, accountToUpdate) {

        $scope.accountToUpdate = accountToUpdate;
        $scope.alerts = [];
        $scope.availableOutChannels = AccountSettings.outChannels;

        $scope.dismissAlert = function() {
            $scope.alerts.length = 0;
        };

        $scope.ok = function () {
            $scope.dismissAlert();
            $scope.accountToUpdate.$update(
                function(updatedAccount) {
                    $modalInstance.close(updatedAccount);
                    $state.go('accounts', {}, { reload: true }).then(function() {
                        growl.success(localizedMessages.get('crud.account.update.success', {account: updatedAccount}), {title: 'Account updated'});
                    });
                },
                function(httpResponse) {
                    var alert = apiErrorHandler.mapToAlert(httpResponse);
                    $scope.alerts.push(alert);
                });
        };

        $scope.cancel = function () {
            $modalInstance.dismiss('cancel');
            $state.go('accounts');
        };
    }
])

.controller('DeleteAccountCtrl', ['$scope', '$modalInstance', '$state', 'localizedMessages', 'apiErrorHandler', 'growl', 'AccountSettings', 'accountToDelete', 
    function ($scope, $modalInstance, $state, localizedMessages, apiErrorHandler, growl, AccountSettings, accountToDelete) {

        $scope.accountToDelete = accountToDelete;
        $scope.alerts = [];
        $scope.availableOutChannels = AccountSettings.outChannels;

        $scope.dismissAlert = function() {
            $scope.alerts.length = 0;
        };

        $scope.ok = function () {
            $scope.dismissAlert();
            $scope.accountToDelete.$delete(
                function() {
                    $modalInstance.close($scope.accountToDelete);
                    $state.go('accounts', {}, { reload: true }).then(function() {
                        growl.success(localizedMessages.get('crud.account.delete.success', {account: $scope.accountToDelete}), {title: 'Account deleted'});
                    });
                },
                function(httpResponse) {
                    var alert = apiErrorHandler.mapToAlert(httpResponse);
                    $scope.alerts.push(alert);
                });
        };

        $scope.cancel = function () {
            $modalInstance.dismiss('cancel');
            $state.go('accounts');
        };
    }
]);
