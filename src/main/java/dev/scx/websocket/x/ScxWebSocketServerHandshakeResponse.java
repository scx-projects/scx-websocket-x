package dev.scx.websocket.x;

import dev.scx.exception.ScxWrappedException;
import dev.scx.http.ScxHttpServerResponse;
import dev.scx.http.headers.ScxHttpHeaderName;
import dev.scx.http.headers.ScxHttpHeaders;
import dev.scx.http.headers.accept.Accept;
import dev.scx.http.headers.content_disposition.ContentDisposition;
import dev.scx.http.headers.content_encoding.ScxContentEncoding;
import dev.scx.http.headers.cookie.Cookie;
import dev.scx.http.headers.cookie.Cookies;
import dev.scx.http.media_type.ScxMediaType;
import dev.scx.http.sender.IllegalSenderStateException;
import dev.scx.http.sender.ScxHttpReceiveException;
import dev.scx.http.sender.ScxHttpSendException;
import dev.scx.http.status_code.ScxHttpStatusCode;
import dev.scx.websocket.ScxWebSocket;
import dev.scx.websocket.x.exception.ScxWebSocketServerHandshakeInvalidException;

/// ScxWebSocketServerHandshakeResponse
///
/// @author scx567888
public interface ScxWebSocketServerHandshakeResponse extends ScxHttpServerResponse {

    @Override
    ScxWebSocketServerHandshakeRequest request();

    /// 执行 WebSocket 协议升级.
    ///
    /// - 首次调用: 提交 101 Switching Protocols, 完成升级, 创建并缓存 WebSocket 会话并返回
    /// - 再次调用: 不再产生任何 IO, 直接返回同一 WebSocket 实例.
    /// - 与普通 HTTP send(...) 提交互斥.
    ScxWebSocket upgrade() throws IllegalSenderStateException, ScxHttpSendException, ScxWrappedException, ScxHttpReceiveException, ScxWebSocketServerHandshakeInvalidException;

    // ******************** 重写返回值 以便链式调用 ********************

    @Override
    ScxWebSocketServerHandshakeResponse statusCode(ScxHttpStatusCode statusCode);

    @Override
    default ScxWebSocketServerHandshakeResponse statusCode(int statusCode) {
        return (ScxWebSocketServerHandshakeResponse) ScxHttpServerResponse.super.statusCode(statusCode);
    }

    @Override
    default ScxWebSocketServerHandshakeResponse headers(ScxHttpHeaders otherHeaders) {
        return (ScxWebSocketServerHandshakeResponse) ScxHttpServerResponse.super.headers(otherHeaders);
    }

    @Override
    default ScxWebSocketServerHandshakeResponse setHeader(ScxHttpHeaderName headerName, String... values) {
        return (ScxWebSocketServerHandshakeResponse) ScxHttpServerResponse.super.setHeader(headerName, values);
    }

    @Override
    default ScxWebSocketServerHandshakeResponse addHeader(ScxHttpHeaderName headerName, String... values) {
        return (ScxWebSocketServerHandshakeResponse) ScxHttpServerResponse.super.addHeader(headerName, values);
    }

    @Override
    default ScxWebSocketServerHandshakeResponse removeHeader(ScxHttpHeaderName headerName) {
        return (ScxWebSocketServerHandshakeResponse) ScxHttpServerResponse.super.removeHeader(headerName);
    }

    @Override
    default ScxWebSocketServerHandshakeResponse setHeader(String headerName, String... values) {
        return (ScxWebSocketServerHandshakeResponse) ScxHttpServerResponse.super.setHeader(headerName, values);
    }

    @Override
    default ScxWebSocketServerHandshakeResponse addHeader(String headerName, String... values) {
        return (ScxWebSocketServerHandshakeResponse) ScxHttpServerResponse.super.addHeader(headerName, values);
    }

    @Override
    default ScxWebSocketServerHandshakeResponse removeHeader(String headerName) {
        return (ScxWebSocketServerHandshakeResponse) ScxHttpServerResponse.super.removeHeader(headerName);
    }

    @Override
    default ScxWebSocketServerHandshakeResponse contentType(ScxMediaType contentType) {
        return (ScxWebSocketServerHandshakeResponse) ScxHttpServerResponse.super.contentType(contentType);
    }

    @Override
    default ScxWebSocketServerHandshakeResponse contentLength(long contentLength) {
        return (ScxWebSocketServerHandshakeResponse) ScxHttpServerResponse.super.contentLength(contentLength);
    }

    @Override
    default ScxWebSocketServerHandshakeResponse contentEncoding(ScxContentEncoding contentEncoding) {
        return (ScxWebSocketServerHandshakeResponse) ScxHttpServerResponse.super.contentEncoding(contentEncoding);
    }

    @Override
    default ScxWebSocketServerHandshakeResponse contentDisposition(ContentDisposition contentDisposition) {
        return (ScxWebSocketServerHandshakeResponse) ScxHttpServerResponse.super.contentDisposition(contentDisposition);
    }

    @Override
    default ScxWebSocketServerHandshakeResponse cookies(Cookies cookies) {
        return (ScxWebSocketServerHandshakeResponse) ScxHttpServerResponse.super.cookies(cookies);
    }

    @Override
    default ScxWebSocketServerHandshakeResponse setCookies(Cookies cookies) {
        return (ScxWebSocketServerHandshakeResponse) ScxHttpServerResponse.super.setCookies(cookies);
    }

    @Override
    default ScxWebSocketServerHandshakeResponse addCookie(Cookie... cookies) {
        return (ScxWebSocketServerHandshakeResponse) ScxHttpServerResponse.super.addCookie(cookies);
    }

    @Override
    default ScxWebSocketServerHandshakeResponse removeCookie(String name) {
        return (ScxWebSocketServerHandshakeResponse) ScxHttpServerResponse.super.removeCookie(name);
    }

    @Override
    default ScxWebSocketServerHandshakeResponse addSetCookie(Cookie... cookies) {
        return (ScxWebSocketServerHandshakeResponse) ScxHttpServerResponse.super.addSetCookie(cookies);
    }

    @Override
    default ScxWebSocketServerHandshakeResponse removeSetCookie(String name) {
        return (ScxWebSocketServerHandshakeResponse) ScxHttpServerResponse.super.removeSetCookie(name);
    }

    @Override
    default ScxWebSocketServerHandshakeResponse accept(Accept accept) {
        return (ScxWebSocketServerHandshakeResponse) ScxHttpServerResponse.super.accept(accept);
    }

}
