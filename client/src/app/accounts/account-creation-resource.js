(function() {
    function AccountCreationFactory($resource) {

        var AccountCreation = $resource('/accountcreations/:uuid', { uuid: '@uuid' } , {
            save: {
                method: 'POST',
                url: '/accountcreations'
            }
        });

        return AccountCreation;
    }

    angular.module('accounts.accountCreationResource', ['ngResource'])

    .factory('AccountCreation', ['$resource', AccountCreationFactory]);
})();
