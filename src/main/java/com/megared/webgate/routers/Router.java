package com.megared.webgate.routers;

import rawhttp.core.RawHttpRequest;
import rawhttp.core.RawHttpResponse;

import java.util.Optional;

public interface Router {
    public boolean isPatternMatches(String path);
    public Optional<RawHttpResponse<?>> route(RawHttpRequest request);
}
