describe('resource: AccountUpdate', function () {
    var $httpBackend;
    var $rootScope;
    var AccountUpdate;

    beforeEach(module('app'));

    beforeEach(module('accounts.accountUpdateResource'));

    beforeEach(inject(function ($injector) {
        $httpBackend = $injector.get('$httpBackend');
        $rootScope = $injector.get('$rootScope');
        AccountUpdate = $injector.get('AccountUpdate');
    }));

    describe('$save()', function() {

        it('should issue a POST', function () {
            var accountToUpdate = new AccountUpdate();
            accountToUpdate.uuid = '11111111-2222-2222-333333333333';
            accountToUpdate.name = 'Account to update';
            accountToUpdate.mmaId = 123345666;
            accountToUpdate.allowedOutChannels = [];

            var updatedAccount = new AccountUpdate();
            updatedAccount.uuid = '7864532-89765-98777-65';
            updatedAccount.name = accountToUpdate.name;
            updatedAccount.mmaId = accountToUpdate.mmaId;
            updatedAccount.allowedOutChannesl = accountToUpdate.allowedOutChannels;
            $httpBackend.expect('POST', '/accountupdates').respond(201, angular.toJson(updatedAccount));

            accountToUpdate.$save();
            $httpBackend.flush();
        });
    });
});
