package dev.scx.websocket.x.test;

import dev.scx.http.x.HttpServer;
import dev.scx.http.x.HttpServerOptions;
import dev.scx.websocket.event.ScxEventWebSocket;
import dev.scx.websocket.handshake.ScxServerWebSocketHandshakeRequest;
import dev.scx.websocket.x.WebSocketClient;
import dev.scx.websocket.x.WebSocketUpgradeRequestFactory;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;

public class WebSocketServerTest {

    public static final char[] NUMBER_AND_LOWER_LETTER = "0123456789abcdefghijklmnopqrstuvwxyz".toCharArray();
    static List<String> eventWebSockets = new CopyOnWriteArrayList<>();
    static AtomicInteger number = new AtomicInteger(0);

    public static void main(String[] args) throws IOException {
        test1();
    }

    public static void test1() throws IOException {
        //测试 是否存在 幽灵连接
        // 查看 localhost: 8080 eventWebSockets 最终应该为 0 , number 也应该为 0 (表示没有多次触发 onClose)
        startServer();

        for (int i = 0; i < 10000; i = i + 1) {
            sleep(1);
            Thread.ofVirtual().start(() -> {
                try {
                    var scxWebSocket = new WebSocketClient().webSocketHandshakeRequest().uri("ws://localhost:8080/websocket").upgrade();
                    sleep(1000);
                    scxWebSocket.sendClose();
                } catch (Exception _) {

                }
            });
        }

    }

    public static void startServer() throws IOException {
        var httpServer = new HttpServer(new HttpServerOptions().addUpgradeRequestFactory(new WebSocketUpgradeRequestFactory()));

        httpServer.onRequest(c -> {
            if (c instanceof ScxServerWebSocketHandshakeRequest wsRequest) {
                System.out.println("收到 WebSocket 握手请求 !!!");
                number.addAndGet(1);
                // 使用执行器
//                 var scxEventWebSocket = ScxEventWebSocket.of(wsRequest.webSocket(), Executors.newVirtualThreadPerTaskExecutor());
                var scxEventWebSocket = ScxEventWebSocket.of(wsRequest.upgrade());
                var s = randomString(10);
                eventWebSockets.add(s);
                scxEventWebSocket.onClose((code, reason) -> {
                    eventWebSockets.remove(s);
                    number.addAndGet(-1);
                    System.err.println("WebSocket closed !!! code : " + code + " reason : " + reason);
                });
                scxEventWebSocket.onError(e -> {
                    System.err.println("WebSocket error : " + e.getMessage());
                });
                // 使用执行器
//                 scxEventWebSocket.start(Executors.newVirtualThreadPerTaskExecutor());
                scxEventWebSocket.start();
            } else {
                System.out.println("收到 普通 请求 !!!");
                c.response().send("number  : " + number.get() + " \r\nclients : \r\n" +
                    String.join("\r\n", eventWebSockets)
                );
            }
        });

        httpServer.start(8080);

    }

    private static void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {

        }
    }

    public static String randomString(int size) {
        var value = new char[size];
        int length = NUMBER_AND_LOWER_LETTER.length;
        for (int i = 0; i < size; i = i + 1) {
            value[i] = NUMBER_AND_LOWER_LETTER[ThreadLocalRandom.current().nextInt(length)];
        }
        return new String(value);
    }

}
