describe('resource: Account', function () {
    var $httpBackend;
    var $rootScope;
    var Account;

    var allAccounts = [
        { uuid: '789665-65432-78944', name: 'First Office Account', mmaId: '78345678', simsmeAccountRefCreation: { action: 'none' } },
        { uuid: '789665-65432-38944', name: 'Second Office Account', mmaId: '78345677', simsmeAccountRefCreation: { action: 'none' } },
        { uuid: '789665-65432-18944', name: 'Third Office Account', mmaId: '78345676', simsmeAccountRefCreation: { action: 'none' } },
        { uuid: '789665-65432-98944', name: 'Fourth Office Account', mmaId: '78345675', simsmeAccountRefCreation: { action: 'none' } },
        { uuid: '789665-65432-48944', name: 'Fifth Office Account', mmaId: '78345674', simsmeAccountRefCreation: { action: 'none' } }
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

    beforeEach(module('resources.account'));

    beforeEach(inject(function ($injector) {
        $httpBackend = $injector.get('$httpBackend');
        $rootScope = $injector.get('$rootScope');
        Account = $injector.get('Account');
    }));

    it('should return an array of all accounts in response to query()', function () {
        $httpBackend.expect('GET', '/accounts').respond(200, allAccounts);
        var accounts = Account.query();
        $httpBackend.flush();
        expect(accounts).toEqualData(allAccounts);
    });

    it('should issue a PUT in response to $update()', function () {
        var accountToUpdate = new Account();
        accountToUpdate.uuid = '56711234-897654-89';
        accountToUpdate.name = 'Account to update';
        accountToUpdate.mmaId = 123345666;
        accountToUpdate.allowedOutChannels = [];

        $httpBackend.expect('PUT', '/accounts/uuid/' + accountToUpdate.uuid).respond(200);
        accountToUpdate.$update();
        $httpBackend.flush();
    });

    it('should issue a POST in response to $save()', function () {
        var accountToCreate = new Account();
        accountToCreate.name = 'Account to update';
        accountToCreate.mmaId = 123345666;
        accountToCreate.allowedOutChannels = [];

        var createdAccount = new Account();
        createdAccount.uuid = '7864532-89765-98777-65';
        createdAccount.name = accountToCreate.name;
        createdAccount.mmaId = accountToCreate.mmaId;
        createdAccount.allowedOutChannesl = accountToCreate.allowedOutChannels;

        $httpBackend.expect('POST', '/accounts/creations').respond(201, angular.toJson(createdAccount));
        accountToCreate.$save();
        $httpBackend.flush();
    });

    it('should issue a GET in response to get()', function () {
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

    it('should return from get() a properly transformed Account instance', function () {
        var accountUuid = '563422-89-7777-675';
        var accountToReturn = new Account();
        accountToReturn.uuid = accountUuid;
        accountToReturn.name = 'Properly transformed account';
        accountToReturn.mmaId = 111111897645;
        accountToReturn.allowedOutChannels = [];

        $httpBackend.expect('GET', '/accounts/uuid/' + accountUuid).respond(200, angular.toJson(accountToReturn));
        var returnedAccount = Account.get({uuid: accountUuid});
        $httpBackend.flush();

        expect(returnedAccount.requireSimsmeSubaccount).toBeDefined();
        expect(returnedAccount.simsmeAccountRefCreation.action).toEqual('none');

        returnedAccount.requireSimsmeSubaccount();
        expect(returnedAccount.simsmeAccountRefCreation).toBeDefined();

        returnedAccount.unrequireSimsmeSubaccount();
        expect(returnedAccount.simsmeAccountRefCreation.action).toEqual('none');

        returnedAccount.allowedOutChannels.push('SIMSme');
        expect(returnedAccount.requiresSimsmeSubaccount()).toBeTruthy();
    });

});
