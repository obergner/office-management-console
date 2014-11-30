package io.obergner.office;

import org.junit.Test;
import org.springframework.http.HttpStatus;
import redis.clients.jedis.exceptions.JedisDataException;

import static org.junit.Assert.assertEquals;

public class ApiErrorCodeTest {

    @Test
    public void shouldMapJdeWithEmptyMessageToFallbackApiErrorCode() {
        final JedisDataException jde = new JedisDataException("");
        final ApiError error = ApiErrorCode.map(jde);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, error.httpStatus());
    }
}
