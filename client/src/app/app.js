angular.module('app', [
    'ngSanitize',
    'ngResource',
    'ngAnimate',
    'ui.bootstrap',
    'ui.router',
    'ui.select',
    'angular-growl',
    'accounts',
    'services.httpRequestTracker',
    'templates.app',
'templates.common']);

//TODO: move those messages to a separate module
angular.module('app').constant('I18N.MESSAGES', {
    'error.fatal':'Caught exception {{exception}} caused by {{cause}}',
    'errors.route.changeError':'Route change error: {{rejection}}',
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

angular.module('app').config(['$urlRouterProvider', 'uiSelectConfig', 'growlProvider', function ($urlRouterProvider, uiSelectConfig, growlProvider) {
    $urlRouterProvider.otherwise('/accounts');
    uiSelectConfig.theme = 'bootstrap';
    growlProvider.globalTimeToLive(5000);
    growlProvider.globalDisableCountDown(true);
    growlProvider.globalDisableIcons(true);
}]);

angular.module('app').controller('AppCtrl', ['$scope', 'growl', 'localizedMessages', function($scope, growl, localizedMessages) {

    $scope.$on('$stateNotFound', function(event, unfoundState, fromState, fromParams) { 
        console.log('Wanted to change from state ' + angular.toJson(fromState) + ' to ' + angular.toJson(unfoundState) + ' but could not find it');
    });

    $scope.$on('$stateChangeSuccess', function(event, toState, toParams, fromState, fromParams) {
        console.log('Successfully changed from ' + angular.toJson(fromState) + ' to ' + angular.toJson(toState));
    });

    $scope.$on('$stateChangeError', function(event, toState, toParams, fromState, fromParams, error){
        growl.error(localizedMessages.get('errors.route.changeError', {rejection: error}));
    });
}]);

angular.module('app').controller('HeaderCtrl', ['$rootScope', '$scope', 'httpRequestTracker',
    function ($rootScope, $scope, httpRequestTracker) {

        $scope.updateSearchModel = function(searchModel) {
            $rootScope.$broadcast('searchModelUpdated', searchModel);
        };

        $scope.hasPendingRequests = function() {
            return httpRequestTracker.hasPendingRequests();
        };
    }
]);
