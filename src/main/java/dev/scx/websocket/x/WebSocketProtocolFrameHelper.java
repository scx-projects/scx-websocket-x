package dev.scx.websocket.x;

import dev.scx.io.ByteInput;
import dev.scx.io.ByteOutput;
import dev.scx.io.exception.*;
import dev.scx.websocket.WebSocketOpCode;
import dev.scx.websocket.exception.WebSocketException;

import java.util.ArrayList;

import static dev.scx.websocket.close_info.WebSocketCloseInfo.TOO_BIG;

/// WebSocketProtocolFrameHelper
///
/// @author scx567888
/// @version 0.0.1
/// @see <a href="https://www.rfc-editor.org/rfc/rfc6455">https://www.rfc-editor.org/rfc/rfc6455</a>
public class WebSocketProtocolFrameHelper {

    public static WebSocketProtocolFrame readFrameHeader(ByteInput byteInput) throws NoMoreDataException, ScxInputException, InputAlreadyClosedException {
        byte[] header = byteInput.readFully(2);

        var b1 = header[0];

        var fin = (b1 & 0b1000_0000) != 0;
        var rsv1 = (b1 & 0b0100_0000) != 0;
        var rsv2 = (b1 & 0b0010_0000) != 0;
        var rsv3 = (b1 & 0b0001_0000) != 0;
        var opCode = WebSocketOpCode.of(b1 & 0b0000_1111);

        var b2 = header[1];

        var masked = (b2 & 0b1000_0000) != 0;
        int payloadLength = b2 & 0b0111_1111;

        // 读取扩展长度
        if (payloadLength == 126) {
            byte[] extendedPayloadLength = byteInput.readFully(2);
            payloadLength = (extendedPayloadLength[0] & 0b1111_1111) << 8 |
                extendedPayloadLength[1] & 0b1111_1111;
        } else if (payloadLength == 127) {
            byte[] extendedPayloadLength = byteInput.readFully(8);
            // 我们假定长度都是在 int 范围内的 (理论上不会有 2GB 的文件会通过 websocket 发送)
            payloadLength = (int) ((extendedPayloadLength[0] & 0b1111_1111L) << 56 |
                (extendedPayloadLength[1] & 0b1111_1111L) << 48 |
                (extendedPayloadLength[2] & 0b1111_1111L) << 40 |
                (extendedPayloadLength[3] & 0b1111_1111L) << 32 |
                (extendedPayloadLength[4] & 0b1111_1111) << 24 |
                (extendedPayloadLength[5] & 0b1111_1111) << 16 |
                (extendedPayloadLength[6] & 0b1111_1111) << 8 |
                extendedPayloadLength[7] & 0b1111_1111);
        }

        byte[] maskingKey = null;

        if (masked) {
            maskingKey = byteInput.readFully(4);
        }

        return new WebSocketProtocolFrame(fin, rsv1, rsv2, rsv3, opCode, masked, payloadLength, maskingKey);
    }

    public static WebSocketProtocolFrame readFramePayload(WebSocketProtocolFrame frame, ByteInput byteInput) throws NoMoreDataException, ScxInputException, InputAlreadyClosedException {
        var payloadLength = frame.payloadLength();
        var masked = frame.masked();
        var maskingKey = frame.maskingKey();

        var payloadData = byteInput.readFully(payloadLength);

        // 掩码计算
        if (masked) {
            for (int i = 0; i < payloadLength; i = i + 1) {
                payloadData[i] = (byte) (payloadData[i] ^ maskingKey[i % 4]);
            }
        }

        return frame.payloadData(payloadData);
    }

    //读取单个帧
    public static WebSocketProtocolFrame readFrame(ByteInput byteInput, long maxWebSocketFrameSize) throws NoMoreDataException, ScxInputException, InputAlreadyClosedException, WebSocketException {
        var webSocketFrame = readFrameHeader(byteInput);

        //这里检查 最大帧大小
        if (webSocketFrame.payloadLength() > maxWebSocketFrameSize) {
            throw new WebSocketException(TOO_BIG.code() + "Frame too big");
        }

        return readFramePayload(webSocketFrame, byteInput);
    }

    public static WebSocketProtocolFrame readFrameUntilLast(ByteInput byteInput, long maxWebSocketFrameSize, long maxWebSocketMessageSize) throws NoMoreDataException, ScxInputException, InputAlreadyClosedException, WebSocketException {
        var frameList = new ArrayList<WebSocketProtocolFrame>();
        long totalPayloadLength = 0;

        while (true) {
            var webSocketFrame = readFrameHeader(byteInput);
            var framePayloadLength = webSocketFrame.payloadLength();

            // 检查单个帧大小限制
            if (framePayloadLength > maxWebSocketFrameSize) {
                throw new WebSocketException(TOO_BIG.code() + "Frame too big");
            }

            // 检查合并后的消息大小限制
            if (totalPayloadLength + framePayloadLength > maxWebSocketMessageSize) {
                throw new WebSocketException(TOO_BIG.code() + "Message too big");
            }

            webSocketFrame = readFramePayload(webSocketFrame, byteInput);

            // 增加当前帧的有效负载长度
            totalPayloadLength += framePayloadLength;

            frameList.add(webSocketFrame);
            if (webSocketFrame.fin()) {
                break;
            }
        }

        var first = frameList.get(0);

        if (frameList.size() == 1) {
            return first;
        }

        var opCode = first.opCode();
        var length = frameList.stream().mapToInt(WebSocketProtocolFrame::payloadLength).sum();
        var payloadData = new byte[length];

        int offset = 0;
        for (var webSocketFrame : frameList) {
            System.arraycopy(webSocketFrame.payloadData(), 0, payloadData, offset, webSocketFrame.payloadLength());
            offset += webSocketFrame.payloadLength();
        }

        return WebSocketProtocolFrame.of(true, opCode, payloadData);

    }

    public static void writeFrame(WebSocketProtocolFrame frame, ByteOutput byteOutput) throws ScxOutputException, OutputAlreadyClosedException {
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
