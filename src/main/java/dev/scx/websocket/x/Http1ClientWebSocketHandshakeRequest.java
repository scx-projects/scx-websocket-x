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
import dev.scx.websocket.ScxClientWebSocketHandshakeRequest;
import dev.scx.websocket.ScxClientWebSocketHandshakeResponse;

import java.util.Base64;

import static dev.scx.http.headers.HttpHeaderName.SEC_WEBSOCKET_KEY;
import static dev.scx.http.headers.HttpHeaderName.SEC_WEBSOCKET_VERSION;
import static dev.scx.http.x.http1.headers.connection.Connection.UPGRADE;
import static dev.scx.http.x.http1.headers.upgrade.Upgrade.WEB_SOCKET;
import static dev.scx.random.ScxRandom.randomBytes;

/// ClientWebSocketHandshakeRequest
///
/// @author scx567888
/// @version 0.0.1
public class Http1ClientWebSocketHandshakeRequest implements ScxClientWebSocketHandshakeRequest {

    private final WebSocketOptions webSocketOptions;
    private final HttpClientRequest _request;

    public Http1ClientWebSocketHandshakeRequest(HttpClientRequest request, WebSocketOptions webSocketOptions) {
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
    public ScxClientWebSocketHandshakeRequest uri(ScxURI uri) {
        _request.uri(uri);
        return this;
    }

    @Override
    public ScxClientWebSocketHandshakeResponse sendHandshake() throws IllegalSenderStateException, ScxHttpSendException, ScxWrappedException, ScxHttpReceiveException {
        //1, 创建 secWebsocketKey
        var secWebsocketKey = Base64.getEncoder().encodeToString(randomBytes(16));
        this._request.headers().connection(UPGRADE);
        this._request.headers().upgrade(WEB_SOCKET);
        this._request.headers().set(SEC_WEBSOCKET_KEY, secWebsocketKey);
        this._request.headers().set(SEC_WEBSOCKET_VERSION, "13");
        var clientResponse = (Http1ClientResponse) _request.send();
        return new Http1ClientWebSocketHandshakeResponse(clientResponse, webSocketOptions);
    }

}
