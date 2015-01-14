(function() {
    function HttpRequestTracker($http) {
        var httpRequestTracker = {};
        httpRequestTracker.hasPendingRequests = function() {
            return $http.pendingRequests.length > 0;
        };

        return httpRequestTracker;
    }

    angular.module('services.httpRequestTracker', [])

    .factory('httpRequestTracker', ['$http', HttpRequestTracker]);
})();
