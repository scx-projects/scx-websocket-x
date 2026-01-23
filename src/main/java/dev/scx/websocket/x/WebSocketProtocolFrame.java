package dev.scx.websocket.x;

import dev.scx.websocket.WebSocketOpCode;

/// WebSocketFrame
///
/// @author scx567888
/// @version 0.0.1
/// @see <a href="https://www.rfc-editor.org/rfc/rfc6455">https://www.rfc-editor.org/rfc/rfc6455</a>
public final class WebSocketProtocolFrame {

    private final boolean fin;
    private final boolean rsv1;
    private final boolean rsv2;
    private final boolean rsv3;
    private final WebSocketOpCode opCode;
    private final boolean masked;
    private final int payloadLength;
    private final byte[] maskingKey;
    private byte[] payloadData;

    public WebSocketProtocolFrame(boolean fin,
                                  boolean rsv1,
                                  boolean rsv2,
                                  boolean rsv3,
                                  WebSocketOpCode opCode,
                                  boolean masked,
                                  int payloadLength,
                                  byte[] maskingKey,
                                  byte[] payloadData) {
        this.fin = fin;
        this.rsv1 = rsv1;
        this.rsv2 = rsv2;
        this.rsv3 = rsv3;
        this.opCode = opCode;
        this.masked = masked;
        this.payloadLength = payloadLength;
        this.maskingKey = maskingKey;
        this.payloadData = payloadData;
    }

    public WebSocketProtocolFrame(boolean fin, boolean rsv1, boolean rsv2, boolean rsv3, WebSocketOpCode opCode, boolean masked, int payloadLength, byte[] maskingKey) {
        this(fin, rsv1, rsv2, rsv3, opCode, masked, payloadLength, maskingKey, null);
    }

    public static WebSocketProtocolFrame of(boolean fin, WebSocketOpCode opCode, byte[] maskingKey, byte[] payloadData) {
        return new WebSocketProtocolFrame(fin, false, false, false, opCode, maskingKey != null, payloadData.length, maskingKey, payloadData);
    }

    public static WebSocketProtocolFrame of(boolean fin, WebSocketOpCode opCode, byte[] payloadData) {
        return of(fin, opCode, null, payloadData);
    }

    public static WebSocketProtocolFrame of(WebSocketOpCode opCode, byte[] payloadData) {
        return of(true, opCode, payloadData);
    }

    public boolean fin() {
        return fin;
    }


    public boolean rsv1() {
        return rsv1;
    }

    public boolean rsv2() {
        return rsv2;
    }

    public boolean rsv3() {
        return rsv3;
    }

    public WebSocketOpCode opCode() {
        return opCode;
    }

    public boolean masked() {
        return masked;
    }

    public int payloadLength() {
        return payloadLength;
    }

    public byte[] maskingKey() {
        return maskingKey;
    }

    public byte[] payloadData() {
        return payloadData;
    }

    public WebSocketProtocolFrame payloadData(byte[] payloadData) {
        this.payloadData = payloadData;
        return this;
    }

    @Override
    public String toString() {
        return "WebSocketFrame[" +
            "fin=" + fin + ", " +
            "rsv1=" + rsv1 + ", " +
            "rsv2=" + rsv2 + ", " +
            "rsv3=" + rsv3 + ", " +
            "opCode=" + opCode + ", " +
            "masked=" + masked + ", " +
            "payloadLength=" + payloadLength + ", " +
            "maskingKey=" + maskingKey + ", " +
            "payloadData=" + payloadData + ']';
    }

}
