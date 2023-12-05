package com.megared.webgate.routers;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.janboerman.invsee.spigot.api.EnderSpectatorInventory;
import com.janboerman.invsee.spigot.api.response.SpectateResponse;
import org.bukkit.OfflinePlayer;
import org.bukkit.Server;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.json.simple.JSONObject;
import rawhttp.core.RawHttp;
import rawhttp.core.RawHttpRequest;
import rawhttp.core.RawHttpResponse;
import rawhttp.core.body.StringBody;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.bukkit.Bukkit.getServer;

public class OnlineRouter implements Router {
    private final Pattern pathPattern = Pattern.compile("/?/online/?");
    private final RawHttp http = new RawHttp();

    @Override
    public boolean isPatternMatches(String path) {
        return getMatcherForPath(path).matches();
    }

    public Matcher getMatcherForPath(String path) {
        return pathPattern.matcher(path);
    }

    private int getOnlinePlayersCount() {
        return getServer().getOnlinePlayers().size();
    }

    @Override
    public Optional<RawHttpResponse<?>> route(RawHttpRequest request) {
        final JSONObject json = new JSONObject();
        json.put("online", getOnlinePlayersCount());

        return Optional.of(http.parseResponse("HTTP/1.1 200 OK\r\n" +
                        "Content-Type: application/json"
                ).withBody(
                        new StringBody(json.toJSONString()))
        );
    }
}
