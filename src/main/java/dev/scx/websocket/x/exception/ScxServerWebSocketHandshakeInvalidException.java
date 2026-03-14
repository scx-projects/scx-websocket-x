package dev.scx.websocket.x.exception;

import dev.scx.http.exception.ScxHttpException;
import dev.scx.http.status_code.ScxHttpStatusCode;

import static dev.scx.http.status_code.HttpStatusCode.BAD_REQUEST;

/// ScxServerWebSocketHandshakeInvalidException
///
/// @author scx567888
/// @version 0.0.1
public final class ScxServerWebSocketHandshakeInvalidException extends RuntimeException implements ScxHttpException {

    public ScxServerWebSocketHandshakeInvalidException(String message) {
        super(message);
    }

    @Override
    public ScxHttpStatusCode statusCode() {
        return BAD_REQUEST;
    }

}
