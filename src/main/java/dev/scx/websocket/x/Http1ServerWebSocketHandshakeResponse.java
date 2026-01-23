package dev.scx.websocket.x;

import dev.scx.exception.ScxWrappedException;
import dev.scx.http.ScxHttpServerResponse;
import dev.scx.http.headers.ScxHttpHeaders;
import dev.scx.http.headers.ScxHttpHeadersWritable;
import dev.scx.http.media.MediaWriter;
import dev.scx.http.sender.IllegalSenderStateException;
import dev.scx.http.sender.ScxHttpReceiveException;
import dev.scx.http.sender.ScxHttpSendException;
import dev.scx.http.status_code.HttpStatusCode;
import dev.scx.http.status_code.ScxHttpStatusCode;
import dev.scx.http.x.http1.Http1ServerConnection;
import dev.scx.http.x.http1.Http1ServerRequest;
import dev.scx.http.x.http1.Http1ServerResponse;
import dev.scx.http.x.http1.headers.Http1Headers;
import dev.scx.http.x.sender.AbstractHttpSender;
import dev.scx.websocket.ScxServerWebSocketHandshakeResponse;
import dev.scx.websocket.ScxWebSocket;

import java.util.Base64;

import static dev.scx.digest.ScxDigest.sha1;
import static dev.scx.http.headers.HttpHeaderName.SEC_WEBSOCKET_ACCEPT;
import static dev.scx.http.status_code.HttpStatusCode.SWITCHING_PROTOCOLS;
import static dev.scx.http.x.http1.headers.connection.Connection.UPGRADE;
import static dev.scx.http.x.http1.headers.upgrade.Upgrade.WEB_SOCKET;

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
        this._response = request._request.response();
        this.webSocketOptions = webSocketOptions;
    }

    // todo 抽取出一个 SecWebSocketAcceptHelper 类?
    // 生成 Sec-WebSocket-Accept 的方法
    private static String generateSecWebSocketAccept(String key) {
        // 根据 WebSocket 协议生成接受密钥
        return Base64.getEncoder().encodeToString(sha1(key + "258EAFA5-E914-47DA-95CA-C5AB0DC85B11"));
    }

    @Override
    public ScxWebSocket webSocket() {
        return webSocket != null ? webSocket : acceptHandshake();
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

    private ScxWebSocket acceptHandshake() {
        // 实现握手接受逻辑, 返回适当的响应头
        if (webSocket == null) {
            _response.headers().upgrade(WEB_SOCKET);
            _response.headers().connection(UPGRADE);
            _response.headers().set(SEC_WEBSOCKET_ACCEPT, generateSecWebSocketAccept(request().secWebSocketKey()));

            // 一旦 websocket 升级 响应发送成功, 整个 tcp 将会被 websocket 独占.
            statusCode(SWITCHING_PROTOCOLS).send();

            webSocket = new WebSocket(_response.connection.socketIO, webSocketOptions, false);

        }
        return webSocket;
    }

}
