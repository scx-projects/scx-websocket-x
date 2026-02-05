package dev.scx.websocket.x;

import dev.scx.io.ByteInput;
import dev.scx.io.ByteOutput;
import dev.scx.io.exception.*;
import dev.scx.websocket.WebSocketOpCode;
import dev.scx.websocket.exception.WebSocketException;

import static dev.scx.websocket.close_info.WebSocketCloseInfo.TOO_BIG;

/// WebSocketProtocolFrameHelper
///
/// @author scx567888
/// @version 0.0.1
/// @see <a href="https://www.rfc-editor.org/rfc/rfc6455">https://www.rfc-editor.org/rfc/rfc6455</a>
public final class WebSocketProtocolFrameHelper {

    private static WebSocketProtocolFrame readProtocolFrameHeader(ByteInput byteInput) throws IllegalArgumentException, NoMoreDataException, ScxInputException, InputAlreadyClosedException {
        var protocolFrame = new WebSocketProtocolFrame();

        byte b1 = byteInput.read();

        protocolFrame.fin = (b1 & 0b1000_0000) != 0;
        protocolFrame.rsv1 = (b1 & 0b0100_0000) != 0;
        protocolFrame.rsv2 = (b1 & 0b0010_0000) != 0;
        protocolFrame.rsv3 = (b1 & 0b0001_0000) != 0;
        protocolFrame.opCode = WebSocketOpCode.of(b1 & 0b0000_1111);

        byte b2 = byteInput.read();

        protocolFrame.masked = (b2 & 0b1000_0000) != 0;
        long payloadLength = b2 & 0b0111_1111;

        // 读取扩展长度
        if (payloadLength == 126) {
            byte[] extendedPayloadLength = byteInput.readFully(2);
            payloadLength = (extendedPayloadLength[0] & 0b1111_1111L) << 8 |
                extendedPayloadLength[1] & 0b1111_1111L;
        } else if (payloadLength == 127) {
            byte[] extendedPayloadLength = byteInput.readFully(8);
            payloadLength = (extendedPayloadLength[0] & 0b1111_1111L) << 56 |
                (extendedPayloadLength[1] & 0b1111_1111L) << 48 |
                (extendedPayloadLength[2] & 0b1111_1111L) << 40 |
                (extendedPayloadLength[3] & 0b1111_1111L) << 32 |
                (extendedPayloadLength[4] & 0b1111_1111L) << 24 |
                (extendedPayloadLength[5] & 0b1111_1111L) << 16 |
                (extendedPayloadLength[6] & 0b1111_1111L) << 8 |
                extendedPayloadLength[7] & 0b1111_1111L;
        }

        protocolFrame.payloadLength = payloadLength;

        if (protocolFrame.masked) {
            protocolFrame.maskingKey = byteInput.readFully(4);
        }

        return protocolFrame;
    }

    private static WebSocketProtocolFrame readProtocolFramePayload(WebSocketProtocolFrame frame, ByteInput byteInput) throws NoMoreDataException, ScxInputException, InputAlreadyClosedException {
        var payloadLength = frame.payloadLength;
        var masked = frame.masked;
        var maskingKey = frame.maskingKey;

        // 这里我们假设 payloadLength 小于 int 值. 此处强转.
        var payloadData = byteInput.readFully((int) payloadLength);

        // 掩码计算
        if (masked) {
            for (int i = 0; i < payloadData.length; i = i + 1) {
                payloadData[i] = (byte) (payloadData[i] ^ maskingKey[i % 4]);
            }
        }

        frame.payloadData = payloadData;

        return frame;
    }

    /// 读取单个帧
    public static WebSocketProtocolFrame readProtocolFrame(ByteInput byteInput, long maxWebSocketFrameSize) throws IllegalArgumentException, NoMoreDataException, ScxInputException, InputAlreadyClosedException, WebSocketException {
        var webSocketFrame = readProtocolFrameHeader(byteInput);

        // 这里检查 最大帧大小
        if (webSocketFrame.payloadLength > maxWebSocketFrameSize) {
            throw new WebSocketException("Frame too big");
        }

        return readProtocolFramePayload(webSocketFrame, byteInput);
    }

    public static void writeProtocolFrame(WebSocketProtocolFrame frame, ByteOutput byteOutput) throws ScxOutputException, OutputAlreadyClosedException {
        // 头部
        int fullOpCode = (frame.fin() ? 0b1000_0000 : 0) |
            (frame.rsv1() ? 0b0100_0000 : 0) |
            (frame.rsv2() ? 0b0010_0000 : 0) |
            (frame.rsv3() ? 0b0001_0000 : 0) |
            frame.opCode().code();

        //写入头部
        byteOutput.write((byte) fullOpCode);

        var length = frame.payloadLength();
        var masked = frame.masked() ? 0b1000_0000 : 0;

        if (length < 126L) {
            byteOutput.write((byte) (length | masked));
        } else if (length < 65536L) {
            byteOutput.write((byte) (126 | masked));
            byteOutput.write((byte) ((length >>> 8) & 0b1111_1111));
            byteOutput.write((byte) (length & 0b1111_1111));
        } else {
            byteOutput.write((byte) (127 | masked));
            for (int i = 56; i >= 0; i -= 8) {
                byteOutput.write((byte) ((length >>> i) & 0b1111_1111));
            }
        }

        // 写入掩码键（如果有）
        if (frame.masked()) {
            byte[] maskingKey = frame.maskingKey();
            byteOutput.write(maskingKey);
        }

        // 处理掩码（如果有）
        byte[] payloadData = frame.payloadData();
        byte[] maskingKey = frame.maskingKey();
        if (frame.masked()) {
            for (int i = 0; i < payloadData.length; i = i + 1) {
                payloadData[i] = (byte) (payloadData[i] ^ maskingKey[i % 4]);
            }
        }

        // 写入有效负载数据
        byteOutput.write(payloadData);
        byteOutput.flush();
    }

}
