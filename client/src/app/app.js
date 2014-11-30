angular.module('app', [
    'ngSanitize',
    'ngRoute',
    'ngResource',
    'ngAnimate',
    'ui.bootstrap',
    'ui.router',
    'ui.select',
    'growlNotifications',
    'ncy-angular-breadcrumb',
    'accounts',
    'resources.account',
    'services.i18nNotifications',
    'services.httpRequestTracker',
    'templates.app',
'templates.common']);

//TODO: move those messages to a separate module
angular.module('app').constant('I18N.MESSAGES', {
    'errors.route.changeError':'Route change error',
    'crud.account.create.success':"Account '{{account.uuid}} | {{account.name}}' successfully created.",
    'crud.account.create.error':"Failed to create account '{{account.name}}: ",
    'crud.account.update.success':"Account '{{account.uuid}} | {{account.name}}' successfully updated.",
    'crud.account.update.error':"Failed to update account '{{account.name}}: ",
    'crud.account.delete.success':"Account '{{account.uuid}} | {{account.name}}' successfully deleted.",
    'crud.account.delete.error':"Failed to delete account '{{account.uuid}} | {{account.name}}': ",
    'crud.configuration.save.success':"A configuration with id '{{id}}' was saved successfully.",
    'crud.configuration.remove.success':"A configuration with id '{{id}}' was removed successfully.",
    'crud.configuration.save.error':"Something went wrong when saving a configuration..."
});

angular.module('app').constant('BACKEND', {
    'host': 'http://localhost:8080'
});

angular.module('app').config(['$urlRouterProvider', '$locationProvider', 'uiSelectConfig', function ($urlRouterProvider, $locationProvider, uiSelectConfig) {
    $locationProvider.html5Mode(true);
    $urlRouterProvider.otherwise('/accounts');
    uiSelectConfig.theme = 'bootstrap';
}]);

angular.module('app').controller('AppCtrl', ['$scope', 'i18nNotifications', 'localizedMessages', function($scope, i18nNotifications, localizedMessages) {

    $scope.notifications = i18nNotifications;

    $scope.removeNotification = function (notification) {
        i18nNotifications.remove(notification);
    };

    $scope.$on('$routeChangeError', function(event, current, previous, rejection){
        i18nNotifications.pushForCurrentRoute('errors.route.changeError', 'error', {}, {rejection: rejection});
    });
}]);

angular.module('app').controller('HeaderCtrl', ['$scope', '$location', 'notifications', 'httpRequestTracker',
    function ($scope, $location, notifications, httpRequestTracker) {
        $scope.location = $location;

        $scope.home = function () {
            $location.path('/accounts');
        };

        $scope.isNavbarActive = function (navBarPath) {
            return $location.path().indexOf(navBarPath) === 1;
        };

        $scope.hasPendingRequests = function () {
            return httpRequestTracker.hasPendingRequests();
        };
    }
]);
