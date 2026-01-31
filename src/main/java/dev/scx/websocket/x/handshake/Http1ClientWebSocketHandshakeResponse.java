package dev.scx.websocket.x.handshake;

import dev.scx.http.headers.ScxHttpHeaders;
import dev.scx.http.peer_info.PeerInfo;
import dev.scx.http.status_code.ScxHttpStatusCode;
import dev.scx.http.version.HttpVersion;
import dev.scx.http.x.http1.Http1ClientResponse;
import dev.scx.io.ByteInput;
import dev.scx.websocket.ScxWebSocket;
import dev.scx.websocket.handshake.ScxClientWebSocketHandshakeRejectedException;
import dev.scx.websocket.handshake.ScxClientWebSocketHandshakeResponse;
import dev.scx.websocket.x.WebSocket;
import dev.scx.websocket.x.WebSocketOptions;

import static dev.scx.http.status_code.HttpStatusCode.SWITCHING_PROTOCOLS;
import static dev.scx.http.version.HttpVersion.HTTP_1_1;

/// ClientWebSocketHandshakeResponse
///
/// @author scx567888
/// @version 0.0.1
public class Http1ClientWebSocketHandshakeResponse implements ScxClientWebSocketHandshakeResponse {

    private final Http1ClientResponse _response;
    private final WebSocketOptions webSocketOptions;
    private ScxWebSocket webSocket;

    public Http1ClientWebSocketHandshakeResponse(Http1ClientResponse response, WebSocketOptions webSocketOptions) {
        this._response = response;
        this.webSocketOptions = webSocketOptions;
    }

    // todo 校验不完整 正常应该 校验 头中的 secWebsocketKey
    @Override
    public boolean handshakeAccepted() {
        return SWITCHING_PROTOCOLS == _response.statusCode();
    }

    @Override
    public ScxWebSocket upgrade() throws ScxClientWebSocketHandshakeRejectedException {
        if (webSocket == null) {
            if (!handshakeAccepted()) {
                throw new ScxClientWebSocketHandshakeRejectedException("Unexpected response status: " + _response.statusCode());
            }
            webSocket = new WebSocket(_response.connection.socketIO, webSocketOptions, true);
        }
        return webSocket;
    }

    @Override
    public ScxHttpStatusCode statusCode() {
        return _response.statusCode();
    }

    @Override
    public HttpVersion version() {
        return HTTP_1_1;
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
