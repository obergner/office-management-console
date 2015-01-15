(function() {
    function DeleteAccountController($scope, $modalInstance, $state, localizedMessages, apiErrorHandler, growl, AccountSettings, accountToDelete) {

        $scope.account = accountToDelete;
        $scope.apiErrors = apiErrorHandler; 
        $scope.availableOutChannels = AccountSettings.outChannels;

        $scope.ok = function () {
            $scope.apiErrors.dismissAlerts();
            $scope.account.$delete(
                function() {
                    $modalInstance.close($scope.account);
                    $state.go('accounts', {}, { reload: true }).then(function() {
                        growl.success(localizedMessages.get('crud.account.delete.success', {account: $scope.account}), {title: 'Account deleted'});
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

    angular
    .module('accounts.delete', [
        'services.localizedMessages',
        'services.apiErrorHandler'
    ])
    .controller('DeleteAccountController', ['$scope', '$modalInstance', '$state', 'localizedMessages', 'apiErrorHandler', 'growl', 'AccountSettings', 'accountToDelete',
        DeleteAccountController]);
})();
