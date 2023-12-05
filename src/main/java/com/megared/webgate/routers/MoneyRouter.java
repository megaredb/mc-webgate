package com.megared.webgate.routers;

import rawhttp.core.RawHttpRequest;
import rawhttp.core.RawHttpResponse;

import java.util.Optional;
import java.util.regex.Pattern;

public class MoneyInEnderchest implements Router {
    private final Pattern pathPattern = Pattern.compile("/money/");

    @Override
    public boolean isPatternMatches(String path) {
        return pathPattern.matcher(path).matches();
    }

    @Override
    public Optional<RawHttpResponse<?>> route(RawHttpRequest request) {
        return Optional.empty();
    }
}
