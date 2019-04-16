/*
 * Copyright (c) 2017 IDCC, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.onosproject.mesh.controller.impl;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.junit.Test;
import org.onosproject.core.netty.ChannelAdapter;
import org.onosproject.mesh.MeshChannelHandlerContextAdapter;
import org.onosproject.meshmanager.api.message.LinkReport;
import org.onosproject.meshmanager.api.message.LinkReportInfo;
import org.onosproject.meshmanager.api.message.MeshNCReportInd;

import java.nio.ByteBuffer;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertEquals;


public class MeshMessageDecoderTest {

    byte[] messageData1 = {0x66, 0x34, 0x04, 0x00, 0x00, 0x08, 0x27, 0x0c, 0x00, 0x03, 0x0b, 0x00, 0x00,
            0x00, 0x01, 0x00, 0x25, 0x04, 0x10, 0x00, 0x10, 0x00, 0x00, 0x08, 0x27, 0x0c, 0x00, 0x03, 0x0c,
            0x00, 0x00, 0x08, 0x27, 0x0c, 0x00, 0x09, 0x0d};
    byte[] messageData1PeerSector = {0x00, 0x00, 0x08, 0x27, 0x0c, 0x00, 0x09, 0x0d};
    byte[] messageData2 = {0x66, 0x34, 0x04, 0x00, 0x00, 0x08, 0x27, 0x0c, 0x00, 0x02, 0x0a, 0x00, 0x00, 0x00, 0x01, 0x00,
            0x60, 0x01, 0x08, 0x00, 0x17, 0x00, 0x00, 0x08, 0x27, 0x0c, 0x00, 0x02, 0x0a, 0x00, 0x00, 0x08,
            0x27, 0x0c, 0x00, 0x01, 0x0a, 0x01, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x08, 0x00, 0x17, 0x00,
            0x00, 0x08, 0x27, 0x0c, 0x00, 0x02, 0x0b, 0x00, 0x00, 0x08, 0x27, 0x0c, 0x00, 0x04, 0x0b, 0x01,
            0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x08, 0x00, 0x17, 0x00, 0x00, 0x08, 0x27, 0x0c, 0x00, 0x02,
            0x0c, 0x00, 0x00, 0x08, 0x27, 0x0c, 0x00, 0x09, 0x0a, 0x01, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
    byte[] messageData3 = {0x66, 0x34, 0x04, 0x00, 0x00, 0x08, 0x27, 0x0c, 0x00, 0x02, 0x0a, 0x00, 0x00, 0x00, 0x01, 0x00,
            0x60, 0x01, 0x08, 0x00, 0x17, 0x00, 0x00, 0x08, 0x27, 0x0c, 0x00, 0x02, 0x0a, 0x00, 0x00, 0x08,
            0x27, 0x0c, 0x00, 0x01, 0x0a, 0x01, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x08, 0x00, 0x17, 0x00,
            0x00, 0x08, 0x27, 0x0c, 0x00, 0x02, 0x0b, 0x00, 0x00, 0x08, 0x27, 0x0c, 0x00, 0x04, 0x0b, 0x01};
    byte[] messageData4 = {0x66, 0x34};
    byte[] messageData5 = {0x66, 0x34, 0x04, 0x00, 0x00, 0x08};

    static class ConnectedChannel extends ChannelAdapter {
        @Override
        public boolean isConnected() {
            return true;
        }
    }

    private ChannelBuffer getMessageBuffer(int n) {
        ChannelBuffer channelBuffer = ChannelBuffers.dynamicBuffer();

        switch (n) {
            case 1:
                channelBuffer.writeBytes(messageData1);
                break;
            case 2:
                byte[] messageDataLength1 = new byte[messageData1.length + messageData2.length];
                System.arraycopy(messageData1, 0, messageDataLength1, 0, messageData1.length);
                System.arraycopy(messageData2, 0, messageDataLength1, messageData1.length, messageData2.length);
                channelBuffer.writeBytes(messageDataLength1);
                break;
            case 3:
                byte[] messageDataLength2 = new byte[messageData1.length + messageData3.length];
                System.arraycopy(messageData1, 0, messageDataLength2, 0, messageData1.length);
                System.arraycopy(messageData3, 0, messageDataLength2, messageData1.length, messageData3.length);
                channelBuffer.writeBytes(messageDataLength2);
                break;
            case 4:
                channelBuffer.writeBytes(messageData3);
                break;
            case 5:
                channelBuffer.writeBytes(messageData4);
                break;
            case 6:
                channelBuffer.writeBytes(messageData5);
                break;

        }
        return channelBuffer;
    }

    /**
     * Tests decoding a message on a closed channel.
     *
     * @throws Exception when an exception is thrown from the decoder
     */
    @Test
    public void testDecodeNoChannel() throws Exception {
        MeshMessageDecoder decoder = new MeshMessageDecoder();
        ChannelBuffer channelBuffer = getMessageBuffer(1);
        Object message =
                decoder.decode(new MeshChannelHandlerContextAdapter(),
                               new ChannelAdapter(),
                               channelBuffer);
        assertThat(message, nullValue());
    }

    //Case when magic number or type is missing
    @Test
    public void testMinimumLength() throws Exception {
        MeshMessageDecoder decoder = new MeshMessageDecoder();
        ChannelBuffer channelBuffer = getMessageBuffer(5);
        Object message =
                decoder.decode(new MeshChannelHandlerContextAdapter(),
                               new ConnectedChannel(),
                               channelBuffer);
        assertThat(message, nullValue());
    }

    //Case when single complete message is processed
    @Test
    public void testDecodeSingle() throws Exception {
        MeshMessageDecoder decoder = new MeshMessageDecoder();
        ChannelBuffer channelBuffer = getMessageBuffer(1);
        Object message =
                decoder.decode(new MeshChannelHandlerContextAdapter(),
                               new ConnectedChannel(),
                               channelBuffer);
        assertThat(message, notNullValue());
        assertThat(message, instanceOf(MeshNCReportInd.class));
        Long peerSector = ((MeshNCReportInd)message).getPeerSector();
        Long messageData1PeerSectorLong = bytesToLong(messageData1PeerSector);
        System.out.println(peerSector + "::" + messageData1PeerSectorLong);
        assertEquals(messageData1PeerSectorLong, peerSector);
    }

    //Case when two complete messages together are processed
    @Test
    public void testDecodeDouble() throws Exception {
        MeshMessageDecoder decoder = new MeshMessageDecoder();
        ChannelBuffer channelBuffer = getMessageBuffer(2);
        int oldReaderIndex1 = channelBuffer.readerIndex();
        System.out.println("oldReaderIndex1 :: " + oldReaderIndex1);
        Object message =
                decoder.decode(new MeshChannelHandlerContextAdapter(),
                               new ConnectedChannel(),
                               channelBuffer);
        assertThat(message, notNullValue());
        assertThat(message, instanceOf(MeshNCReportInd.class));
        Long peerSector = ((MeshNCReportInd)message).getPeerSector();
        Long messageData1PeerSectorLong = bytesToLong(messageData1PeerSector);
        System.out.println(peerSector + "::" + messageData1PeerSectorLong);
        assertEquals(peerSector, messageData1PeerSectorLong);

        int newReaderIndex1 = channelBuffer.readerIndex();
        System.out.println("newReaderIndex1 :: " + newReaderIndex1);
        int message2Index = messageData1.length;
        System.out.println("Message2 Index :: " + message2Index);
        assertEquals(newReaderIndex1, message2Index);
        message =
                decoder.decode(new MeshChannelHandlerContextAdapter(),
                               new ConnectedChannel(),
                               channelBuffer);

        assertThat(message, notNullValue());
        assertThat(message, instanceOf(MeshNCReportInd.class));
        List<LinkReportInfo> linkReportInfoList = ((MeshNCReportInd)message).getLinkReportInfoList();
        assertEquals(3, linkReportInfoList.size());
        byte linkMcs = linkReportInfoList.get(2).getLinkReport().getLinkMcs();
        assertEquals(1l, linkMcs);
    }

    //Case when one complete and one incomplete message together is processed
    @Test
    public void testDecodeOneAndHalf() throws Exception {
        MeshMessageDecoder decoder = new MeshMessageDecoder();
        ChannelBuffer channelBuffer = getMessageBuffer(3);
        int oldReaderIndex1 = channelBuffer.readerIndex();
        System.out.println("oldReaderIndex1 :: " + oldReaderIndex1);
        Object message =
                decoder.decode(new MeshChannelHandlerContextAdapter(),
                               new ConnectedChannel(),
                               channelBuffer);
        int newReaderIndex1 = channelBuffer.readerIndex();
        System.out.println("newReaderIndex1 :: " + newReaderIndex1);
        assertThat(message, notNullValue());
        assertThat(message, instanceOf(MeshNCReportInd.class));

        int message2Index = messageData1.length;
        System.out.println("Message2 Index :: " + message2Index);
        assertEquals(newReaderIndex1, message2Index);
        message =
                decoder.decode(new MeshChannelHandlerContextAdapter(),
                               new ConnectedChannel(),
                               channelBuffer);

        assertThat(message, nullValue());
    }

    //Case when dpip or transactionId or length fields are missing in the buffer
    @Test
    public void testDecodeHalf() throws Exception {
        MeshMessageDecoder decoder = new MeshMessageDecoder();
        ChannelBuffer channelBuffer = getMessageBuffer(6);
        int oldReaderIndex1 = channelBuffer.readerIndex();
        System.out.println("messageData3 Length :: " + messageData3.length);
        System.out.println("oldReaderIndex1 :: " + oldReaderIndex1);
        Object message =
                decoder.decode(new MeshChannelHandlerContextAdapter(),
                               new ConnectedChannel(),
                               channelBuffer);
        int newReaderIndex1 = channelBuffer.readerIndex();
        System.out.println("newReaderIndex1 :: " + newReaderIndex1);
        assertThat(message, nullValue());
    }

    //Case when readable bytes are less than message length
    @Test
    public void testDecodeHalf1() throws Exception {
        MeshMessageDecoder decoder = new MeshMessageDecoder();
        ChannelBuffer channelBuffer = getMessageBuffer(4);
        int oldReaderIndex1 = channelBuffer.readerIndex();
        System.out.println("messageData3 Length :: " + messageData3.length);
        System.out.println("oldReaderIndex1 :: " + oldReaderIndex1);
        Object message =
                decoder.decode(new MeshChannelHandlerContextAdapter(),
                               new ConnectedChannel(),
                               channelBuffer);
        int newReaderIndex1 = channelBuffer.readerIndex();
        System.out.println("newReaderIndex1 :: " + newReaderIndex1);
        assertThat(message, nullValue());
    }

    public Long bytesToLong(byte[] bytes) {
        ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES);
        buffer.put(bytes);
        buffer.flip();
        return buffer.getLong();
    }
}