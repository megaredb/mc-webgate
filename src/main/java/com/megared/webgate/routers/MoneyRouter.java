package com.megared.webgate.routers;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.janboerman.invsee.spigot.InvseePlusPlus;
import com.janboerman.invsee.spigot.api.EnderSpectatorInventory;
import com.janboerman.invsee.spigot.api.InvseeAPI;
import com.janboerman.invsee.spigot.api.response.SpectateResponse;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.Server;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.json.simple.JSONObject;
import rawhttp.core.RawHttp;
import rawhttp.core.RawHttpRequest;
import rawhttp.core.RawHttpResponse;
import rawhttp.core.body.StringBody;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.bukkit.Bukkit.getServer;

public class MoneyRouter implements Router {
    private final Pattern pathPattern = Pattern.compile("/?/money/(\\w+)/?.*");
    private final InvseePlusPlus invseePlusPlus = (InvseePlusPlus) getServer().getPluginManager().getPlugin("InvSeePlusPlus");
    private final InvseeAPI invseeApi = invseePlusPlus.getApi();
    private final RawHttp http = new RawHttp();
    private final int moneyCustomModelData;
    private final Material moneyMaterial;

    public MoneyRouter(int moneyCustomModelData, String moneyMaterialName) {
        this.moneyCustomModelData = moneyCustomModelData;
        this.moneyMaterial = Material.getMaterial(moneyMaterialName);
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

    private ArrayList<ItemStack> getMoneyStacks(Inventory inventory) {
        ArrayList<ItemStack> result = new ArrayList<>();

        for (ItemStack itemStack : inventory.all(moneyMaterial).values()) {
            if (!itemStack.hasItemMeta()) {
                continue;
            }

            ItemMeta meta = itemStack.getItemMeta();
            if (meta.hasCustomModelData() && meta.getCustomModelData() == moneyCustomModelData) {
                result.add(itemStack);
            }
        }

        return result;
    }

    @Override
    public Optional<RawHttpResponse<?>> route(RawHttpRequest request) {
        String path = request.getUri().getPath().strip();
        String username = getUsernameFromPath(path);

        if (username == null) {
            return Optional.empty();
        }

        Server server = getServer();
        OfflinePlayer offlinePlayer = server.getOfflinePlayerIfCached(username);

        if (offlinePlayer == null) {
            return Optional.empty();
        }

        PlayerProfile playerProfile = offlinePlayer.getPlayerProfile();
        CompletableFuture<SpectateResponse<EnderSpectatorInventory>> enderChestFuture
                = invseeApi.enderSpectatorInventory(playerProfile.getId(), playerProfile.getName());
        SpectateResponse<EnderSpectatorInventory> spectateResponse;
        Inventory enderChestInventory;

        try {
            spectateResponse = enderChestFuture.get();
            enderChestInventory = spectateResponse.getInventory();
        } catch (InterruptedException | ExecutionException | NoSuchElementException e) {
            return Optional.empty();
        }

        Iterator<ItemStack> moneyStacksIterator = getMoneyStacks(enderChestInventory).iterator();

        int money = 0;

        while (moneyStacksIterator.hasNext()) {
            ItemStack moneyStack = moneyStacksIterator.next();
            money += moneyStack.getAmount();
        }

        final JSONObject json = new JSONObject();
        json.put("money", money);

        return Optional.of(http.parseResponse("HTTP/1.1 200 OK\r\n" +
                "Content-Type: application/json"
        ).withBody(
                new StringBody(json.toJSONString()))
        );
    }
}
