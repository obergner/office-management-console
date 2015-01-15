(function() {
    function AccountUpdateFactory($resource) {
        var AccountUpdate = $resource('/accountupdates', {} , {
            save: {
                method: 'POST'
            }
        });

        return AccountUpdate;
    }

    angular
    .module('accounts.accountUpdateResource', ['ngResource'])
    .factory('AccountUpdate', ['$resource', AccountUpdateFactory]);
})();
