package com.megared.webgate.routers;

import org.bukkit.OfflinePlayer;
import org.bukkit.Server;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitScheduler;
import rawhttp.core.RawHttp;
import rawhttp.core.RawHttpRequest;
import rawhttp.core.RawHttpResponse;

import java.util.ArrayList;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.bukkit.Bukkit.getServer;

public class WhitelistRouter implements Router {
    private final Pattern pathPattern = Pattern.compile("/?/whitelist/(\\w+)/?.*");
    private final RawHttp http = new RawHttp();
    private final ArrayList<String> methods = new ArrayList<>();

    public WhitelistRouter() {
        this.methods.add("POST");
        this.methods.add("DELETE");
    }

    @Override
    public boolean isPatternMatches(String path) {
        return getMatcherForPath(path).matches();
    }

    public Matcher getMatcherForPath(String path) {
        return pathPattern.matcher(path);
    }

    private String getUsernameFromPath(String path) {
        Matcher matcher = getMatcherForPath(path);
        if (!matcher.find()) {
            return null;
        }
        return matcher.group(1);
    }

    @Override
    public Optional<RawHttpResponse<?>> route(RawHttpRequest request) {
        String method = request.getMethod();

        if (!methods.contains(method)) {
            return Optional.of(http.parseResponse("HTTP/1.1 405 Method Not Allowed\r\n" +
                    "Content-Type: text/plain"
            ));
        }

        String username = getUsernameFromPath(request.getUri().getPath().strip());

        if (username == null) {
            return Optional.of(http.parseResponse("HTTP/1.1 400 Bad Request\r\n" +
                    "Content-Type: text/plain"
            ));
        }

        Server server = getServer();
        Plugin plugin = server.getPluginManager().getPlugin("WebGate");

        assert plugin != null;

        BukkitScheduler scheduler = server.getScheduler();
        OfflinePlayer offlinePlayer = server.getOfflinePlayer(username);
        Runnable runnable = () -> offlinePlayer.setWhitelisted(false);

        if (method.equals("POST")) {
            runnable = () -> offlinePlayer.setWhitelisted(true);
        }

        scheduler.runTask(plugin, runnable);

        return Optional.of(http.parseResponse("HTTP/1.1 200 OK\r\n" +
                "Content-Type: text/plain"
        ));
    }
}
