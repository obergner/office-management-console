describe('accounts: AccountModificationViewModel', function () {
    var $httpBackend;
    var $rootScope;
    var Account;
    var AccountModificationViewModel;

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

    beforeEach(module('accounts.accountResource'));

    beforeEach(module('accounts.accountModificationViewModel'));

    beforeEach(inject(function ($injector) {
        $httpBackend = $injector.get('$httpBackend');
        $rootScope = $injector.get('$rootScope');
        Account = $injector.get('Account');
        AccountModificationViewModel = $injector.get('AccountModificationViewModel');
    }));

    describe('an instance created by new with no argument passed in', function() {

        it('should expose all additional methods', function() {
            var accountModificationViewModel = new AccountModificationViewModel();

            expect(accountModificationViewModel.isUpdate).toBeDefined();
            expect(accountModificationViewModel.isUpdate()).toBeFalsy();

            expect(accountModificationViewModel.subaccounts.simsme).toBeDefined();
            expect(accountModificationViewModel.subaccounts.simsme.action).toEqual('none');

            expect(accountModificationViewModel.subaccounts.requiresAccountRefOfType('SIMSme')).toBeDefined();
            expect(accountModificationViewModel.subaccounts.requiresAccountRefOfType('SIMSme')).toBeFalsy();
            accountModificationViewModel.allowedOutChannels.push('SIMSme');
            expect(accountModificationViewModel.subaccounts.requiresAccountRefOfType('SIMSme')).toBeTruthy();

            expect(accountModificationViewModel.subaccounts.createsAccountRefOfType).toBeDefined();
            expect(accountModificationViewModel.subaccounts.createsAccountRefOfType('Nonsense')).toBeFalsy();
            expect(accountModificationViewModel.subaccounts.createsAccountRefOfType('SIMSme')).toBeFalsy();
            accountModificationViewModel.subaccounts.switchAccountRef('SIMSme', 'createNew');
            expect(accountModificationViewModel.subaccounts.createsAccountRefOfType('SIMSme')).toBeTruthy();

            expect(accountModificationViewModel.subaccounts.createsAccountRefOfTypeWithAction).toBeDefined();
            expect(accountModificationViewModel.subaccounts.createsAccountRefOfTypeWithAction('Nonsense', 'rubbish')).toBeFalsy();
            expect(accountModificationViewModel.subaccounts.createsAccountRefOfTypeWithAction('SIMSme', 'rubbish')).toBeFalsy();
            accountModificationViewModel.subaccounts.switchAccountRef('SIMSme', 'createNew');
            expect(accountModificationViewModel.subaccounts.createsAccountRefOfTypeWithAction('SIMSme', 'createNew')).toBeTruthy();
            accountModificationViewModel.subaccounts.switchAccountRef('SIMSme', 'referenceExisting');
            expect(accountModificationViewModel.subaccounts.createsAccountRefOfTypeWithAction('SIMSme', 'referenceExisting')).toBeTruthy();

            expect(accountModificationViewModel.subaccounts.supportsRemovingAccountRefOfType).toBeDefined();
            expect(accountModificationViewModel.subaccounts.supportsRemovingAccountRefOfType('SIMSme')).toBeFalsy();
        });

        it('should remember CreateNewSimsmeAccountRefModification', function() {
            var nameToRemember = 'nameToRemember';
            var imageToRemember = 'imageToRemember';

            var accountModificationViewModel = new AccountModificationViewModel();

            accountModificationViewModel.subaccounts.switchAccountRef('SIMSme', 'createNew');
            accountModificationViewModel.subaccounts.simsme.name = nameToRemember;
            accountModificationViewModel.subaccounts.simsme.imageBase64Jpeg = imageToRemember;

            accountModificationViewModel.subaccounts.switchAccountRef('SIMSme', 'none');
            expect(accountModificationViewModel.subaccounts.simsme.action).toEqual('none');
            expect(accountModificationViewModel.subaccounts.simsme.name).not.toBeDefined();
            expect(accountModificationViewModel.subaccounts.simsme.imageBase64Jpeg).not.toBeDefined();

            accountModificationViewModel.subaccounts.switchAccountRef('SIMSme', 'createNew');
            expect(accountModificationViewModel.subaccounts.simsme.action).toEqual('createNew');
            expect(accountModificationViewModel.subaccounts.simsme.name).toEqual(nameToRemember);
            expect(accountModificationViewModel.subaccounts.simsme.imageBase64Jpeg).toEqual(imageToRemember);
        });

        it('should expose additional methods on CreateNewSimsmeAccountRefModification', function() {
            var accountModificationViewModel = new AccountModificationViewModel();

            accountModificationViewModel.subaccounts.switchAccountRef('SIMSme', 'createNew');
            expect(accountModificationViewModel.subaccounts.simsme.useCustomName).toBeDefined();
            expect(accountModificationViewModel.subaccounts.simsme.usesCustomName).toBeDefined();

            expect(accountModificationViewModel.subaccounts.simsme.usesCustomName()).toBeFalsy();
            accountModificationViewModel.subaccounts.simsme.useCustomName();
            expect(accountModificationViewModel.subaccounts.simsme.usesCustomName()).toBeTruthy();

            var expectedName = 'custom name';
            accountModificationViewModel.subaccounts.simsme.name = expectedName;
            accountModificationViewModel.subaccounts.simsme.useCustomName();
            expect(accountModificationViewModel.subaccounts.simsme.name).toEqual(expectedName);
        });

        it('should remember ReferenceExistingSimsmeAccountRefModification', function() {
            var simsmeGuidToRemember = 'simsmeGuidToRemember';

            var accountModificationViewModel = new AccountModificationViewModel();

            accountModificationViewModel.subaccounts.switchAccountRef('SIMSme', 'referenceExisting');
            accountModificationViewModel.subaccounts.simsme.existingSimsmeGuid = simsmeGuidToRemember;

            accountModificationViewModel.subaccounts.switchAccountRef('SIMSme', 'none');
            expect(accountModificationViewModel.subaccounts.simsme.action).toEqual('none');
            expect(accountModificationViewModel.subaccounts.simsme.existingSimsmeGuid).not.toBeDefined();

            accountModificationViewModel.subaccounts.switchAccountRef('SIMSme', 'referenceExisting');
            expect(accountModificationViewModel.subaccounts.simsme.action).toEqual('referenceExisting');
            expect(accountModificationViewModel.subaccounts.simsme.existingSimsmeGuid).toEqual(simsmeGuidToRemember);
        });
    });

    describe('save()', function() {

        it('should issue a POST', function () {
            var accountToCreate = new AccountModificationViewModel();
            accountToCreate.name = 'Account to update';
            accountToCreate.mmaId = 123345666;
            accountToCreate.allowedOutChannels = [];

            var createdAccount = {};
            createdAccount.uuid = '7864532-89765-98777-65';
            createdAccount.name = accountToCreate.name;
            createdAccount.mmaId = accountToCreate.mmaId;
            createdAccount.allowedOutChannesl = accountToCreate.allowedOutChannels;
            $httpBackend.expect('POST', '/accountcreations').respond(201, angular.toJson(createdAccount));

            accountToCreate.save();
            $httpBackend.flush();
        });
    });

    describe('an instance created by new with an account to update passed in', function() {

        it('should expose all additional methods', function() {
            var accountToUpdate = new Account();
            accountToUpdate.uuid = '11111111-2222-3333-4444-555555555555';
            accountToUpdate.name = 'Account to update';
            accountToUpdate.mmaId = 3333333333;
            accountToUpdate.allowedOutChannels = ['ch 1', 'ch 2'];
            accountToUpdate.simsmeAccountRef = { simsmeGuid: '0:{22222222-3333-4444-5555-666666666666}' };

            var accountModificationViewModel = new AccountModificationViewModel(accountToUpdate);

            expect(accountModificationViewModel.isUpdate).toBeDefined();
            expect(accountModificationViewModel.isUpdate()).toBeTruthy();

            expect(accountModificationViewModel.subaccounts.simsme).toBeDefined();
            expect(accountModificationViewModel.subaccounts.simsme.action).toEqual('referenceExisting');

            expect(accountModificationViewModel.subaccounts.requiresAccountRefOfType('SIMSme')).toBeDefined();
            expect(accountModificationViewModel.subaccounts.requiresAccountRefOfType('SIMSme')).toBeFalsy();
            accountModificationViewModel.allowedOutChannels.push('SIMSme');
            expect(accountModificationViewModel.subaccounts.requiresAccountRefOfType('SIMSme')).toBeTruthy();

            expect(accountModificationViewModel.subaccounts.createsAccountRefOfType).toBeDefined();
            expect(accountModificationViewModel.subaccounts.createsAccountRefOfType('Nonsense')).toBeFalsy();
            expect(accountModificationViewModel.subaccounts.createsAccountRefOfType('SIMSme')).toBeTruthy();
            accountModificationViewModel.subaccounts.switchAccountRef('SIMSme', 'createNew');
            expect(accountModificationViewModel.subaccounts.createsAccountRefOfType('SIMSme')).toBeTruthy();

            expect(accountModificationViewModel.subaccounts.createsAccountRefOfTypeWithAction).toBeDefined();
            expect(accountModificationViewModel.subaccounts.createsAccountRefOfTypeWithAction('Nonsense', 'rubbish')).toBeFalsy();
            expect(accountModificationViewModel.subaccounts.createsAccountRefOfTypeWithAction('SIMSme', 'rubbish')).toBeFalsy();
            accountModificationViewModel.subaccounts.switchAccountRef('SIMSme', 'createNew');
            expect(accountModificationViewModel.subaccounts.createsAccountRefOfTypeWithAction('SIMSme', 'createNew')).toBeTruthy();
            accountModificationViewModel.subaccounts.switchAccountRef('SIMSme', 'referenceExisting');
            expect(accountModificationViewModel.subaccounts.createsAccountRefOfTypeWithAction('SIMSme', 'referenceExisting')).toBeTruthy();

            expect(accountModificationViewModel.subaccounts.supportsRemovingAccountRefOfType).toBeDefined();
            expect(accountModificationViewModel.subaccounts.supportsRemovingAccountRefOfType('SIMSme')).toBeTruthy();
        });

        it('should correctly initialize all fields', function() {
            var accountToUpdate = new Account();
            accountToUpdate.uuid = '11111111-2222-3333-4444-555555555555';
            accountToUpdate.name = 'Account to update';
            accountToUpdate.mmaId = 3333333333;
            accountToUpdate.allowedOutChannels = ['ch 1', 'ch 2'];
            accountToUpdate.simsmeAccountRef = { simsmeGuid: '0:{22222222-3333-4444-5555-666666666666}' };

            var accountModificationViewModel = new AccountModificationViewModel(accountToUpdate);

            expect(accountModificationViewModel.uuid).toEqual(accountToUpdate.uuid);
            expect(accountModificationViewModel.name).toEqual(accountToUpdate.name);
            expect(accountModificationViewModel.mmaId).toEqual(accountToUpdate.mmaId);
            expect(accountModificationViewModel.allowedOutChannels).toEqual(accountToUpdate.allowedOutChannels);
            expect(accountModificationViewModel.subaccounts.simsme.existingSimsmeGuid).toEqual(accountToUpdate.simsmeAccountRef.simsmeGuid);
        });
    });
});
