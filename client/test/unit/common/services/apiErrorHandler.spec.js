describe('apiErrorHandler', function () {

    var apiErrorHandler;

    beforeEach(module('services.apiErrorHandler'));

    beforeEach(inject(function($injector) {
        apiErrorHandler = $injector.get('apiErrorHandler');
    }));

    it('should correctly convert httpResponse containing with status 400 ApiError to alert of type warning', function () {
        var apiError = {
            status: 400,
            code: 'api.error.common.malformed-request',
            message: 'This is a test ApiError'
        };
        var statusText = 'Bad Request';
        var expectedAlertType = 'warning';

        var httpResponse = {
            status: apiError.status,
            statusText: statusText,
            data: apiError,
            headers: {}
        };

        var alert = apiErrorHandler.mapToAlert(httpResponse);

        expect(alert.status).toEqual(httpResponse.status);
        expect(alert.statusText).toEqual(httpResponse.statusText);
        expect(alert.code).toEqual(apiError.code);
        expect(alert.message).toEqual(apiError.message);
        expect(alert.type).toEqual(expectedAlertType);
    });

    it('should correctly convert httpResponse containing with status 501 ApiError to alert of type error', function () {
        var apiError = {
            status: 501,
            code: 'api.error.common.internal-server-error',
            message: 'This is a test ApiError'
        };
        var statusText = 'Internal Server Error';
        var expectedAlertType = 'error';

        var httpResponse = {
            status: apiError.status,
            statusText: statusText,
            data: apiError,
            headers: {}
        };

        var alert = apiErrorHandler.mapToAlert(httpResponse);

        expect(alert.status).toEqual(httpResponse.status);
        expect(alert.statusText).toEqual(httpResponse.statusText);
        expect(alert.code).toEqual(apiError.code);
        expect(alert.message).toEqual(apiError.message);
        expect(alert.type).toEqual(expectedAlertType);
    });
});
