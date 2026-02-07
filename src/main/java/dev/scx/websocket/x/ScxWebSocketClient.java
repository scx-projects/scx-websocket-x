package dev.scx.websocket.x;

import dev.scx.websocket.handshake.ScxClientWebSocketHandshakeRequest;

/// WebSocket 客户端
///
/// @author scx567888
/// @version 0.0.1
public interface ScxWebSocketClient {

    ScxClientWebSocketHandshakeRequest webSocketHandshakeRequest();

}
