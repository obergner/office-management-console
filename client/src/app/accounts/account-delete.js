angular.module('accounts.delete', [
    'services.localizedMessages',
    'services.apiErrorHandler'
])

.controller('DeleteAccountController', ['$scope', '$modalInstance', '$state', 'localizedMessages', 'apiErrorHandler', 'growl', 'AccountSettings', 'accountToDelete', 
    function ($scope, $modalInstance, $state, localizedMessages, apiErrorHandler, growl, AccountSettings, accountToDelete) {

        $scope.account = accountToDelete;
        $scope.alerts = [];
        $scope.availableOutChannels = AccountSettings.outChannels;

        $scope.dismissAlert = function() {
            $scope.alerts.length = 0;
        };

        $scope.ok = function () {
            $scope.dismissAlert();
            $scope.account.$delete(
                function() {
                    $modalInstance.close($scope.account);
                    $state.go('accounts', {}, { reload: true }).then(function() {
                        growl.success(localizedMessages.get('crud.account.delete.success', {account: $scope.account}), {title: 'Account deleted'});
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
