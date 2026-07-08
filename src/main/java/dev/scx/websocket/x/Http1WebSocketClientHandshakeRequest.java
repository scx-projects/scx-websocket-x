package dev.scx.websocket.x;

import dev.scx.exception.ScxWrappedException;
import dev.scx.http.sender.IllegalSenderStateException;
import dev.scx.http.sender.ScxHttpReceiveException;
import dev.scx.http.sender.ScxHttpSendException;
import dev.scx.http.uri.ScxURI;
import dev.scx.http.uri.ScxURIWritable;
import dev.scx.http.x.HttpClientRequest;
import dev.scx.http.x.http1.Http1ClientResponse;
import dev.scx.http.x.http1.headers.Http1Headers;

import static dev.scx.http.headers.HttpHeaderName.SEC_WEBSOCKET_KEY;
import static dev.scx.http.headers.HttpHeaderName.SEC_WEBSOCKET_VERSION;
import static dev.scx.http.method.HttpMethod.GET;
import static dev.scx.http.x.http1.headers.connection.Connection.UPGRADE;
import static dev.scx.http.x.http1.headers.upgrade.Upgrade.WEB_SOCKET;

/// Http1WebSocketClientHandshakeRequest
///
/// @author scx567888
public final class Http1WebSocketClientHandshakeRequest implements ScxWebSocketClientHandshakeRequest {

    private final HttpClientRequest _request;
    private final WebSocketOptions webSocketOptions;

    public Http1WebSocketClientHandshakeRequest(HttpClientRequest request, WebSocketOptions webSocketOptions) {
        this._request = request;
        this.webSocketOptions = webSocketOptions;
    }

    @Override
    public ScxURIWritable uri() {
        return _request.uri();
    }

    @Override
    public Http1Headers headers() {
        return _request.headers();
    }

    @Override
    public ScxWebSocketClientHandshakeRequest uri(ScxURI uri) {
        _request.uri(uri);
        return this;
    }

    @Override
    public ScxWebSocketClientHandshakeResponse handshake() throws IllegalSenderStateException, ScxHttpSendException, ScxWrappedException, ScxHttpReceiveException {
        // 1, 创建 secWebSocketKey
        var secWebSocketKey = WebSocketHandshakeHelper.generateSecWebSocketKey();
        this._request.method(GET);
        this._request.headers().connection(UPGRADE);
        this._request.headers().upgrade(WEB_SOCKET);
        this._request.headers().set(SEC_WEBSOCKET_KEY, secWebSocketKey);
        this._request.headers().set(SEC_WEBSOCKET_VERSION, "13");
        var clientResponse = (Http1ClientResponse) _request.send();
        return new Http1WebSocketClientHandshakeResponse(clientResponse, secWebSocketKey, webSocketOptions);
    }

}
