package dev.scx.websocket.x;

import dev.scx.http.version.HttpVersion;
import dev.scx.http.x.HttpClient;

/// WebSocketClient
///
/// @author scx567888
public final class WebSocketClient implements ScxWebSocketClient {

    private final HttpClient httpClient;
    private final WebSocketOptions options;

    public WebSocketClient(HttpClient httpClient, WebSocketOptions options) {
        this.httpClient = httpClient;
        this.options = options;
    }

    public WebSocketClient(WebSocketOptions options) {
        this(new HttpClient(), options);
    }

    public WebSocketClient() {
        this(new HttpClient(), new WebSocketOptions());
    }

    @Override
    public ScxWebSocketClientHandshakeRequest handshakeRequest() {
        var request = httpClient.request(HttpVersion.HTTP_1_1);
        return new Http1WebSocketClientHandshakeRequest(request, options);
    }

    public HttpClient httpClient() {
        return httpClient;
    }

    public WebSocketOptions options() {
        return options;
    }

}
