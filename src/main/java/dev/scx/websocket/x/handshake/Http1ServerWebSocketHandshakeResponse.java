package dev.scx.websocket.x.handshake;

import dev.scx.exception.ScxWrappedException;
import dev.scx.http.sender.IllegalSenderStateException;
import dev.scx.http.sender.ScxHttpReceiveException;
import dev.scx.http.sender.ScxHttpSendException;
import dev.scx.http.status_code.ScxHttpStatusCode;
import dev.scx.http.x.http1.Http1ServerResponse;
import dev.scx.http.x.http1.headers.Http1Headers;
import dev.scx.websocket.ScxWebSocket;
import dev.scx.websocket.handshake.ScxServerWebSocketHandshakeResponse;
import dev.scx.websocket.x.WebSocket;
import dev.scx.websocket.x.WebSocketOptions;

import static dev.scx.http.headers.HttpHeaderName.SEC_WEBSOCKET_ACCEPT;
import static dev.scx.http.status_code.HttpStatusCode.SWITCHING_PROTOCOLS;
import static dev.scx.http.x.http1.headers.connection.Connection.UPGRADE;
import static dev.scx.http.x.http1.headers.upgrade.Upgrade.WEB_SOCKET;
import static dev.scx.websocket.x.handshake.WebSocketHandshakeHelper.generateSecWebSocketAccept;

/// 基于 http1 的 websocket 握手响应
///
/// @author scx567888
/// @version 0.0.1
public class Http1ServerWebSocketHandshakeResponse  implements ScxServerWebSocketHandshakeResponse {

    private final WebSocketOptions webSocketOptions;
    private final Http1ServerWebSocketHandshakeRequest request;
    private final Http1ServerResponse _response;
    private WebSocket webSocket;

    public Http1ServerWebSocketHandshakeResponse(Http1ServerWebSocketHandshakeRequest request, WebSocketOptions webSocketOptions) {
        this.request = request;
        this._response = request._request().response();
        this.webSocketOptions = webSocketOptions;
    }

    @Override
    public ScxWebSocket upgrade() throws IllegalSenderStateException, ScxHttpSendException, ScxWrappedException, ScxHttpReceiveException {
        if (webSocket == null) {
            // 实现握手接受逻辑, 返回适当的响应头
            _response.headers().upgrade(WEB_SOCKET);
            _response.headers().connection(UPGRADE);
            _response.headers().set(SEC_WEBSOCKET_ACCEPT, generateSecWebSocketAccept(request().secWebSocketKey()));

            // 一旦 websocket 升级 响应发送成功, 整个 tcp 将会被 websocket 独占.
            statusCode(SWITCHING_PROTOCOLS).send();

            webSocket = new WebSocket(_response.connection.socketIO, webSocketOptions, false);
        }
        return webSocket;
    }

    @Override
    public Http1ServerWebSocketHandshakeRequest request() {
        return request;
    }

    @Override
    public ScxHttpStatusCode statusCode() {
        return _response.statusCode();
    }

    @Override
    public Http1ServerWebSocketHandshakeResponse statusCode(ScxHttpStatusCode statusCode) {
        this._response.statusCode(statusCode);
        return this;
    }

    @Override
    public Http1Headers headers() {
        return _response.headers();
    }

    public String reasonPhrase() {
        return _response.reasonPhrase();
    }

    public Http1ServerWebSocketHandshakeResponse reasonPhrase(String reasonPhrase) {
        this._response.reasonPhrase(reasonPhrase);
        return this;
    }

    @Override
    public Void send(BodyWriter bodyWriter) throws IllegalSenderStateException, ScxHttpSendException, ScxWrappedException, ScxHttpReceiveException {
        return this._response.send(bodyWriter);
    }

}
