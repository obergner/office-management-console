(function() {
    function linkFunction(scope, element, attributes) {
        scope.apiErrors = []; 

        scope.onOutChannelSelected = function(outChannel, allOutChannels) {
            if (outChannel === 'SIMSme') {
                scope.account.subaccounts.switchAccountRef('SIMSme', 'createNew');
            }
        };

        scope.onOutChannelDeselected = function(outChannel, allOutChannels) {
            if (outChannel === 'SIMSme') {
                scope.account.subaccounts.switchAccountRef('SIMSme', 'none');
            }
        };

        scope.isValidInput = function() {
            return (scope.accountForm.$valid && (!scope.account.subaccounts.requiresAccountRefOfType('SIMSme') ? true : (scope.account.subaccounts.createsAccountRefOfTypeWithAction('SIMSme', 'createNew') ? scope.createNewSimsmeSubaccountForm.$valid : scope.referenceExistingSimsmeSubaccountForm.$valid)));
        };

        scope.onSimsmeAccountRefCreationActionChanged = function(action) {
            switch(action) {
                case 'createNew':
                    scope.accountForm.$removeControl(scope.referenceExistingSimsmeSubaccountForm);
                    if (!(scope.createNewSimsmeSubaccountForm.$name in scope.accountForm)) {
                        scope.accountForm.$addControl(scope.createNewSimsmeSubaccountForm);
                    }
                    scope.account.subaccounts.switchAccountRef('SIMSme', 'createNew');
                    break;
                case 'referenceExisting':
                    scope.accountForm.$removeControl(scope.createNewSimsmeSubaccountForm);
                    if (!(scope.referenceExistingSimsmeSubaccountForm.$name in scope.accountForm)) {
                        scope.accountForm.$addControl(scope.referenceExistingSimsmeSubaccountForm);
                    }
                    scope.account.subaccounts.switchAccountRef('SIMSme', 'referenceExisting');
                    break;
                default:
                    throw new Error('Unknown action: ' + action);
            }
        };

        scope.ok = function() {
            scope.apiErrors.length = 0;
            scope.onSave({
                account: scope.account,
                apiErrors: scope.apiErrors
            });
        };

        scope.cancel = function() {
            scope.apiErrors.length = 0;
            scope.onCancel({
                account: scope.account,
                apiErrors: scope.apiErrors
            });
        };
    }

    function accountModificationForm() {
        return {
            restrict: 'AE',
            scope: {
                account: '=ngModel',
                title: '@',
                onSave: '&',
                onCancel: '&'
            },
            templateUrl: 'accounts/account-modification-form.tpl.html',
            link: linkFunction
        };
    }

    angular
    .module('accounts.accountModificationForm', [])
    .directive('accountModificationForm', ['apiErrorHandler', accountModificationForm]);
})();
