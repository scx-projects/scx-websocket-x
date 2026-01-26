package dev.scx.websocket.x.handshake;

import java.util.Base64;

import static dev.scx.digest.ScxDigest.sha1;

public final class WebSocketHandshakeHelper {

    // todo 抽取出一个 SecWebSocketAcceptHelper 类?
    // 生成 Sec-WebSocket-Accept 的方法
    public static String generateSecWebSocketAccept(String key) {
        // 根据 WebSocket 协议生成接受密钥
        return Base64.getEncoder().encodeToString(sha1(key + "258EAFA5-E914-47DA-95CA-C5AB0DC85B11"));
    }

}
