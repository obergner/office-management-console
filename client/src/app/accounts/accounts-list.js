angular.module('accounts.list', [])

.controller('AccountsListController', ['$scope', 'accounts', function ($scope, accounts) {
    $scope.accounts = accounts;
    $scope.accountsSearch = '';

    $scope.$on('searchModelUpdated', function(event, searchModel) {
        $scope.accountsSearch = searchModel;
    });
}]);
