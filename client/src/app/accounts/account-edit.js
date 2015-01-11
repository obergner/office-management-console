angular.module('accounts.edit', [
    'services.localizedMessages',
    'services.apiErrorHandler'
])

.controller('EditAccountController', ['$scope', '$modalInstance', '$state', 'localizedMessages', 'apiErrorHandler', 'growl', 'AccountSettings', 'accountToUpdate', 
    function ($scope, $modalInstance, $state, localizedMessages, apiErrorHandler, growl, AccountSettings, accountToUpdate) {

        $scope.accountToUpdate = accountToUpdate;
        $scope.alerts = [];
        $scope.availableOutChannels = AccountSettings.outChannels;

        $scope.dismissAlert = function() {
            $scope.alerts.length = 0;
        };

        $scope.ok = function () {
            $scope.dismissAlert();
            $scope.accountToUpdate.$update(
                function(updatedAccount) {
                    $modalInstance.close(updatedAccount);
                    $state.go('accounts', {}, { reload: true }).then(function() {
                        growl.success(localizedMessages.get('crud.account.update.success', {account: updatedAccount}), {title: 'Account updated'});
                    });
                },
                function(httpResponse) {
                    var alert = apiErrorHandler.mapToAlert(httpResponse);
                    $scope.alerts.push(alert);
                });
        };

        $scope.cancel = function () {
            $modalInstance.dismiss('cancel');
            $state.go('accounts');
        };
    }
]);
