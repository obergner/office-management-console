describe('resource: AccountCreation', function () {
    var $httpBackend;
    var $rootScope;
    var AccountCreation;

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

    beforeEach(module('resources.accountCreation'));

    beforeEach(inject(function ($injector) {
        $httpBackend = $injector.get('$httpBackend');
        $rootScope = $injector.get('$rootScope');
        AccountCreation = $injector.get('AccountCreation');
    }));

    describe('an instance created by new', function() {

        it('should expose all additional methods', function() {
            var accountCreation = new AccountCreation();

            expect(accountCreation.requireSimsmeSubaccount).toBeDefined();
            accountCreation.requireSimsmeSubaccount('createNew');
            expect(accountCreation.simsmeAccountRefCreation).toBeDefined();

            expect(accountCreation.unrequireSimsmeSubaccount).toBeDefined();
            accountCreation.unrequireSimsmeSubaccount();
            expect(accountCreation.simsmeAccountRefCreation.action).toEqual('none');

            expect(accountCreation.requiresSimsmeSubaccount).toBeDefined();
            expect(accountCreation.requiresSimsmeSubaccount()).toBeFalsy();
            accountCreation.allowedOutChannels = [];
            accountCreation.allowedOutChannels.push('SIMSme');
            expect(accountCreation.requiresSimsmeSubaccount()).toBeTruthy();
        });

        it('should remember CreateNewSimsmeAccountRefCreation', function() {
            var nameToRemember = 'nameToRemember';
            var imageToRemember = 'imageToRemember';

            var accountCreation = new AccountCreation();

            accountCreation.requireSimsmeSubaccount('createNew');
            accountCreation.simsmeAccountRefCreation.name = nameToRemember;
            accountCreation.simsmeAccountRefCreation.imageBase64Jpeg = imageToRemember;

            accountCreation.unrequireSimsmeSubaccount();
            expect(accountCreation.simsmeAccountRefCreation.action).toEqual('none');
            expect(accountCreation.simsmeAccountRefCreation.name).not.toBeDefined();
            expect(accountCreation.simsmeAccountRefCreation.imageBase64Jpeg).not.toBeDefined();

            accountCreation.requireSimsmeSubaccount('createNew');
            expect(accountCreation.simsmeAccountRefCreation.action).toEqual('createNew');
            expect(accountCreation.simsmeAccountRefCreation.name).toEqual(nameToRemember);
            expect(accountCreation.simsmeAccountRefCreation.imageBase64Jpeg).toEqual(imageToRemember);
        });

        it('should expose additional methods on CreateNewSimsmeAccountRefCreation', function() {
            var accountCreation = new AccountCreation();

            expect(accountCreation.simsmeAccountRefCreation).not.toBeDefined();

            accountCreation.requireSimsmeSubaccount('createNew');
            expect(accountCreation.simsmeAccountRefCreation.useCustomName).toBeDefined();
            expect(accountCreation.simsmeAccountRefCreation.usesCustomName).toBeDefined();

            expect(accountCreation.simsmeAccountRefCreation.usesCustomName()).toBeFalsy();
            accountCreation.simsmeAccountRefCreation.useCustomName();
            expect(accountCreation.simsmeAccountRefCreation.usesCustomName()).toBeTruthy();

            var expectedName = 'custom name';
            accountCreation.simsmeAccountRefCreation.name = expectedName;
            accountCreation.simsmeAccountRefCreation.useCustomName();
            expect(accountCreation.simsmeAccountRefCreation.name).toEqual(expectedName);
        });

        it('should remember ReferenceExistingSimsmeAccountRefCreation', function() {
            var simsmeGuidToRemember = 'simsmeGuidToRemember';

            var accountCreation = new AccountCreation();

            accountCreation.requireSimsmeSubaccount('referenceExisting');
            accountCreation.simsmeAccountRefCreation.existingSimsmeGuid = simsmeGuidToRemember;

            accountCreation.unrequireSimsmeSubaccount();
            expect(accountCreation.simsmeAccountRefCreation.action).toEqual('none');
            expect(accountCreation.simsmeAccountRefCreation.existingSimsmeGuid).not.toBeDefined();

            accountCreation.requireSimsmeSubaccount('referenceExisting');
            expect(accountCreation.simsmeAccountRefCreation.action).toEqual('referenceExisting');
            expect(accountCreation.simsmeAccountRefCreation.existingSimsmeGuid).toEqual(simsmeGuidToRemember);
        });
    });

    describe('$save()', function() {

        it('should issue a POST', function () {
            var accountToCreate = new AccountCreation();
            accountToCreate.name = 'Account to update';
            accountToCreate.mmaId = 123345666;
            accountToCreate.allowedOutChannels = [];

            var createdAccount = new AccountCreation();
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
