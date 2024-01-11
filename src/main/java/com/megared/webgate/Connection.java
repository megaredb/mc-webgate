package com.megared.webgate;
import com.megared.webgate.routers.*;
import org.bukkit.configuration.file.FileConfiguration;
import rawhttp.core.RawHttp;
import rawhttp.core.RawHttpRequest;
import rawhttp.core.RawHttpResponse;

import java.io.*;
import java.net.Socket;
import java.util.Optional;

import static org.bukkit.Bukkit.getLogger;

public class Connection  implements Runnable {
    private final FileConfiguration config;
    private final Socket connectionSocket;
    private RawHttpRequest request;
    private final RawHttp http = new RawHttp();
    private final Router[] routers;
    private final NotFoundRouter notFoundRouter = new NotFoundRouter();

    /**
     * Creates a Connection object
     *
     * @param connectionSocket The socket the client used to connect to the server
     * @param config The configuration file.
     */
    public Connection(Socket connectionSocket, FileConfiguration config) {
        this.config = config;
        this.connectionSocket = connectionSocket;
        this.routers = new Router[]{
                new MoneyRouter(
                        config.getInt("money-model"),
                        config.getString("money-material")
                ),
                new OnlineRouter(),
                new WhitelistRouter()
        };
    }

    private void parseRequest() throws IOException {
        request = http.parseRequest(connectionSocket.getInputStream());
    }

    private Router findRouterForPath(String path) {
        for (Router router : routers) {
            if (router.isPatternMatches(path)) {
                return router;
            }
        }

        return notFoundRouter;
    }

    private void sendResponse() throws IOException {
        DataOutputStream outStream = new DataOutputStream(connectionSocket.getOutputStream());

        String requestAuthKey = request.getHeaders().get("Authorization").get(0);
        String authKey = (String) config.get("key");

        if (!requestAuthKey.equals(authKey)) {
            http.parseResponse("HTTP/1.1 401 Not authorized\r\n" +
                    "Content-Type: text/plain"
            ).writeTo(outStream);

            outStream.close();

            return;
        }

        String path = request.getUri().getPath();

        Router router = findRouterForPath(path);

        Optional<RawHttpResponse<?>> response = router.route(request);
        if (response.isPresent()) {
            response.orElseThrow().writeTo(outStream);
        } else {
            notFoundRouter.route(request).orElseThrow().writeTo(outStream);
        }

        outStream.flush();
        outStream.close();
    }

    /**
     * Ran at the start of the runnable Connection object's execution inside a thread
     */
    @Override
    public void run() {
        try {
            // Parse the client request and store the request field keys/values inside the request HashMap
            parseRequest();

            // Send an appropriate response to the client based on the request received by the server
            sendResponse();

            // Close the client connection
            this.connectionSocket.close();
        } catch (IOException ex) {
            // If an IOException is caught print out the stack of commands that leads to the error
            getLogger().warning(ex.toString());
        }
    }
}
