(function() {
    function EditAccountController($scope, $modalInstance, $state, localizedMessages, apiErrorHandler, growl, AccountSettings, accountToUpdate) {

        $scope.account = accountToUpdate;
        $scope.apiErrors = apiErrorHandler;
        $scope.availableOutChannels = AccountSettings.outChannels;

        $scope.ok = function () {
            $scope.apiErrors.dismissAlerts();
            $scope.account.$update(
                function(updatedAccount) {
                    $modalInstance.close(updatedAccount);
                    $state.go('accounts', {}, { reload: true }).then(function() {
                        growl.success(localizedMessages.get('crud.account.update.success', {account: updatedAccount}), {title: 'Account updated'});
                    });
                },
                function(httpResponse) {
                    $scope.apiErrors.handleApiErrorResponse(httpResponse);
                });
        };

        $scope.cancel = function () {
            $scope.apiErrors.dismissAlerts();
            $modalInstance.dismiss('cancel');
            $state.go('accounts');
        };
    }

    angular.module('accounts.edit', [
        'services.localizedMessages',
        'services.apiErrorHandler'
    ])

    .controller('EditAccountController', ['$scope', '$modalInstance', '$state', 'localizedMessages', 'apiErrorHandler', 'growl', 'AccountSettings', 'accountToUpdate', 
        EditAccountController]);
})();
