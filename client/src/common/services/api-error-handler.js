angular.module('services.apiErrorHandler', []);
angular.module('services.apiErrorHandler').factory('apiErrorHandler', [function(){

    var apiErrorHandler = {};
    apiErrorHandler.mapToAlert = function(httpResponse) {
        var alert = httpResponse.data;
        alert.status = httpResponse.status;
        alert.statusText = httpResponse.statusText;
        if (httpResponse.status >= 300 && httpResponse.status < 500) {
            alert.type = 'warning';
        } else {
            alert.type = 'error';
        }
        return alert;
    };

    return apiErrorHandler;
}]);
