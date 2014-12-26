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

    beforeEach(module('resources.account'));

    beforeEach(inject(function ($injector) {
        $httpBackend = $injector.get('$httpBackend');
        $rootScope = $injector.get('$rootScope');
        Account = $injector.get('Account');
    }));

    describe('an instance created by new', function() {

        it('should expose all additional methods', function() {
            var account = new Account();

            expect(account.requireSimsmeSubaccount).toBeDefined();
            account.requireSimsmeSubaccount();
            expect(account.simsmeAccountRefCreation).toBeDefined();

            expect(account.unrequireSimsmeSubaccount).toBeDefined();
            account.unrequireSimsmeSubaccount();
            expect(account.simsmeAccountRefCreation.action).toEqual('none');

            expect(account.requiresSimsmeSubaccount).toBeDefined();
            account.allowedOutChannels = [];
            account.allowedOutChannels.push('SIMSme');
            expect(account.requiresSimsmeSubaccount()).toBeTruthy();
        });

        it('should remember CreateNewSimsmeAccountRefCreation', function() {
            var nameToRemember = 'nameToRemember';
            var imageToRemember = 'imageToRemember';

            var account = new Account();

            account.requireSimsmeSubaccount('createNew');
            account.simsmeAccountRefCreation.name = nameToRemember;
            account.simsmeAccountRefCreation.imageBase64Jpeg = imageToRemember;

            account.unrequireSimsmeSubaccount();
            expect(account.simsmeAccountRefCreation.action).toEqual('none');
            expect(account.simsmeAccountRefCreation.name).not.toBeDefined();
            expect(account.simsmeAccountRefCreation.imageBase64Jpeg).not.toBeDefined();

            account.requireSimsmeSubaccount('createNew');
            expect(account.simsmeAccountRefCreation.action).toEqual('createNew');
            expect(account.simsmeAccountRefCreation.name).toEqual(nameToRemember);
            expect(account.simsmeAccountRefCreation.imageBase64Jpeg).toEqual(imageToRemember);
        });

        it('should expose additional methods on CreateNewSimsmeAccountRefCreation', function() {
            var account = new Account();

            expect(account.simsmeAccountRefCreation).not.toBeDefined();

            account.requireSimsmeSubaccount('createNew');
            expect(account.simsmeAccountRefCreation.useCustomName).toBeDefined();
            expect(account.simsmeAccountRefCreation.usesCustomName).toBeDefined();

            expect(account.simsmeAccountRefCreation.usesCustomName()).toBeFalsy();
            account.simsmeAccountRefCreation.useCustomName();
            expect(account.simsmeAccountRefCreation.usesCustomName()).toBeTruthy();

            var expectedName = 'custom name';
            account.simsmeAccountRefCreation.name = expectedName;
            account.simsmeAccountRefCreation.useCustomName();
            expect(account.simsmeAccountRefCreation.name).toEqual(expectedName);
        });

        it('should remember ReferenceExistingSimsmeAccountRefCreation', function() {
            var simsmeGuidToRemember = 'simsmeGuidToRemember';

            var account = new Account();

            account.requireSimsmeSubaccount('referenceExisting');
            account.simsmeAccountRefCreation.existingSimsmeGuid = simsmeGuidToRemember;

            account.unrequireSimsmeSubaccount();
            expect(account.simsmeAccountRefCreation.action).toEqual('none');
            expect(account.simsmeAccountRefCreation.existingSimsmeGuid).not.toBeDefined();

            account.requireSimsmeSubaccount('referenceExisting');
            expect(account.simsmeAccountRefCreation.action).toEqual('referenceExisting');
            expect(account.simsmeAccountRefCreation.existingSimsmeGuid).toEqual(simsmeGuidToRemember);
        });

        it('should expose conditionally activated SIMSme GUID pattern', function() {
            var validSimsmeGuid = '0:{88888888-4444-4444-4444-121212121212}';
            var invalidSimsmeGuid = '0:{88888888-4444-4444-333-121212121212}';

            var account = new Account();

            expect(account.existingSimsmeGuidPattern).toBeDefined();

            account.requireSimsmeSubaccount('createNew');
            expect(account.existingSimsmeGuidPattern().test(validSimsmeGuid)).toBeTruthy();
            expect(account.existingSimsmeGuidPattern().test(invalidSimsmeGuid)).toBeTruthy();

            account.requireSimsmeSubaccount('referenceExisting');
            expect(account.existingSimsmeGuidPattern().test(validSimsmeGuid)).toBeTruthy();
            expect(account.existingSimsmeGuidPattern().test(invalidSimsmeGuid)).toBeFalsy();
        });
    });

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

        it('should return a properly initialized Account instance', function () {
            var accountUuid = '563422-89-7777-675';
            var accountToReturn = new Account();
            accountToReturn.uuid = accountUuid;
            accountToReturn.name = 'Properly initialized account';
            accountToReturn.mmaId = 111111897645;
            accountToReturn.allowedOutChannels = [];
            $httpBackend.expect('GET', '/accounts/uuid/' + accountUuid).respond(200, angular.toJson(accountToReturn));

            var returnedAccount = Account.get({uuid: accountUuid});
            $httpBackend.flush();

            expect(returnedAccount.requireSimsmeSubaccount).toBeDefined();
            returnedAccount.requireSimsmeSubaccount();
            expect(returnedAccount.simsmeAccountRefCreation).toBeDefined();

            returnedAccount.unrequireSimsmeSubaccount();
            expect(returnedAccount.simsmeAccountRefCreation.action).toEqual('none');

            returnedAccount.allowedOutChannels.push('SIMSme');
            expect(returnedAccount.requiresSimsmeSubaccount()).toBeTruthy();
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

    describe('$save()', function() {

        it('should issue a POST', function () {
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
    });

});
