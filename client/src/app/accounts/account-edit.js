(function() {
    function EditAccountController($scope, $modalInstance, $state, localizedMessages, apiErrorHandler, growl, AccountSettings, accountToUpdate) {
        $scope.account = accountToUpdate;

        $scope.onSave = function(account, apiErrors) {
            account.update(
                function(updatedAccount) {
                    $modalInstance.close(updatedAccount);
                    $state.go('accounts', {}, { reload: true }).then(function() {
                        growl.success(localizedMessages.get('crud.account.update.success', {account: updatedAccount}), {title: 'Account updated'});
                    });
                },
                function(httpResponse) {
                    var alerts = apiErrorHandler.mapToAlert(httpResponse);
                    apiErrors.push(alerts);
                });
        };

        $scope.onCancel = function(account, apiErrors) {
            $modalInstance.dismiss('cancel');
            $state.go('accounts');
        };
    }

    angular
    .module('accounts.edit', [
        'services.localizedMessages',
        'services.apiErrorHandler'
    ])
    .controller('EditAccountController', ['$scope', '$modalInstance', '$state', 'localizedMessages', 'apiErrorHandler', 'growl', 'AccountSettings', 'accountToUpdate', 
        EditAccountController]);
})();
