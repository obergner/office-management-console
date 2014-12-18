package io.obergner.office;

import org.junit.Test;
import org.springframework.http.HttpStatus;
import redis.clients.jedis.exceptions.JedisDataException;

import static org.junit.Assert.assertEquals;

public class ApiErrorCodeTest {

    @Test
    public void should_map_jde_with_empty_message_to_fallback_api_error_code() {
        final JedisDataException jde = new JedisDataException("");
        final ApiError error = ApiErrorCode.map(jde);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, error.httpStatus());
    }
}
