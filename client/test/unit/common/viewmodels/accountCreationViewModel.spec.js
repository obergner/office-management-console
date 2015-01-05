describe('viewmodel: AccountCreationViewModel', function () {
    var $httpBackend;
    var $rootScope;
    var AccountCreationViewModel;

    beforeEach(function(){
        jasmine.addMatchers({
            toEqualsimsme: function() {
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

    beforeEach(module('viewmodels.accountCreation'));

    beforeEach(inject(function ($injector) {
        $httpBackend = $injector.get('$httpBackend');
        $rootScope = $injector.get('$rootScope');
        AccountCreationViewModel = $injector.get('AccountCreationViewModel');
    }));

    describe('an instance created by new', function() {

        it('should expose all additional methods', function() {
            var accountCreationViewModel = new AccountCreationViewModel();

            expect(accountCreationViewModel.subaccounts.simsme).toBeDefined();
            expect(accountCreationViewModel.subaccounts.simsme.action).toEqual('none');

            expect(accountCreationViewModel.subaccounts.requiresSimsmeSubaccount).toBeDefined();
            expect(accountCreationViewModel.subaccounts.requiresSimsmeSubaccount()).toBeFalsy();
            accountCreationViewModel.allowedOutChannels.push('SIMSme');
            expect(accountCreationViewModel.subaccounts.requiresSimsmeSubaccount()).toBeTruthy();
        });

        it('should remember CreateNewSimsmeAccountRefCreation', function() {
            var nameToRemember = 'nameToRemember';
            var imageToRemember = 'imageToRemember';

            var accountCreationViewModel = new AccountCreationViewModel();

            accountCreationViewModel.subaccounts.simsmeSwitchTo('createNew');
            accountCreationViewModel.subaccounts.simsme.name = nameToRemember;
            accountCreationViewModel.subaccounts.simsme.imageBase64Jpeg = imageToRemember;

            accountCreationViewModel.subaccounts.simsmeSwitchTo('none');
            expect(accountCreationViewModel.subaccounts.simsme.action).toEqual('none');
            expect(accountCreationViewModel.subaccounts.simsme.name).not.toBeDefined();
            expect(accountCreationViewModel.subaccounts.simsme.imageBase64Jpeg).not.toBeDefined();

            accountCreationViewModel.subaccounts.simsmeSwitchTo('createNew');
            expect(accountCreationViewModel.subaccounts.simsme.action).toEqual('createNew');
            expect(accountCreationViewModel.subaccounts.simsme.name).toEqual(nameToRemember);
            expect(accountCreationViewModel.subaccounts.simsme.imageBase64Jpeg).toEqual(imageToRemember);
        });

        it('should expose additional methods on CreateNewSimsmeAccountRefCreation', function() {
            var accountCreationViewModel = new AccountCreationViewModel();

            accountCreationViewModel.subaccounts.simsmeSwitchTo('createNew');
            expect(accountCreationViewModel.subaccounts.simsme.useCustomName).toBeDefined();
            expect(accountCreationViewModel.subaccounts.simsme.usesCustomName).toBeDefined();

            expect(accountCreationViewModel.subaccounts.simsme.usesCustomName()).toBeFalsy();
            accountCreationViewModel.subaccounts.simsme.useCustomName();
            expect(accountCreationViewModel.subaccounts.simsme.usesCustomName()).toBeTruthy();

            var expectedName = 'custom name';
            accountCreationViewModel.subaccounts.simsme.name = expectedName;
            accountCreationViewModel.subaccounts.simsme.useCustomName();
            expect(accountCreationViewModel.subaccounts.simsme.name).toEqual(expectedName);
        });

        it('should remember ReferenceExistingSimsmeAccountRefCreation', function() {
            var simsmeGuidToRemember = 'simsmeGuidToRemember';

            var accountCreationViewModel = new AccountCreationViewModel();

            accountCreationViewModel.subaccounts.simsmeSwitchTo('referenceExisting');
            accountCreationViewModel.subaccounts.simsme.existingSimsmeGuid = simsmeGuidToRemember;

            accountCreationViewModel.subaccounts.simsmeSwitchTo('none');
            expect(accountCreationViewModel.subaccounts.simsme.action).toEqual('none');
            expect(accountCreationViewModel.subaccounts.simsme.existingSimsmeGuid).not.toBeDefined();

            accountCreationViewModel.subaccounts.simsmeSwitchTo('referenceExisting');
            expect(accountCreationViewModel.subaccounts.simsme.action).toEqual('referenceExisting');
            expect(accountCreationViewModel.subaccounts.simsme.existingSimsmeGuid).toEqual(simsmeGuidToRemember);
        });
    });

    describe('save()', function() {

        it('should issue a POST', function () {
            var accountToCreate = new AccountCreationViewModel();
            accountToCreate.name = 'Account to update';
            accountToCreate.mmaId = 123345666;
            accountToCreate.allowedOutChannels = [];

            var createdAccount = {};
            createdAccount.uuid = '7864532-89765-98777-65';
            createdAccount.name = accountToCreate.name;
            createdAccount.mmaId = accountToCreate.mmaId;
            createdAccount.allowedOutChannesl = accountToCreate.allowedOutChannels;
            $httpBackend.expect('POST', '/accounts/creations').respond(201, angular.toJson(createdAccount));

            accountToCreate.save();
            $httpBackend.flush();
        });
    });

});
