package dev.scx.websocket.x;

import dev.scx.exception.ScxWrappedException;
import dev.scx.http.ScxHttpServerRequest;
import dev.scx.http.sender.IllegalSenderStateException;
import dev.scx.http.sender.ScxHttpReceiveException;
import dev.scx.http.sender.ScxHttpSendException;
import dev.scx.websocket.ScxWebSocket;
import dev.scx.websocket.x.exception.ScxWebSocketServerHandshakeInvalidException;

import static dev.scx.http.headers.HttpHeaderName.SEC_WEBSOCKET_KEY;
import static dev.scx.http.headers.HttpHeaderName.SEC_WEBSOCKET_VERSION;

/// ScxWebSocketServerHandshakeRequest
///
/// @author scx567888
public interface ScxWebSocketServerHandshakeRequest extends ScxHttpServerRequest {

    @Override
    ScxWebSocketServerHandshakeResponse response();

    default String secWebSocketKey() {
        return getHeader(SEC_WEBSOCKET_KEY);
    }

    default String secWebSocketVersion() {
        return getHeader(SEC_WEBSOCKET_VERSION);
    }

    default ScxWebSocket upgrade() throws IllegalSenderStateException, ScxHttpSendException, ScxWrappedException, ScxHttpReceiveException, ScxWebSocketServerHandshakeInvalidException {
        return response().upgrade();
    }

}
