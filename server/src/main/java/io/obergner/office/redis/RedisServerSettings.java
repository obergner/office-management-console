package io.obergner.office.redis;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "redis.server")
public class RedisServerSettings {

    private String host;

    private int port;

    public String getHost() {
        return host;
    }

    public void setHost(final String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(final int port) {
        this.port = port;
    }

    @Override
    public String toString() {
        return "RedisServerSettings[" +
                "host:'" + host + '\'' +
                "|port:" + port +
                ']';
    }
}
