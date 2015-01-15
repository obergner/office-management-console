describe('resource: Account', function () {
    var $httpBackend;
    var $rootScope;
    var Account;

    var allAccounts = [
        { uuid: '789665-65432-78944', name: 'First Office Account', mmaId: '78345678' },
        { uuid: '789665-65432-38944', name: 'Second Office Account', mmaId: '78345677' },
        { uuid: '789665-65432-18944', name: 'Third Office Account', mmaId: '78345676' },
        { uuid: '789665-65432-98944', name: 'Fourth Office Account', mmaId: '78345675' },
        { uuid: '789665-65432-48944', name: 'Fifth Office Account', mmaId: '78345674' }
    ];

    beforeEach(function(){
        jasmine.addMatchers({
            toEqualData: function() {
                return {
                    compare: function(actual, expected) {
                        return {
                            pass: angular.equals(actual, expected),
                            message: actual + ' is not equal to ' + expected
                        };
                    }
                };
            }});
    });

    beforeEach(module('app'));

    beforeEach(module('accounts.accountResource'));

    beforeEach(inject(function ($injector) {
        $httpBackend = $injector.get('$httpBackend');
        $rootScope = $injector.get('$rootScope');
        Account = $injector.get('Account');
    }));

    describe('query()', function() {

        it('should return an array of all accounts', function () {
            $httpBackend.expect('GET', '/accounts').respond(200, allAccounts);

            var accounts = Account.query();
            $httpBackend.flush();

            expect(accounts).toEqualData(allAccounts);
        });
    });

    describe('get()', function() {

        it('should issue a GET', function () {
            var accountUuid = '89999-89-7777-675';
            var accountToReturn = new Account();
            accountToReturn.uuid = accountUuid;
            accountToReturn.name = 'Account to return';
            accountToReturn.mmaId = 334456666;
            accountToReturn.allowedOutChannels = [];
            $httpBackend.expect('GET', '/accounts/uuid/' + accountUuid).respond(200, angular.toJson(accountToReturn));

            var returnedAccount = Account.get({uuid: accountUuid});
            $httpBackend.flush();
        });
    });

    describe('$update()', function() {

        it('should issue a PUT', function () {
            var accountToUpdate = new Account();
            accountToUpdate.uuid = '56711234-897654-89';
            accountToUpdate.name = 'Account to update';
            accountToUpdate.mmaId = 123345666;
            accountToUpdate.allowedOutChannels = [];
            $httpBackend.expect('PUT', '/accounts/uuid/' + accountToUpdate.uuid).respond(200);

            accountToUpdate.$update();
            $httpBackend.flush();
        });
    });
});
