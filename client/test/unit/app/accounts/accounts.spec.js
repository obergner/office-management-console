describe('AccountsViewCtrl', function() {

    beforeEach(function() {
        module('accounts', function($provide) {
            var accMock = {
                get: function(uuid) {
                    return {
                        uuid: uuid,
                        name: 'Name',
                        mmaId: 12345678,
                        createdAt: 88888888888,
                        allowedOutChannels: ['Ch1', 'Ch2']
                    };
                }
            };
            $provide.value('Account', AccountMock = accMock);
        });

        inject(function($templateCache) {
            $templateCache.put('accounts/accounts-new.tpl.html', '');
            $templateCache.put('accounts/accounts-edit.tpl.html', '');
        });
    });

    function runController($scope, $state, accounts) {
        inject(function($controller) {
            $controller('AccountsListController', { $scope: $scope, $state: $state, accounts: accounts });
        });
    }

    function createMockAccount(uuid) {
        return {
            uuid: uuid,
            name: "Name " + uuid,
            mma: 123456789 
        };
    }

    function createMockAccountList() {
        return [ createMockAccount('account-id') ];
    }

    describe('index', function() {

        it("should attach the list of accounts to the scope", inject(function($state, $rootScope) {
            var $scope = $rootScope.$new(),
            accounts = createMockAccountList();

            runController($scope, $state, accounts);
            expect($scope.accounts).toBe(accounts);
        }));
    });
});
