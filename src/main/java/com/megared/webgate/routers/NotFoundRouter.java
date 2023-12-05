package com.megared.webgate.routers;

import rawhttp.core.RawHttp;
import rawhttp.core.RawHttpRequest;
import rawhttp.core.RawHttpResponse;

import java.util.Optional;

public class NotFoundRouter implements Router {
    private final RawHttp http = new RawHttp();

    @Override
    public boolean isPatternMatches(String path) {
        return false;
    }

    @Override
    public Optional<RawHttpResponse<?>> route(RawHttpRequest request) {
        return Optional.of(http.parseResponse("HTTP/1.1 404 Not Found\r\n" +
                "Content-Type: text/plain"
        ));
    }
}
