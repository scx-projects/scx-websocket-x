package dev.scx.websocket.x;

/// WebSocketOptions
///
/// @author scx567888
/// @version 0.0.1
public final class WebSocketOptions {

    private int maxFrameSize;
    private int maxMessageSize;

    public WebSocketOptions() {
        this.maxFrameSize = 1024 * 1024 * 16; // 默认 16 MB
        this.maxMessageSize = 1024 * 1024 * 64; // 默认 64 MB
    }

    public int maxFrameSize() {
        return maxFrameSize;
    }

    public WebSocketOptions maxFrameSize(int maxFrameSize) {
        this.maxFrameSize = maxFrameSize;
        return this;
    }

    public int maxMessageSize() {
        return maxMessageSize;
    }

    public WebSocketOptions maxMessageSize(int maxMessageSize) {
        this.maxMessageSize = maxMessageSize;
        return this;
    }

}
