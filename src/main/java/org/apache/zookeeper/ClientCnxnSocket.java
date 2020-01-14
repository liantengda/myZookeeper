/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.zookeeper;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.util.LinkedList;
import java.util.List;

import org.apache.jute.BinaryInputArchive;
import org.apache.zookeeper.ClientCnxn.Packet;
import org.apache.zookeeper.common.Time;
import org.apache.zookeeper.proto.ConnectResponse;
import org.apache.zookeeper.server.ByteBufferInputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A ClientCnxnSocket does the lower level communication with a socket
 * implementation.
 * 
 * This code has been moved out of ClientCnxn so that a Netty implementation can
 * be provided as an alternative to the NIO socket code.
 * 
 */
abstract class ClientCnxnSocket {
    private static final Logger LOG = LoggerFactory.getLogger(ClientCnxnSocket.class);
    //是否初始化
    protected boolean initialized;

    /**
     * 备用知识
     * 在Java中当我们要对数据进行更底层的操作时，一般是操作数据的字节（byte）形式，这时经常会用到
     * ByteBuffer这样一个类。ByteBuffer提供了两种静态实例方式：
     * allocate
     * allocateDirect
     * 为什么要提供两种方式呢？这与Java的内存使用机制有关。第一种分配方式产生的内存开销是在JVM中的，
     * 而另外一种的分配方式产生的开销在JVM之外，以就是系统级的内存分配。当Java程序接收到外部传来的数据时，
     * 首先是被系统内存所获取，然后在由系统内存复制复制到JVM内存中供Java程序使用。所以在另外一种分配方式中，
     * 能够省去复制这一步操作，效率上会有所提高。可是系统级内存的分配比起JVM内存的分配要耗时得多，
     * 所以并非不论什么时候allocateDirect的操作效率都是最高的。
     * This buffer is only used to read the length of the incoming message.
     * 仅仅用来读取 incoming message的长度
     *
     */
    protected final ByteBuffer lenBuffer = ByteBuffer.allocateDirect(4);

    /**
     * After the length is read, a new incomingBuffer is allocated in
     * readLength() to receive the full message.
     */
    protected ByteBuffer incomingBuffer = lenBuffer;
    protected long sentCount = 0;
    protected long recvCount = 0;
    protected long lastHeard;
    protected long lastSend;
    protected long now;
    //客户端通信的发送线程
    protected ClientCnxn.SendThread sendThread;

    /**
     * The sessionId is only available here for Log and Exception messages.
     * Otherwise the socket doesn't need to know it.
     */
    protected long sessionId;

    void introduce(ClientCnxn.SendThread sendThread, long sessionId) {
        this.sendThread = sendThread;
        this.sessionId = sessionId;
    }

    void updateNow() {
        System.out.print("更新时间为当前时间------>");
        now = Time.currentElapsedTime();
    }

    int getIdleRecv() {
        return (int) (now - lastHeard);
    }

    int getIdleSend() {
        return (int) (now - lastSend);
    }

    long getSentCount() {
        return sentCount;
    }

    long getRecvCount() {
        return recvCount;
    }

    void updateLastHeard() {
        this.lastHeard = now;
    }

    void updateLastSend() {
        this.lastSend = now;
    }

    void updateLastSendAndHeard() {
        this.lastSend = now;
        this.lastHeard = now;
    }

    protected void readLength() throws IOException {
        int len = incomingBuffer.getInt();
        if (len < 0 || len >= ClientCnxn.packetLen) {
            throw new IOException("Packet len" + len + " is out of range!");
        }
        incomingBuffer = ByteBuffer.allocate(len);
    }

    /**
     * 读取连接结果
     * @throws IOException
     */
    void readConnectResult() throws IOException {
        System.out.println("ClientCnxnSocket readConnectResult------>");
        if (LOG.isTraceEnabled()) {
            StringBuilder buf = new StringBuilder("0x[");
            for (byte b : incomingBuffer.array()) {
                buf.append(Integer.toHexString(b) + ",");
            }
            buf.append("]");
            LOG.trace("readConnectResult " + incomingBuffer.remaining() + " "
                    + buf.toString());
        }
        /**
         *
         */
        ByteBufferInputStream bbis = new ByteBufferInputStream(incomingBuffer);
        BinaryInputArchive bbia = BinaryInputArchive.getArchive(bbis);
        ConnectResponse conRsp = new ConnectResponse();
        //反序列化
        conRsp.deserialize(bbia, "connect");

        // read "is read-only" flag
        boolean isRO = false;
        try {
            isRO = bbia.readBool("readOnly");
        } catch (IOException e) {
            // this is ok -- just a packet from an old server which
            // doesn't contain readOnly field
            LOG.warn("Connected to an old server; r-o mode will be unavailable");
        }

        this.sessionId = conRsp.getSessionId();
        sendThread.onConnected(conRsp.getTimeOut(), this.sessionId,
                conRsp.getPasswd(), isRO);
    }

    abstract boolean isConnected();

    abstract void connect(InetSocketAddress addr) throws IOException;

    abstract SocketAddress getRemoteSocketAddress();

    abstract SocketAddress getLocalSocketAddress();

    abstract void cleanup();

    abstract void close();

    abstract void wakeupCnxn();

    abstract void enableWrite();

    abstract void disableWrite();

    abstract void enableReadWriteOnly();

    abstract void doTransport(int waitTimeOut, List<Packet> pendingQueue,
            LinkedList<Packet> outgoingQueue, ClientCnxn cnxn)
            throws IOException, InterruptedException;

    abstract void testableCloseSocket() throws IOException;

    abstract void sendPacket(Packet p) throws IOException;
}
