package com.megared.webgate.routes;

import rawhttp.core.RawHttpRequest;
import rawhttp.core.RawHttpResponse;

import java.util.Optional;
import java.util.regex.Pattern;

public interface Router {
    public boolean isPatternMatches(String path);
    public Optional<RawHttpResponse<?>> route(RawHttpRequest request);
}
