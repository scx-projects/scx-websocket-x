package dev.scx.websocket.x;

import java.util.Base64;

import static dev.scx.digest.ScxDigest.sha1;
import static dev.scx.random.ScxRandom.randomBytes;

/// WebSocketHandshakeHelper
///
/// @author scx567888
public final class WebSocketHandshakeHelper {

    /// 生成 Sec-WebSocket-Key.
    public static String generateSecWebSocketKey() {
        return Base64.getEncoder().encodeToString(randomBytes(16));
    }

    /// 计算 Sec-WebSocket-Accept.
    public static String computeSecWebSocketAccept(String secWebSocketKey) {
        return Base64.getEncoder().encodeToString(sha1(secWebSocketKey + "258EAFA5-E914-47DA-95CA-C5AB0DC85B11"));
    }

}
