angular.module('accounts.new', [
    'services.localizedMessages',
    'services.apiErrorHandler'
])

.controller('NewAccountController', ['$scope', '$modalInstance', '$state', 'localizedMessages', 'apiErrorHandler', 'growl', 'newAccount', 
    function ($scope, $modalInstance, $state, localizedMessages, apiErrorHandler, growl, newAccount) {

        $scope.account = newAccount;
        $scope.alerts = [];

        $scope.dismissAlert = function() {
            $scope.alerts.length = 0;
        };

        $scope.onOutChannelSelected = function(outChannel, allOutChannels) {
            if (outChannel === 'SIMSme') {
                $scope.account.subaccounts.simsmeSwitchTo('createNew');
            }
        };

        $scope.onOutChannelDeselected = function(outChannel, allOutChannels) {
            if (outChannel === 'SIMSme') {
                $scope.account.subaccounts.simsmeSwitchTo('none');
            }
        };

        $scope.isValidInput = function() {
            return ($scope.createAccountForm.$valid && (!$scope.account.subaccounts.requiresSimsmeSubaccount() ? true : ($scope.account.subaccounts.createsNewSimsmeAccount() ? $scope.createNewSimsmeSubaccountForm.$valid : $scope.referenceExistingSimsmeSubaccountForm.$valid)));
        };

        $scope.onSimsmeAccountRefCreationActionChanged = function(action) {
            switch(action) {
                case 'createNew':
                    $scope.createAccountForm.$removeControl($scope.referenceExistingSimsmeSubaccountForm);
                    if (!($scope.createNewSimsmeSubaccountForm.$name in $scope.createAccountForm)) {
                        $scope.createAccountForm.$addControl($scope.createNewSimsmeSubaccountForm);
                    }
                    $scope.account.subaccounts.simsmeSwitchTo('createNew');
                    break;
                case 'referenceExisting':
                    $scope.createAccountForm.$removeControl($scope.createNewSimsmeSubaccountForm);
                    if (!($scope.referenceExistingSimsmeSubaccountForm.$name in $scope.createAccountForm)) {
                        $scope.createAccountForm.$addControl($scope.referenceExistingSimsmeSubaccountForm);
                    }
                    $scope.account.subaccounts.simsmeSwitchTo('referenceExisting');
                    break;
                default:
                    throw new Error('Unknown action: ' + action);
            }
        };

        $scope.ok = function () {
            $scope.dismissAlert();
            $scope.account.save(
                function(createdAccount) {
                    $modalInstance.close(createdAccount);
                    $state.go('accounts', {}, { reload: true }).then(function() {
                        growl.success(localizedMessages.get('crud.account.create.success', {account: createdAccount}), {title: 'Account created'});
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
