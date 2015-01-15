(function() {
    function AccountCreationFactory($resource) {
        var AccountCreation = $resource('/accountcreations', {} , {
            save: {
                method: 'POST'
            }
        });

        return AccountCreation;
    }

    angular
    .module('accounts.accountCreationResource', ['ngResource'])
    .factory('AccountCreation', ['$resource', AccountCreationFactory]);
})();
