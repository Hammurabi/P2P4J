package com.riverssen.p2p4j;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicBoolean;

public class Node implements Runnable {
    private NodeID nodeID;
    private Server server;
    private Socket socket;
    private Map<Long, MessageFulfillRequest>    getRequests;
    private AtomicBoolean                       connected;
    private Set<Packet>                         messages;
    private Set<byte[]>                         received;
    private DataOutputStream                    dataOutputStream;
    private InputStream                         inputStream;

    public Node(NodeID nodeID, Server server, Socket socket) throws IOException {
        this.nodeID = nodeID;
        this.server = server;
        this.socket = socket;
        this.getRequests = new LinkedHashMap<>();
        this.connected = new AtomicBoolean(true);
        this.messages = Collections.synchronizedSet(new LinkedHashSet<>());
        this.dataOutputStream = new DataOutputStream(socket.getOutputStream());
        this.inputStream = socket.getInputStream();
        this.received = Collections.synchronizedSet(new LinkedHashSet<>());
        this.server.getParameters().getThreadPool().execute(()->{
            while (isConnected()) {
                try {
                    CompletableFuture<byte[]> messageReceived = waitFor();
                    if (messageReceived == null)
                        continue;
                    byte data[] = messageReceived.get();
                    received.add(data);

                    Thread.sleep(5);
                } catch (ExecutionException e) {
                } catch (InterruptedException e) {
                } catch (IOException e) {
                }
            }
        });
        this.server.getParameters().getThreadPool().execute(()->{
            while (isConnected()) {
                synchronized (messages) {
                    Iterator<Packet> iterator = messages.iterator();

                    while (iterator.hasNext())
                    {
                        sendMessage(iterator.next());
                        iterator.remove();
                    }
                }
            }
        });
    }

    public NodeID getNodeID() {
        return nodeID;
    }

    public Server getServer() {
        return server;
    }

    public Socket getSocket() {
        return socket;
    }

    protected boolean sendMessage(Packet packet)
    {
        try {
            if (packet.getSize() > server.getParameters().getMaxBytesSend())
                return false;

            byte encodedBytes[] = nodeID.getScheme().encrypt(packet);
            dataOutputStream.writeInt(encodedBytes.length);
            dataOutputStream.write(encodedBytes);
            dataOutputStream.flush();

            /**
             * Cache the hash of this packet to insure
             * that it doesn't get sent and or received
             * again.
             */
            nodeID.cache(packet.getHashCode());

            return true;
        } catch (IOException e) {
        }

        return false;
    }

    public boolean send(Packet packet) {
        if (connected.get()) {
            /**
             * Check that the packet has not been sent
             * before.
             *
             * This is implementation specific.
             */
            if (nodeID.screen(packet.getHashCode()))
                return false;
            return messages.add(packet);
        }
        return false;
    }

    public CompletableFuture<Packet> sendAndGet(Packet packet, long timeOut)
    {
        MessageFulfillRequest request = new MessageFulfillRequest(System.currentTimeMillis(), timeOut);

        if (send(packet))
        {
            getRequests.put(packet.getSerialNumber(), request);
            return request.getFuture();
        }

        return null;
    }

    public void fulfillRequest(long serialNumber, Packet packet)
    {
        if (getRequests.containsKey(serialNumber))
        {
            MessageFulfillRequest request = getRequests.get(serialNumber);
            request.getFuture().complete(packet);
            getRequests.remove(serialNumber);
        }
    }

    public boolean isConnected() {
        return connected.get();
    }

    public void dropConnection() throws IOException {
        if (!connected.get()) return;
        server.getParameters().getDisconnectCallback().onEvent(server, this, nodeID);
        connected.set(false);
        socket.close();
    }

    @Deprecated
    public void dropConnectionUnconditional() throws IOException {
        if (!connected.get()) return;
        connected.set(false);
        socket.close();
    }

    private static int makeInt(byte b3, byte b2, byte b1, byte b0) {
        return (((b3       ) << 24) |
                ((b2 & 0xff) << 16) |
                ((b1 & 0xff) <<  8) |
                ((b0 & 0xff)      ));
    }

    public CompletableFuture<byte[]> waitFor() throws ExecutionException, InterruptedException, IOException {
        CompletableFuture<byte[]> result = new CompletableFuture<>();

        try {
            int length = 0;
            {
                byte data[] = new byte[4];
                int count = 0;
                long timeStamp = System.currentTimeMillis();
                while (count < 4) {
                    int read = inputStream.read(data);
                    if (read < 0)
                        return null;


                    count += read;

                    if ((count == 0))
                        return null;
                }

                length = makeInt(data[0], data[1], data[2], data[3]);
            }

            if (length <= 0 || length > server.getParameters().getMaxBytesReceive())
            {
                nodeID.incrementSpam();
                throw new IOException("invalid byte count.");
            }
            else {
                byte data[] = new byte[length];
                int count = 0;
                long timeStamp = System.currentTimeMillis();
                while (count < length) {
                    int read = inputStream.read(data);
                    if (read < 0)
                        break;

                    count += read;

                    if ((count < length) && (System.currentTimeMillis() - timeStamp) > (5 * length))
                        return null;
                }

                result.complete(data);
                return result;
            }
        } catch (Exception e)
        {
        }

        return null;
    }

    @Override
    public void run() {
        while (server.getParameters().getKeepAlive() && !nodeID.getShouldBan() && connected.get()) {
            synchronized (received) {
                Iterator<byte[]> iterator = received.iterator();

                while (iterator.hasNext())
                {
                    byte[] data = iterator.next();

                    if (data != null && data.length > 0)
                    {
                        byte hashCode[] = server.getParameters().getMessageReceivedCallback().onEvent(server, this, data);
                        if (nodeID.screen(hashCode)) {
                            nodeID.incrementSpam();
                        } else {
                            nodeID.cache(hashCode);
                        }
                    }
                    else if (data != null && data.length == 0)
                        nodeID.incrementSpam();

                    iterator.remove();
                }
            }

            try {
                Set<Map.Entry<Long, MessageFulfillRequest>> requests = getRequests.entrySet();

                synchronized (getRequests) {
                    Iterator<Map.Entry<Long, MessageFulfillRequest>> iterator = requests.iterator();

                    while (iterator.hasNext())
                    {
                        Map.Entry<Long, MessageFulfillRequest> request = iterator.next();
                        if (request.getValue().shouldRemove())
                            iterator.remove();
                    }
                }
            } catch (Exception e)
            {
            }
        }

        getRequests.clear();
        try {
            dropConnection();
        } catch (IOException e) {
        }
    }
}