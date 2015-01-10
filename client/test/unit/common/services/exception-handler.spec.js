describe('exception handler', function () {

    var $exceptionHandler;
    var growlDirective;

    beforeEach(function () {
        angular.module('test', ['services.exceptionHandler'], function($exceptionHandlerProvider){
            $exceptionHandlerProvider.mode('log');
        }).constant('I18N.MESSAGES', {'error.fatal':'Oh, snap!'});
        module('test');
    });

    beforeEach(inject(function (_$exceptionHandler_) {
        $exceptionHandler = _$exceptionHandler_;
    }));

    beforeEach(inject(function($compile, $rootScope) {
        var scope = $rootScope.$new();
        growlDirective = $compile('<div growl></div>')(scope);
        scope.$digest();
    }));

    it('should call through to the delegate', function() {
        inject(function(exceptionHandlerFactory) {
            var error = new Error('Something went wrong...');
            var cause = 'Some obscure problem...';

            var delegate = jasmine.createSpy('delegate');
            var exceptionHandler = exceptionHandlerFactory(delegate);
            exceptionHandler(error, cause);
            expect(delegate).toHaveBeenCalledWith(error, cause);
        });
    });
});
