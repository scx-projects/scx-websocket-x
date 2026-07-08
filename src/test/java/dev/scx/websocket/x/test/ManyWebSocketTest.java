package dev.scx.websocket.x.test;

import dev.scx.http.x.HttpServer;
import dev.scx.http.x.HttpServerOptions;
import dev.scx.websocket.CloseMessage;
import dev.scx.websocket.TextMessage;
import dev.scx.websocket.event.ScxEventWebSocket;
import dev.scx.websocket.x.ScxWebSocketServerHandshakeRequest;
import dev.scx.websocket.x.WebSocketClient;
import dev.scx.websocket.x.WebSocketUpgradeRequestFactory;
import dev.scx.websocket.x.exception.ScxWebSocketClientHandshakeRejectedException;
import org.testng.annotations.Test;

import java.io.IOException;

public class ManyWebSocketTest {

    public static void main(String[] args) throws IOException, ScxWebSocketClientHandshakeRejectedException {
        test1();
    }

//    @Test
    public static void test1() throws IOException, ScxWebSocketClientHandshakeRejectedException {
        startServer();
        startClient();
    }

    public static void startServer() throws IOException {
        var s = System.nanoTime();
        var httpServer = new HttpServer(new HttpServerOptions().addUpgradeRequestFactory(new WebSocketUpgradeRequestFactory()));

        httpServer.onRequest(req -> {
            if (req instanceof ScxWebSocketServerHandshakeRequest wsReq) {
                var webSocket = wsReq.upgrade();
                // 可以以这种 偏底层的方式使用
                while (true) {
                    var message = webSocket.read();
                    if (message instanceof CloseMessage) {
                        break;
                    }
                    if (message instanceof TextMessage textMessage) {
                        webSocket.send(textMessage.text().substring(0, textMessage.text().indexOf(" : ")) + " : " + "🤣🤣🤣🤣🤣🤣".repeat(100));
                        System.out.println("服 : " + textMessage.text());
                    }
                }
                System.err.println("结束了 !!!");
                httpServer.stop();
            }
        });

        httpServer.start(8880);
        System.out.println("http server started " + (System.nanoTime() - s) / 1000_000);
    }

    public static void startClient() throws ScxWebSocketClientHandshakeRejectedException {
        var webSocketClient = new WebSocketClient();

        var webSocket = webSocketClient.handshakeRequest().uri("ws://127.0.0.1:8880/websocket").upgrade();

        // 循环发送
        Thread.ofVirtual().start(() -> {
            try {
                for (int i = 0; i < 99999; i = i + 1) {
                    webSocket.send(i + " : " + "😀😀😀😀😀😀".repeat(100));
                }
                webSocket.sendClose();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        // 也可以使用事件驱动的方式来使用
        ScxEventWebSocket.of(webSocket).onText((data) -> {
            System.out.println("客 : " + data);
        }).start();

    }

}
