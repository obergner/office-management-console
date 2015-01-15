(function() {
    function ExceptionHandlerFactory($injector) {
        return function($delegate) {
            return function (exception, cause) {
                // Pass through to original handler
                $delegate(exception, cause);
                // Avoid circular dependency: $exceptionHandler <- $interpolate <- localizedMessages <- exceptionHandlerFactory <- $exceptionHandler
                var localizedMessages = $injector.get('localizedMessages');
                // Avoid circular dependency: $exceptionHandler <- $rootScope <- growl <- exceptionHandlerFactory <- $exceptionHandler
                var growl = $injector.get('growl');
                // Push a notification error
                growl.error(localizedMessages.get('error.fatal', { exception: exception, cause: cause }), {title: 'Exception'});
            };
        };
    }

    angular.module('services.exceptionHandler', ['services.localizedMessages', 'angular-growl'])

    .factory('exceptionHandlerFactory', ['$injector', ExceptionHandlerFactory])

    .config(['$provide', function($provide) {
        $provide.decorator('$exceptionHandler', ['$delegate', 'exceptionHandlerFactory', function($delegate, exceptionHandlerFactory) {
            return exceptionHandlerFactory($delegate);
        }]);
    }]);
})();
