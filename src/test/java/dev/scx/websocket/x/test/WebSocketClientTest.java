package dev.scx.websocket.x.test;

import dev.scx.http.x.HttpServer;
import dev.scx.http.x.HttpServerOptions;
import dev.scx.websocket.event.ScxEventWebSocket;
import dev.scx.websocket.exception.WebSocketException;
import dev.scx.websocket.handshake.ScxServerWebSocketHandshakeRequest;
import dev.scx.websocket.x.WebSocketClient;
import dev.scx.websocket.x.WebSocketUpgradeRequestFactory;
import org.testng.annotations.Test;

import java.io.IOException;

public class WebSocketClientTest {

    public static void main(String[] args) throws IOException, WebSocketException {
        test1();
    }

    @Test
    public static void test1() throws IOException, WebSocketException {
        startServer();
        var webSocket = new WebSocketClient().webSocketHandshakeRequest()
            .uri("http://localhost:8899/中文路径😎😎😎😎?a=1&b=llll")
            .addHeader("a", "b")
            .upgrade();

        System.out.println("连接成功");
        webSocket.send("测试数据");
    }

    private static void startServer() throws IOException {
        var httpServer = new HttpServer(new HttpServerOptions().addUpgradeRequestFactory(new WebSocketUpgradeRequestFactory()));
        httpServer.onRequest(c -> {
            System.out.println(c.method() + " " + c.uri() + " -> " + c.asString());
            //通过 c 的类型判断是不是 websocket 连接
            if (c instanceof ScxServerWebSocketHandshakeRequest w) {
                System.out.println("这是 websocket handshake");
                w.upgrade().send("hello");
                ScxEventWebSocket.of(w.upgrade()).onTextMessage((s, _) -> {
                    System.out.println("收到消息 :" + s);
                }).start();

            } else {
                // c.response().setHeader("transfer-encoding", "chunked");
                c.response().send("123");
            }
        });
        httpServer.start(8899);
        System.out.println("启动完成 !!! 端口号 : " + httpServer.localAddress().getPort());
    }

}

