package dev.scx.websocket.x.test;

import dev.scx.io.exception.NoMoreDataException;
import dev.scx.io.output.ByteArrayByteOutput;
import dev.scx.websocket.WebSocketFrame;
import dev.scx.websocket.WebSocketOpCode;
import dev.scx.websocket.exception.WebSocketException;
import dev.scx.websocket.x.WebSocketHelper;
import dev.scx.websocket.x.WebSocketProtocolFrameHelper;
import org.testng.annotations.Test;

import static dev.scx.io.ScxIO.createByteInput;
import static org.testng.Assert.*;

public class WebSocketProtocolFrameHelperTest {

    public static void main(String[] args) throws NoMoreDataException, WebSocketException {
        testReadFrame();
        testWriteFrame();
    }

    @Test
    public static void testReadFrame() throws NoMoreDataException, WebSocketException {
        byte[] frameData = {
            (byte) 0b1000_0001, // FIN + Text frame
            (byte) 0b0000_0101, // No mask, payload length = 5
            'H', 'e', 'l', 'l', 'o'
        };
        var reader = createByteInput(frameData);
        var frame = WebSocketProtocolFrameHelper.readProtocolFrame(reader, Integer.MAX_VALUE);

        assertTrue(frame.fin);
        assertFalse(frame.rsv1);
        assertFalse(frame.rsv2);
        assertFalse(frame.rsv3);
        assertEquals(frame.opCode, WebSocketOpCode.TEXT);
        assertFalse(frame.masked);
        assertEquals(frame.payloadLength, 5);
        assertEquals(frame.payloadData, new byte[]{'H', 'e', 'l', 'l', 'o'});
    }

    @Test
    public static void testWriteFrame() {
        var frame = new WebSocketFrame(WebSocketOpCode.TEXT, new byte[]{'H', 'e', 'l', 'l', 'o'}, true);
        var protocolFrame = WebSocketHelper.toProtocolFrame(frame, false);
        var out = new ByteArrayByteOutput();
        WebSocketProtocolFrameHelper.writeProtocolFrame(protocolFrame, out);
        byte[] expectedFrame = {
            (byte) 0b1000_0001, // FIN + Text frame
            (byte) 0b0000_0101, // No mask, payload length = 5
            'H', 'e', 'l', 'l', 'o'
        };
        assertEquals(expectedFrame, out.bytes());
    }

}
