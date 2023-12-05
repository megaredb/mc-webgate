package com.megared.webgate;

import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public final class WebGate extends JavaPlugin {
    private ServerSocket serverSocket = null;

    @Override
    public void onEnable() {
        // Plugin startup logic
        getConfig().options().copyDefaults();
        saveDefaultConfig();

        if (!validateConfig()) {
            getLogger().severe("Invalid configuration, disabling the plugin.");
            this.setEnabled(false);
            return;
        }

        startWebServer(getConfig().getInt("port"));
    }

    private boolean validateConfig() {
        return getConfig().isInt("port") && getConfig().isString("key");
    }


    private void startWebServer(int port) {
        try {
            serverSocket = new ServerSocket(port);
            getLogger().info("Listening for connections on port " + port);

            new BukkitRunnable() {
                @Override
                public void run() {
                    acceptConnection();
                }
            }.runTaskTimerAsynchronously(this, 20, 10);
        } catch (IOException e) {
            getLogger().severe("Error starting the web server: " + e.getMessage());
        }
    }

    private void acceptConnection() {
        try {
            Socket connectionSocket = serverSocket.accept();
            Thread connectionThread = new Thread(new Connection(connectionSocket, getConfig()));
            connectionThread.start();

            if (getConfig().getBoolean("LogNewConnections", true)) {  // default to true if not found
                getLogger().info("New connection accepted");
            }
        } catch (IOException e) {
            getLogger().severe("Error accepting a new connection: " + e.getMessage());
        }
    }

    @Override
    public void onDisable() {
        if (serverSocket != null && !serverSocket.isClosed()) {
            try {
                serverSocket.close();
            } catch (IOException e) {
                getLogger().warning("Error closing server socket: " + e.getMessage());
            }
        }
    }
}
