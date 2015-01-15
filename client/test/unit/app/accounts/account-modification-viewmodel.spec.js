describe('accounts: AccountModificationViewModel', function () {
    var $httpBackend;
    var $rootScope;
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

    beforeEach(module('accounts.accountModificationViewModel'));

    beforeEach(inject(function ($injector) {
        $httpBackend = $injector.get('$httpBackend');
        $rootScope = $injector.get('$rootScope');
        AccountModificationViewModel = $injector.get('AccountModificationViewModel');
    }));

    describe('an instance created by new', function() {

        it('should expose all additional methods', function() {
            var accountModificationViewModel = new AccountModificationViewModel();

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

});
