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
            $controller('AccountsViewCtrl', { $scope: $scope, $state: $state, accounts: accounts });
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

        it("should attach the list of accounts to the scope", inject(function($state) {
            var $scope = {},
            accounts = createMockAccountList();

            runController($scope, $state, accounts);
            expect($scope.accounts).toBe(accounts);
        }));
    });

    describe('editAccount(account)', function() {

        it('should cause a transition into state accounts.edit', inject(function($location, $state, $timeout, $rootScope) {
            $rootScope.$on('$stateChangeStart', function(event, toState, toParams, fromState, fromParams) { 
                console.log('$stateChangeStart: ' + event + ' - ' + fromState + ' -> ' + toState);
            });
            $rootScope.$on('$stateChangeError', function(event, toState, toParams, fromState, fromParams, error) { 
                console.log('$stateChaneError: ' + event + ' - ' + fromState + ' -> ' + toState + ': ' + error);
            });
            $rootScope.$on('$stateChangeSuccess', function(event, toState, toParams, fromState, fromParams) { 
                console.log('$stateChangeSuccess: ' + event + ' - ' + fromState + ' -> ' + toState);
            });
            spyOn($state, 'go');
            var $scope = $rootScope.$new();
            var accounts = createMockAccountList();
            runController($scope, $state, accounts);

            $scope.editAccount(accounts[0]);
            $rootScope.$apply();
            // See: http://stackoverflow.com/questions/25502568/angularjs-ui-router-test-ui-sref
            $timeout.flush();

            expect($state.go).toHaveBeenCalledWith('accounts.edit', { uuid: accounts[0].uuid });
            //expect($state.is('accounts.edit')).toBe(true);
        }));
    });
});
