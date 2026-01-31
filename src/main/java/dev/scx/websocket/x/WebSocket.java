package dev.scx.websocket.x;

import dev.scx.http.x.SocketIO;
import dev.scx.io.exception.NoMoreDataException;
import dev.scx.websocket.ScxWebSocket;
import dev.scx.websocket.WebSocketFrame;
import dev.scx.websocket.close_info.ScxWebSocketCloseInfo;
import dev.scx.websocket.exception.WebSocketException;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.concurrent.locks.ReentrantLock;

import static dev.scx.random.ScxRandom.randomBytes;
import static dev.scx.websocket.WebSocketOpCode.CLOSE;
import static dev.scx.websocket.close_info.WebSocketCloseInfo.NORMAL_CLOSE;
import static dev.scx.websocket.x.WebSocketProtocolFrameHelper.writeFrame;

/// WebSocket
///
/// @author scx567888
/// @version 0.0.1
public class WebSocket implements ScxWebSocket {

    private final SocketIO socketIO;
    private final WebSocketOptions options;
    //为了防止底层的 OutputStream 被乱序写入 此处需要加锁
    private final ReentrantLock lock;
    //是否是客户端 客户端需要加掩码
    private final boolean isClient;
    //限制只发送一次 close 帧
    protected boolean closeSent;

    public WebSocket(SocketIO socketIO, WebSocketOptions options, boolean isClient) {
        this.socketIO = socketIO;
        this.options = options;
        this.lock = new ReentrantLock();
        this.isClient = isClient;
    }

    @Override
    public WebSocketFrame readFrame() throws WebSocketException {
        try {
            var protocolFrame = readProtocolFrame();
            //当我们接收到了 close 帧 我们应该发送 close 帧并关闭
            if (protocolFrame.opCode() == CLOSE) {
                handleCloseFrame(protocolFrame);
            }
            return WebSocketFrame.of(protocolFrame.opCode(), protocolFrame.payloadData(), protocolFrame.fin());
        } catch (NoMoreDataException e) {
            throw new WebSocketException(NORMAL_CLOSE.code() + NORMAL_CLOSE.reason());
        }
    }

    @Override
    public void sendFrame(WebSocketFrame frame) {
        if (socketIO.tcpSocket.isClosed()) {
            throw new IllegalStateException("Cannot send frame: WebSocket is already closed");
        }

        if (closeSent) {
            if (frame.opCode() == CLOSE) { // 允许 用户多次发送 close 我们直接忽略
                return;
            } else {// 其余则抛出异常
                throw new IllegalStateException("Cannot send non-close frames after a Close frame has been sent");
            }
        }

        var protocolFrame = createProtocolFrame(frame);

        try {
            writeProtocolFrame(protocolFrame);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }

        if (frame.opCode() == CLOSE) {
            closeSent = true;
        }
    }

    @Override
    public void close() {
        socketIO.closeQuietly();  // 这里有可能已经被远端关闭 我们忽略异常
    }

    private WebSocketProtocolFrame createProtocolFrame(WebSocketFrame frame) {
        // 和服务器端不同, 客户端的是需要发送掩码的
        if (isClient) {
            var maskingKey = randomBytes(4);
            return WebSocketProtocolFrame.of(frame.fin(), frame.opCode(), maskingKey, frame.payloadData());
        } else {
            return WebSocketProtocolFrame.of(frame.fin(), frame.opCode(), frame.payloadData());
        }
    }

    private void writeProtocolFrame(WebSocketProtocolFrame protocolFrame) throws IOException {
        lock.lock();
        try {
            writeFrame(protocolFrame, socketIO.out);
        } finally {
            lock.unlock();
        }
    }

    private WebSocketProtocolFrame readProtocolFrame() throws NoMoreDataException, WebSocketException {
        if (options.mergeWebSocketFrame()) {
            return WebSocketProtocolFrameHelper.readFrameUntilLast(socketIO.in, options.maxWebSocketFrameSize(), options.maxWebSocketMessageSize());
        } else {
            return WebSocketProtocolFrameHelper.readFrame(socketIO.in, options.maxWebSocketFrameSize());
        }
    }

    public void handleCloseFrame(WebSocketProtocolFrame protocolFrame) {
        var closeInfo = ScxWebSocketCloseInfo.ofPayload(protocolFrame.payloadData());
        //1, 发送关闭响应帧
        try {
            sendClose(closeInfo); // 这里有可能无法发送 我们忽略异常
        } catch (Exception _) {

        }
        //2, 关闭底层 tcp 连接
        this.close();
    }

}
