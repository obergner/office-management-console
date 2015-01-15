describe('resource: AccountCreation', function () {
    var $httpBackend;
    var $rootScope;
    var AccountCreation;

    beforeEach(module('app'));

    beforeEach(module('accounts.accountCreationResource'));

    beforeEach(inject(function ($injector) {
        $httpBackend = $injector.get('$httpBackend');
        $rootScope = $injector.get('$rootScope');
        AccountCreation = $injector.get('AccountCreation');
    }));

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
            $httpBackend.expect('POST', '/accountcreations').respond(201, angular.toJson(createdAccount));

            accountToCreate.$save();
            $httpBackend.flush();
        });
    });

});
