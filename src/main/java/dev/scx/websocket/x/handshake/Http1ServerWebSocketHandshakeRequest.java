package dev.scx.websocket.x.handshake;

import dev.scx.http.headers.ScxHttpHeaders;
import dev.scx.http.method.ScxHttpMethod;
import dev.scx.http.peer_info.PeerInfo;
import dev.scx.http.uri.ScxURI;
import dev.scx.http.version.HttpVersion;
import dev.scx.http.x.http1.Http1ServerConnection;
import dev.scx.http.x.http1.Http1ServerRequest;
import dev.scx.http.x.http1.headers.Http1Headers;
import dev.scx.http.x.http1.request_line.Http1RequestLine;
import dev.scx.io.ByteInput;
import dev.scx.websocket.handshake.ScxServerWebSocketHandshakeRequest;
import dev.scx.websocket.x.WebSocketOptions;

/// 基于 http1 的 websocket 握手请求
///
/// @author scx567888
/// @version 0.0.1
public class Http1ServerWebSocketHandshakeRequest implements ScxServerWebSocketHandshakeRequest {

    private final Http1ServerRequest _request;
    private final Http1ServerWebSocketHandshakeResponse response;

    public Http1ServerWebSocketHandshakeRequest(Http1RequestLine requestLine, Http1Headers headers, Long bodyLength, ByteInput bodyByteInput, Http1ServerConnection connection, WebSocketOptions webSocketOptions) {
        this._request = new Http1ServerRequest(requestLine, headers, bodyLength, bodyByteInput, connection);
        this.response = new Http1ServerWebSocketHandshakeResponse(this, webSocketOptions);
    }

    /// 原始 _request
    Http1ServerRequest _request() {
        return _request;
    }

    @Override
    public Http1ServerWebSocketHandshakeResponse response() {
        return this.response;
    }

    @Override
    public ScxHttpMethod method() {
        return _request.method();
    }

    @Override
    public ScxURI uri() {
        return _request.uri();
    }

    @Override
    public HttpVersion version() {
        return _request.version();
    }

    @Override
    public ScxHttpHeaders headers() {
        return _request.headers();
    }

    @Override
    public Long bodyLength() {
        return _request.bodyLength();
    }

    @Override
    public ByteInput body() {
        return _request.body();
    }

    @Override
    public PeerInfo remotePeer() {
        return _request.remotePeer();
    }

    @Override
    public PeerInfo localPeer() {
        return _request.localPeer();
    }

}
