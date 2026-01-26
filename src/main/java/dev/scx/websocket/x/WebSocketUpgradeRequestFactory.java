package dev.scx.websocket.x;

import dev.scx.http.x.http1.Http1ServerConnection;
import dev.scx.http.x.http1.Http1UpgradeRequestFactory;
import dev.scx.http.x.http1.headers.Http1Headers;
import dev.scx.http.x.http1.headers.upgrade.ScxUpgrade;
import dev.scx.http.x.http1.request_line.Http1RequestLine;
import dev.scx.io.ByteInput;
import dev.scx.websocket.x.handshake.Http1ServerWebSocketHandshakeRequest;

import static dev.scx.http.x.http1.headers.upgrade.Upgrade.WEB_SOCKET;

/// WebSocketUpgradeHandler
///
/// @author scx567888
/// @version 0.0.1
public final class WebSocketUpgradeRequestFactory implements Http1UpgradeRequestFactory<Http1ServerWebSocketHandshakeRequest> {

    private final WebSocketOptions webSocketOptions;

    public WebSocketUpgradeRequestFactory(WebSocketOptions webSocketOptions) {
        this.webSocketOptions = webSocketOptions;
    }

    public WebSocketUpgradeRequestFactory() {
        this.webSocketOptions = new WebSocketOptions();
    }

    @Override
    public ScxUpgrade upgradeProtocol() {
        return WEB_SOCKET;
    }

    @Override
    public Http1ServerWebSocketHandshakeRequest createUpgradeRequest(Http1RequestLine requestLine, Http1Headers headers, Long bodyLength, ByteInput bodyByteInput, Http1ServerConnection connection) {
        return new Http1ServerWebSocketHandshakeRequest(requestLine, headers, bodyLength, bodyByteInput, connection, webSocketOptions);
    }

}
