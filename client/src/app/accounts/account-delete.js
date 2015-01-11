angular.module('accounts.delete', [
    'services.localizedMessages',
    'services.apiErrorHandler'
])

.controller('DeleteAccountController', ['$scope', '$modalInstance', '$state', 'localizedMessages', 'apiErrorHandler', 'growl', 'AccountSettings', 'accountToDelete', 
    function ($scope, $modalInstance, $state, localizedMessages, apiErrorHandler, growl, AccountSettings, accountToDelete) {

        $scope.accountToDelete = accountToDelete;
        $scope.alerts = [];
        $scope.availableOutChannels = AccountSettings.outChannels;

        $scope.dismissAlert = function() {
            $scope.alerts.length = 0;
        };

        $scope.ok = function () {
            $scope.dismissAlert();
            $scope.accountToDelete.$delete(
                function() {
                    $modalInstance.close($scope.accountToDelete);
                    $state.go('accounts', {}, { reload: true }).then(function() {
                        growl.success(localizedMessages.get('crud.account.delete.success', {account: $scope.accountToDelete}), {title: 'Account deleted'});
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
