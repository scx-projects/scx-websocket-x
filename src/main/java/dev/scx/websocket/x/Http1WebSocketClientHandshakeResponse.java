package dev.scx.websocket.x;

import dev.scx.http.headers.ScxHttpHeaders;
import dev.scx.http.peer_info.PeerInfo;
import dev.scx.http.status_code.ScxHttpStatusCode;
import dev.scx.http.version.HttpVersion;
import dev.scx.http.x.http1.Http1ClientResponse;
import dev.scx.http.x.http1.headers.Http1Headers;
import dev.scx.io.ByteInput;
import dev.scx.websocket.ScxWebSocket;
import dev.scx.websocket.frame.ScxFrameWebSocket;
import dev.scx.websocket.x.exception.ScxWebSocketClientHandshakeRejectedException;

import static dev.scx.http.headers.HttpHeaderName.SEC_WEBSOCKET_ACCEPT;
import static dev.scx.http.status_code.HttpStatusCode.SWITCHING_PROTOCOLS;
import static dev.scx.http.x.http1.headers.connection.Connection.UPGRADE;
import static dev.scx.http.x.http1.headers.upgrade.Upgrade.WEB_SOCKET;

/// Http1WebSocketClientHandshakeResponse
///
/// @author scx567888
public final class Http1WebSocketClientHandshakeResponse implements ScxWebSocketClientHandshakeResponse {

    private final Http1ClientResponse _response;
    private final String secWebSocketKey;
    private final WebSocketOptions webSocketOptions;
    private ScxWebSocket webSocket;

    public Http1WebSocketClientHandshakeResponse(Http1ClientResponse response, String secWebSocketKey, WebSocketOptions webSocketOptions) {
        this._response = response;
        this.secWebSocketKey = secWebSocketKey;
        this.webSocketOptions = webSocketOptions;
    }

    private void validateHandshake() throws ScxWebSocketClientHandshakeRejectedException {
        var statusCode = _response.statusCode();
        // 1, 101
        if (statusCode != SWITCHING_PROTOCOLS) {
            throw new ScxWebSocketClientHandshakeRejectedException("Invalid status code: " + statusCode);
        }

        var headers = (Http1Headers) _response.headers();

        // 2, Connection: Upgrade
        var connection = headers.connection();
        if (connection == null) {
            throw new ScxWebSocketClientHandshakeRejectedException("Missing required header: Connection");
        }
        if (connection != UPGRADE) {
            throw new ScxWebSocketClientHandshakeRejectedException("Invalid Connection: " + connection);
        }

        // 3, Upgrade: websocket
        var upgrade = headers.upgrade();
        if (upgrade == null) {
            throw new ScxWebSocketClientHandshakeRejectedException("Missing required header: Upgrade");
        }
        if (upgrade != WEB_SOCKET) {
            throw new ScxWebSocketClientHandshakeRejectedException("Invalid Upgrade: " + upgrade);
        }

        // 4, Sec-WebSocket-Accept
        var expectedSecWebSocketAccept = WebSocketHandshakeHelper.computeSecWebSocketAccept(secWebSocketKey);

        var secWebSocketAccept = headers.get(SEC_WEBSOCKET_ACCEPT);
        if (secWebSocketAccept == null) {
            throw new ScxWebSocketClientHandshakeRejectedException("Missing required header: Sec-WebSocket-Accept");
        }
        if (!expectedSecWebSocketAccept.equals(secWebSocketAccept)) {
            throw new ScxWebSocketClientHandshakeRejectedException("Invalid Sec-WebSocket-Accept");
        }
    }

    @Override
    public boolean handshakeAccepted() {
        try {
            validateHandshake();
            return true;
        } catch (ScxWebSocketClientHandshakeRejectedException e) {
            return false;
        }
    }

    @Override
    public ScxWebSocket upgrade() throws ScxWebSocketClientHandshakeRejectedException {
        if (webSocket == null) {
            // 先校验握手 是否正确.
            validateHandshake();
            // 创建 webSocket
            webSocket = ScxWebSocket.of(
                ScxFrameWebSocket.of(_response.connection.endpoint, true, webSocketOptions.maxFrameSize()),
                webSocketOptions.maxMessageSize()
            );
        }
        return webSocket;
    }

    @Override
    public ScxHttpStatusCode statusCode() {
        return _response.statusCode();
    }

    @Override
    public HttpVersion version() {
        return _response.version();
    }

    @Override
    public ScxHttpHeaders headers() {
        return _response.headers();
    }

    @Override
    public PeerInfo remotePeer() {
        return _response.remotePeer();
    }

    @Override
    public PeerInfo localPeer() {
        return _response.localPeer();
    }

    @Override
    public ByteInput body() {
        return _response.body();
    }

}
