(function() {
    function NewAccountController($scope, $modalInstance, $state, localizedMessages, apiErrorHandler, growl, newAccount) {
        $scope.account = newAccount;

        $scope.onSave = function(account, apiErrors) {
            account.save(
                function(createdAccount) {
                    $modalInstance.close(createdAccount);
                    $state.go('accounts', {}, { reload: true }).then(function() {
                        growl.success(localizedMessages.get('crud.account.create.success', {account: createdAccount}), {title: 'Account created'});
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
    .module('accounts.new', [
        'accounts.accountModificationForm',
        'services.localizedMessages',
        'services.apiErrorHandler'
    ])
    .controller('NewAccountController', ['$scope', '$modalInstance', '$state', 'localizedMessages', 'apiErrorHandler', 'growl', 'newAccount', NewAccountController]);
})();
