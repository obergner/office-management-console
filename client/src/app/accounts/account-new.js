angular.module('accounts.new', [
    'services.localizedMessages',
    'services.apiErrorHandler'
])

.controller('NewAccountController', ['$scope', '$modalInstance', '$state', 'localizedMessages', 'apiErrorHandler', 'growl', 'newAccount', 
    function ($scope, $modalInstance, $state, localizedMessages, apiErrorHandler, growl, newAccount) {
        /*
        * Inherit from our base controller. A little ugly, but well ...
        */
        BaseAccountController.call(this, $scope, newAccount, 'createAccountForm', apiErrorHandler);

        $scope.ok = function () {
            $scope.apiErrors.dismissAlerts();
            $scope.account.save(
                function(createdAccount) {
                    $modalInstance.close(createdAccount);
                    $state.go('accounts', {}, { reload: true }).then(function() {
                        growl.success(localizedMessages.get('crud.account.create.success', {account: createdAccount}), {title: 'Account created'});
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
]);
