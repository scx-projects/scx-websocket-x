package dev.scx.websocket.x.test;

import dev.scx.io.exception.NoMoreDataException;
import dev.scx.io.output.ByteArrayByteOutput;
import dev.scx.websocket.WebSocketOpCode;
import dev.scx.websocket.exception.WebSocketParseException;
import dev.scx.websocket.x.WebSocketProtocolFrame;
import dev.scx.websocket.x.WebSocketProtocolFrameHelper;
import org.testng.annotations.Test;

import static dev.scx.io.ScxIO.createByteInput;
import static org.testng.Assert.*;

public class WebSocketProtocolFrameHelperTest {

    public static void main(String[] args) throws NoMoreDataException, WebSocketParseException {
        testReadFrame();
        testWriteFrame();
        testReadFrameUntilLast();
    }

    @Test
    public static void testReadFrame() throws NoMoreDataException, WebSocketParseException {
        byte[] frameData = {
            (byte) 0b1000_0001, // FIN + Text frame
            (byte) 0b0000_0101, // No mask, payload length = 5
            'H', 'e', 'l', 'l', 'o'
        };
        var reader = createByteInput(frameData);
        var frame = WebSocketProtocolFrameHelper.readFrame(reader, Integer.MAX_VALUE);

        assertTrue(frame.fin());
        assertFalse(frame.rsv1());
        assertFalse(frame.rsv2());
        assertFalse(frame.rsv3());
        assertEquals(frame.opCode(), WebSocketOpCode.TEXT);
        assertFalse(frame.masked());
        assertEquals(frame.payloadLength(), 5);
        assertEquals(frame.payloadData(), new byte[]{'H', 'e', 'l', 'l', 'o'});
    }

    @Test
    public static void testWriteFrame() {
        var frame = WebSocketProtocolFrame.of(true, WebSocketOpCode.TEXT, new byte[]{'H', 'e', 'l', 'l', 'o'});
        var out = new ByteArrayByteOutput();
        WebSocketProtocolFrameHelper.writeFrame(frame, out);
        byte[] expectedFrame = {
            (byte) 0b1000_0001, // FIN + Text frame
            (byte) 0b0000_0101, // No mask, payload length = 5
            'H', 'e', 'l', 'l', 'o'
        };
        assertEquals(expectedFrame, out.bytes());
    }

    @Test
    public static void testReadFrameUntilLast() throws NoMoreDataException, WebSocketParseException {
        byte[] frameData = {
            (byte) 0b0000_0001, // Text frame, not final
            (byte) 0b0000_0011, // No mask, payload length = 3
            'H', 'e', 'l',
            (byte) 0b1000_0000, // Continuation frame, final
            (byte) 0b0000_0010, // No mask, payload length = 2
            'l', 'o'
        };
        var reader = createByteInput(frameData);
        var frame = WebSocketProtocolFrameHelper.readFrameUntilLast(reader, Integer.MAX_VALUE, Integer.MAX_VALUE);

        assertTrue(frame.fin());
        assertFalse(frame.rsv1());
        assertFalse(frame.rsv2());
        assertFalse(frame.rsv3());
        assertEquals(frame.opCode(), WebSocketOpCode.TEXT);
        assertFalse(frame.masked());
        assertEquals(frame.payloadLength(), 5);
        assertEquals(frame.payloadData(), new byte[]{'H', 'e', 'l', 'l', 'o'});
    }

}
