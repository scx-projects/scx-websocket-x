package dev.scx.websocket.x;

import dev.scx.exception.ScxWrappedException;
import dev.scx.http.sender.IllegalSenderStateException;
import dev.scx.http.sender.ScxHttpReceiveException;
import dev.scx.http.sender.ScxHttpSendException;
import dev.scx.http.status_code.ScxHttpStatusCode;
import dev.scx.http.x.http1.Http1ServerConnection;
import dev.scx.http.x.http1.Http1ServerResponse;
import dev.scx.http.x.http1.headers.Http1Headers;
import dev.scx.websocket.ScxWebSocket;
import dev.scx.websocket.frame.ScxFrameWebSocket;
import dev.scx.websocket.handshake.WebSocketHandshakeHelper;
import dev.scx.websocket.x.exception.ScxServerWebSocketHandshakeInvalidException;

import java.util.Base64;

import static dev.scx.http.headers.HttpHeaderName.SEC_WEBSOCKET_ACCEPT;
import static dev.scx.http.status_code.HttpStatusCode.SWITCHING_PROTOCOLS;
import static dev.scx.http.x.http1.headers.connection.Connection.UPGRADE;
import static dev.scx.http.x.http1.headers.upgrade.Upgrade.WEB_SOCKET;

/// 基于 http1 的 websocket 握手响应
///
/// @author scx567888
/// @version 0.0.1
public final class Http1ServerWebSocketHandshakeResponse implements ScxServerWebSocketHandshakeResponse {

    private final Http1ServerWebSocketHandshakeRequest request;
    private final Http1ServerResponse _response;
    private final WebSocketOptions webSocketOptions;
    private ScxWebSocket webSocket;

    public Http1ServerWebSocketHandshakeResponse(Http1ServerWebSocketHandshakeRequest request, Http1ServerResponse _response, WebSocketOptions webSocketOptions) {
        this.request = request;
        this._response = _response;
        this.webSocketOptions = webSocketOptions;
    }

    /// 这里不需要校验 Connection 和 Upgrade 头,
    /// 因为 [Http1ServerConnection] 只有在这两个请求头符合要求时, 才会调用 [WebSocketUpgradeRequestFactory].
    private void validateHandshake() throws ScxServerWebSocketHandshakeInvalidException {
        // 1, 校验 websocket 版本
        var secWebSocketVersion = request.secWebSocketVersion();
        if (secWebSocketVersion == null) {
            throw new ScxServerWebSocketHandshakeInvalidException("Missing required header: Sec-WebSocket-Version");
        }
        if (!"13".equals(secWebSocketVersion)) {
            throw new ScxServerWebSocketHandshakeInvalidException("Unsupported Sec-WebSocket-Version: " + secWebSocketVersion);
        }

        // 2, 校验 secWebSocketKey
        var secWebSocketKey = request.secWebSocketKey();
        if (secWebSocketKey == null) {
            throw new ScxServerWebSocketHandshakeInvalidException("Missing required header: Sec-WebSocket-Key");
        }
        byte[] decoded;
        try {
            decoded = Base64.getDecoder().decode(secWebSocketKey);
        } catch (Exception e) {
            throw new ScxServerWebSocketHandshakeInvalidException("Invalid Sec-WebSocket-Key: not a valid Base64 value");
        }
        if (decoded.length != 16) {
            throw new ScxServerWebSocketHandshakeInvalidException("Invalid Sec-WebSocket-Key: decoded length must be 16 bytes");
        }
    }

    @Override
    public ScxWebSocket upgrade() throws IllegalSenderStateException, ScxHttpSendException, ScxWrappedException, ScxHttpReceiveException, ScxServerWebSocketHandshakeInvalidException {
        if (webSocket == null) {
            // 先校验握手 是否正确.
            validateHandshake();
            // 实现握手接受逻辑, 返回适当的响应头
            _response.headers().upgrade(WEB_SOCKET);
            _response.headers().connection(UPGRADE);
            _response.headers().set(SEC_WEBSOCKET_ACCEPT, WebSocketHandshakeHelper.computeSecWebSocketAccept(request.secWebSocketKey()));
            _response.statusCode(SWITCHING_PROTOCOLS);
            // 一旦 websocket 升级 响应发送成功, 整个 tcp 将会被 websocket 独占.
            _response.send();
            // 创建 webSocket
            webSocket = ScxWebSocket.of(
                ScxFrameWebSocket.of(_response.connection.endpoint, false, webSocketOptions.maxFrameSize()),
                webSocketOptions.maxMessageSize()
            );
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
