angular.module('services.apiErrorHandler', []);
angular.module('services.apiErrorHandler').factory('apiErrorHandler', [function(){

    var apiErrorHandler = {
        alerts: [],

        dismissAlerts: function() {
            this.alerts.length = 0;
        },

        mapToAlert: function(httpResponse) {
            var alert = httpResponse.data;
            alert.status = httpResponse.status;
            alert.statusText = httpResponse.statusText;
            if (httpResponse.status >= 300 && httpResponse.status < 500) {
                alert.type = 'warning';
            } else {
                alert.type = 'error';
            }
            return alert;
        },

        handleApiErrorResponse: function(httpResponse) {
            var alert = this.mapToAlert(httpResponse);
            this.alerts.push(alert);
        }
    };

    return apiErrorHandler;
}]);
