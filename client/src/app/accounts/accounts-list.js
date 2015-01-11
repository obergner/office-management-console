angular.module('accounts.list', [
])

.controller('AccountsListController', ['$scope', '$state', 'accounts', function ($scope, $state, accounts) {
    $scope.accounts = accounts;
    $scope.accountsSearch = '';

    $scope.editAccount = function (account) {
        $state.go('accounts.edit', { uuid: account.uuid });
    };

    $scope.deleteAccount = function (account) {
        $state.go('accounts.delete', { uuid: account.uuid });
    };

    $scope.$on('searchModelUpdated', function(event, searchModel) {
        $scope.accountsSearch = searchModel;
    });
}]);
