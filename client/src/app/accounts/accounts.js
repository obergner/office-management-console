angular.module('accounts', [
    'services.i18nNotifications',
    'services.apiErrorHandler',
    'resources.account',
    'ui.router',
    'ngRoute'
])

.constant('AccountSettings', {
    outChannels: ['SIMSme', 'USSD', 'FlashSMS']
})

.config(['$stateProvider', function ($stateProvider) {
    $stateProvider
    .state('accounts', {
        url: '/accounts',
        templateUrl:'accounts/accounts-list.tpl.html',
        controller:'AccountsViewCtrl',
        resolve:{
            accounts:['Account', function (Account) {
                return Account.query();
            }]
        },
        ncyBreadcrumb: {
            label: 'Accounts'
        }
    })
    .state('accounts.new', {
        parent: 'accounts',
        url: '/new',
        onEnter: ['$modal', function($modal) {
            $modal.open({
                templateUrl: "accounts/account-new.tpl.html",
                size: 'lg',
                resolve: {
                    newAccount: ['Account', function (Account) {
                        return new Account();
                    }]
                },
                controller: 'CreateAccountCtrl'
            });
        }],
        ncyBreadcrumb: {
            label: 'New Account'
        }
    })
    .state('accounts.edit', {
        parent: 'accounts',
        url: '/edit/:uuid',
        onEnter: ['$modal', '$stateParams', function($modal, $stateParams) {
            $modal.open({
                templateUrl: "accounts/account-edit.tpl.html",
                size: 'lg',
                resolve: {
                    accountToUpdate: ['Account', function (Account) {
                        return Account.get({uuid: $stateParams['uuid']});
                    }]
                },
                controller: 'UpdateAccountCtrl'
            });
        }],
        ncyBreadcrumb: {
            label: 'Edit Account'
        }
    })
    .state('accounts.delete', {
        parent: 'accounts',
        url: '/delete/:uuid',
        onEnter: ['$modal', '$stateParams', function($modal, $stateParams) {
            $modal.open({
                templateUrl: "accounts/account-delete.tpl.html",
                size: 'lg',
                resolve: {
                    accountToDelete: ['Account', function (Account) {
                        return Account.get({uuid: $stateParams['uuid']});
                    }]
                },
                controller: 'DeleteAccountCtrl'
            });
        }],
        ncyBreadcrumb: {
            label: 'Delete Account'
        }
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

.controller('CreateAccountCtrl', ['$scope', '$location', '$modalInstance', '$state', 'i18nNotifications', 'apiErrorHandler', 'AccountSettings', 'newAccount', 
    function ($scope, $location, $modalInstance, $state, i18nNotifications, apiErrorHandler, AccountSettings, newAccount) {

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
                    i18nNotifications.pushForCurrentRoute('crud.account.create.success', 'success', {account : createdAccount});
                    $state.go('accounts', {}, { reload: true });
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

.controller('UpdateAccountCtrl', ['$scope', '$location', '$modalInstance', '$state', 'i18nNotifications', 'apiErrorHandler', 'AccountSettings', 'accountToUpdate', 
    function ($scope, $location, $modalInstance, $state, i18nNotifications, apiErrorHandler, AccountSettings, accountToUpdate) {

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
                    i18nNotifications.pushForCurrentRoute('crud.account.update.success', 'success', {account : updatedAccount});
                    $state.go('accounts', {}, { reload: true });
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

.controller('DeleteAccountCtrl', ['$scope', '$location', '$modalInstance', '$state', 'i18nNotifications', 'apiErrorHandler', 'accountToDelete', 
    function ($scope, $location, $modalInstance, $state, i18nNotifications, apiErrorHandler, accountToDelete) {

        $scope.accountToDelete = accountToDelete;
        $scope.alerts = [];

        $scope.dismissAlert = function() {
            $scope.alerts.length = 0;
        };

        $scope.ok = function () {
            $scope.dismissAlert();
            $scope.accountToDelete.$delete(
                function() {
                    $modalInstance.close($scope.accountToDelete);
                    i18nNotifications.pushForCurrentRoute('crud.account.delete.success', 'success', {account : $scope.accountToDelete});
                    $state.go('accounts', {}, { reload: true });
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
