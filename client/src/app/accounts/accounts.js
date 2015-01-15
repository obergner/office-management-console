(function() {
    angular
    .module('accounts', [
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
                        accountToUpdate: ['AccountModificationViewModel', 'Account', function (AccountModificationViewModel, Account) {
                            return Account.get({uuid: $stateParams.uuid})
                            .$promise.then(function(account) {
                                return new AccountModificationViewModel(account);
                            });
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

