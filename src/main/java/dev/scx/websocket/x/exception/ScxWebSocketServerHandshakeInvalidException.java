package dev.scx.websocket.x.exception;

import dev.scx.http.exception.ScxHttpException;
import dev.scx.http.status_code.ScxHttpStatusCode;

import static dev.scx.http.status_code.HttpStatusCode.BAD_REQUEST;

/// ScxWebSocketServerHandshakeInvalidException
///
/// @author scx567888
public final class ScxWebSocketServerHandshakeInvalidException extends RuntimeException implements ScxHttpException {

    public ScxWebSocketServerHandshakeInvalidException(String message) {
        super(message);
    }

    @Override
    public ScxHttpStatusCode statusCode() {
        return BAD_REQUEST;
    }

}
