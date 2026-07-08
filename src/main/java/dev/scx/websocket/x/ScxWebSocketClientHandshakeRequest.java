package dev.scx.websocket.x;

import dev.scx.exception.ScxWrappedException;
import dev.scx.http.ScxHttpClientRequest;
import dev.scx.http.ScxHttpClientResponse;
import dev.scx.http.headers.ScxHttpHeaderName;
import dev.scx.http.headers.ScxHttpHeaders;
import dev.scx.http.headers.accept.Accept;
import dev.scx.http.headers.content_disposition.ContentDisposition;
import dev.scx.http.headers.content_encoding.ScxContentEncoding;
import dev.scx.http.headers.cookie.Cookie;
import dev.scx.http.headers.cookie.Cookies;
import dev.scx.http.media_type.ScxMediaType;
import dev.scx.http.method.ScxHttpMethod;
import dev.scx.http.sender.IllegalSenderStateException;
import dev.scx.http.sender.ScxHttpReceiveException;
import dev.scx.http.sender.ScxHttpSendException;
import dev.scx.http.uri.ScxURI;
import dev.scx.websocket.ScxWebSocket;
import dev.scx.websocket.x.exception.ScxWebSocketClientHandshakeRejectedException;

import static dev.scx.http.method.HttpMethod.GET;

/// ScxWebSocketClientHandshakeRequest
///
/// - 1, WebSocket 协议中指定了 必须由 GET 方法 和 空请求体 所以我们这里屏蔽掉一些方法
/// - 2, 重写一些方法的返回值 方便我们链式调用
///
/// @author scx567888
public interface ScxWebSocketClientHandshakeRequest extends ScxHttpClientRequest {

    /// 发送握手请求
    /// 能力可以看作 send, 但因为 WebSocket 握手请求不允许发送请求体,
    /// 我们在此处使用 handshake 来代替 send 保证规范性.
    ScxWebSocketClientHandshakeResponse handshake() throws IllegalSenderStateException, ScxHttpSendException, ScxWrappedException, ScxHttpReceiveException;

    /// 发送握手然后等待远端接受握手并返回 websocket 对象
    default ScxWebSocket upgrade() throws IllegalSenderStateException, ScxHttpSendException, ScxWrappedException, ScxHttpReceiveException, ScxWebSocketClientHandshakeRejectedException {
        return handshake().upgrade();
    }

    // **************** 屏蔽方法 ****************

    @Override
    default ScxHttpMethod method() {
        return GET;
    }

    @Override
    default ScxHttpClientRequest method(ScxHttpMethod method) {
        throw new UnsupportedOperationException("Not supported Custom HttpMethod.");
    }

    @Override
    default ScxHttpClientResponse send(BodyWriter writer) {
        throw new UnsupportedOperationException("Not supported Custom HttpBody.");
    }

    // ******************** 重写返回值 以便链式调用 ********************

    @Override
    ScxWebSocketClientHandshakeRequest uri(ScxURI uri);

    @Override
    default ScxWebSocketClientHandshakeRequest uri(String uri) {
        return (ScxWebSocketClientHandshakeRequest) ScxHttpClientRequest.super.uri(uri);
    }

    @Override
    default ScxWebSocketClientHandshakeRequest headers(ScxHttpHeaders otherHeaders) {
        return (ScxWebSocketClientHandshakeRequest) ScxHttpClientRequest.super.headers(otherHeaders);
    }

    @Override
    default ScxWebSocketClientHandshakeRequest setHeader(ScxHttpHeaderName headerName, String... values) {
        return (ScxWebSocketClientHandshakeRequest) ScxHttpClientRequest.super.setHeader(headerName, values);
    }

    @Override
    default ScxWebSocketClientHandshakeRequest addHeader(ScxHttpHeaderName headerName, String... values) {
        return (ScxWebSocketClientHandshakeRequest) ScxHttpClientRequest.super.addHeader(headerName, values);
    }

    @Override
    default ScxWebSocketClientHandshakeRequest removeHeader(ScxHttpHeaderName headerName) {
        return (ScxWebSocketClientHandshakeRequest) ScxHttpClientRequest.super.removeHeader(headerName);
    }

    @Override
    default ScxWebSocketClientHandshakeRequest setHeader(String headerName, String... values) {
        return (ScxWebSocketClientHandshakeRequest) ScxHttpClientRequest.super.setHeader(headerName, values);
    }

    @Override
    default ScxWebSocketClientHandshakeRequest addHeader(String headerName, String... values) {
        return (ScxWebSocketClientHandshakeRequest) ScxHttpClientRequest.super.addHeader(headerName, values);
    }

    @Override
    default ScxWebSocketClientHandshakeRequest removeHeader(String headerName) {
        return (ScxWebSocketClientHandshakeRequest) ScxHttpClientRequest.super.removeHeader(headerName);
    }

    @Override
    default ScxWebSocketClientHandshakeRequest contentType(ScxMediaType contentType) {
        return (ScxWebSocketClientHandshakeRequest) ScxHttpClientRequest.super.contentType(contentType);
    }

    @Override
    default ScxWebSocketClientHandshakeRequest contentLength(long contentLength) {
        return (ScxWebSocketClientHandshakeRequest) ScxHttpClientRequest.super.contentLength(contentLength);
    }

    @Override
    default ScxWebSocketClientHandshakeRequest contentEncoding(ScxContentEncoding contentEncoding) {
        return (ScxWebSocketClientHandshakeRequest) ScxHttpClientRequest.super.contentEncoding(contentEncoding);
    }

    @Override
    default ScxWebSocketClientHandshakeRequest contentDisposition(ContentDisposition contentDisposition) {
        return (ScxWebSocketClientHandshakeRequest) ScxHttpClientRequest.super.contentDisposition(contentDisposition);
    }

    @Override
    default ScxWebSocketClientHandshakeRequest cookies(Cookies cookies) {
        return (ScxWebSocketClientHandshakeRequest) ScxHttpClientRequest.super.cookies(cookies);
    }

    @Override
    default ScxWebSocketClientHandshakeRequest setCookies(Cookies cookies) {
        return (ScxWebSocketClientHandshakeRequest) ScxHttpClientRequest.super.setCookies(cookies);
    }

    @Override
    default ScxWebSocketClientHandshakeRequest addCookie(Cookie... cookies) {
        return (ScxWebSocketClientHandshakeRequest) ScxHttpClientRequest.super.addCookie(cookies);
    }

    @Override
    default ScxWebSocketClientHandshakeRequest removeCookie(String name) {
        return (ScxWebSocketClientHandshakeRequest) ScxHttpClientRequest.super.removeCookie(name);
    }

    @Override
    default ScxWebSocketClientHandshakeRequest addSetCookie(Cookie... cookies) {
        return (ScxWebSocketClientHandshakeRequest) ScxHttpClientRequest.super.addSetCookie(cookies);
    }

    @Override
    default ScxWebSocketClientHandshakeRequest removeSetCookie(String name) {
        return (ScxWebSocketClientHandshakeRequest) ScxHttpClientRequest.super.removeSetCookie(name);
    }

    @Override
    default ScxWebSocketClientHandshakeRequest accept(Accept accept) {
        return (ScxWebSocketClientHandshakeRequest) ScxHttpClientRequest.super.accept(accept);
    }

}
