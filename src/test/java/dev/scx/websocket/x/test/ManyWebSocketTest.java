package dev.scx.websocket.x.test;

import dev.scx.http.x.HttpServer;
import dev.scx.http.x.HttpServerOptions;
import dev.scx.websocket.event.ScxEventWebSocket;
import dev.scx.websocket.x.ScxServerWebSocketHandshakeRequest;
import dev.scx.websocket.x.WebSocketClient;
import dev.scx.websocket.x.WebSocketUpgradeRequestFactory;
import dev.scx.websocket.x.exception.ScxClientWebSocketHandshakeRejectedException;

import java.io.IOException;

import static dev.scx.websocket.WebSocketMessageType.CLOSE;

public class ManyWebSocketTest {

    public static void main(String[] args) throws IOException, ScxClientWebSocketHandshakeRejectedException {
        test1();
    }

    public static void test1() throws IOException, ScxClientWebSocketHandshakeRejectedException {
        startServer();
        startClient();
    }

    public static void startServer() throws IOException {
        var s = System.nanoTime();
        var httpServer = new HttpServer(new HttpServerOptions().addUpgradeRequestFactory(new WebSocketUpgradeRequestFactory()));

        httpServer.onRequest(req -> {
            if (req instanceof ScxServerWebSocketHandshakeRequest wsReq) {
                var webSocket = wsReq.upgrade();
                //可以以这种 偏底层的方式使用
                while (true) {
                    var frame = webSocket.read();
                    if (frame.type() == CLOSE) {
                        break;
                    }
                    var data = new String(frame.payloadData());
                    webSocket.send(data);
                    System.out.println("服 : " + data);
                }
                System.err.println("结束了 !!!");
                httpServer.stop();
            }
        });

        httpServer.start(8080);
        System.out.println("http server started " + (System.nanoTime() - s) / 1000_000);
    }

    public static void startClient() throws ScxClientWebSocketHandshakeRejectedException {
        var httpClient = new WebSocketClient();

        var webSocket = httpClient.webSocketHandshakeRequest().uri("ws://127.0.0.1:8080/websocket").upgrade();

        //这里只有当 onConnect 走完才会 执行 来自客户端请求的监听 所以这里 创建线程发送 不阻塞 onConnect
        Thread.ofVirtual().start(() -> {
            try {
                for (int i = 0; i < 99999; i = i + 1) {
                    webSocket.send(i + "😀😀😀😀😀😀".repeat(100));
                }
                webSocket.sendClose();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        //也可以使用事件驱动的方式来使用
        ScxEventWebSocket.of(webSocket).onText((data) -> {
            System.out.println("客 : " + data);
        }).start();

    }

}
