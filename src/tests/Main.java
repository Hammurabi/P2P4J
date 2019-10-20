package tests;

import com.riverssen.*;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.concurrent.*;

public class Main {
    public static boolean isAlive = true;
    public static void main(String args[]) throws MessageFactoryException, IOException {
        createServerA("Joey", 6969);
        createServerB("Chandler", 6970);

//        waiting for connection
//        waiting for connection
//        Joey sent 22
//        Chandler sent 23
//        Chandler received str: So it seems like this internet thing is here to stay, huh?
//        Joey received str: How you doin'
//        Joey received str request (getname).
//                sent: true 15
//        Chandler received str response (Joey) SUCCESS.
//                Chandler sent 17 -9223372036854775808
//        Joey received int request (0).
//                sent: true 15
//        Chandler received int response (4) SUCCESS.
//                Chandler sent 14 -9223372036854775807
//        Chandler.. response1: SUCCESS Joey
//        Chandler.. response2: SUCCESS 4
    }

    public static void createServerA(String name, int port) throws IOException {
        MessageFactory<PacketType, ResponseCode> factory = new Factory();

        Server server = createServer(name, port);
        Executors.newSingleThreadExecutor().execute(server);

        Executors.newSingleThreadExecutor().execute(()->{
            System.out.println("waiting for connection");
            while (server.getNetworkSize() == 0)
            {
            }

            try {
                Message message = factory.<String>makeMessage(PacketType.UTF, "So it seems like this internet thing is here to stay, huh?");
                System.out.println(name + " sent " + message.getPacket().getSize());
                server.broadcast(message);
            } catch (MessageFactoryException e) {
                e.printStackTrace();
            }

            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
    }

    public static void createServerB(String name, int port) throws IOException {
        MessageFactory<PacketType, ResponseCode> factory = new Factory();

        Server server = createServer(name, port);
        NodeID node = new NodeIDImpl(InetAddress.getLocalHost(), 6969);
        Executors.newSingleThreadExecutor().execute(server);
        server.connect(node);

        System.out.println("waiting for connection");
        while (server.getNetworkSize() == 0) {}

        Executors.newSingleThreadExecutor().execute(()->{
            Node theNode = server.getNodeMap().get(node);

            try {
                Message request1 = factory.<String>makeRequest(PacketType.UTF, "getname");
                Message request2 = factory.<Integer>makeRequest(PacketType.INT32, 0);
                Message message = factory.<String>makeMessage(PacketType.UTF, "How you doin'");

                message.send(theNode);
                System.out.println(name + " sent " + message.getPacket().getSize());
                ResponsePacket<String, ResponseCode> response1 = request1.<String, ResponseCode>sendGetReply(theNode, 5000);
                System.out.println(name + " sent " + request1.getPacket().getSize() + " " + request1.getPacket().getSerialNumber());
                ResponsePacket<Integer, ResponseCode> response2 = request2.<Integer, ResponseCode>sendGetReply(theNode, 5000);
                System.out.println(name + " sent " + request2.getPacket().getSize() + " " + request2.getPacket().getSerialNumber());

                System.out.println(name + ".. response1: " + response1);
                System.out.println(name + ".. response2: " + response2);
            } catch (MessageFactoryException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        });
    }
    public static Server createServer(String name, int port) throws IOException {
        MessageFactory<PacketType, ResponseCode> factory = new Factory();
        return new Server(new ServerParameters(5000, 5000, 20, port,
                Executors.newCachedThreadPool(),
                (servr, node, packetData) -> {
                    try {
                        ByteBuffer buffer = ByteBuffer.wrap(packetData);
                        int size = packetData.length;

                        if (size < 8)
                            return;
                        long serial = buffer.getLong();

                        int code = Byte.toUnsignedInt(buffer.get());
                        if (code == MessageCode.MESSAGE.ordinal()) {
                            int type = Byte.toUnsignedInt(buffer.get());
                            if (type == PacketType.INT32.ordinal())
                                System.out.println(name + " received int: " + buffer.getInt());
                            else if (type == PacketType.UTF.ordinal()) {
                                byte bytes[] = new byte[buffer.remaining()];
                                buffer.get(bytes);
                                String string = new String(bytes);
                                System.out.println(name + " received str: " + string);
                            } else
                                System.out.println(name + "received err.");
                        } else if (code == MessageCode.REQUEST.ordinal()) {
                            int type = Byte.toUnsignedInt(buffer.get());
                            if (type == PacketType.INT32.ordinal()) {
                                int i = buffer.getInt();
                                System.out.println(name + " received int request (" + i + ").");
                                Message message = factory.<Integer>makeResponse(PacketType.INT32, ResponseCode.SUCCESS, serial, name.length());
                                System.out.println("sent: " + message.send(node) + " " + message.getPacket().getSize());
                            } else if (type == PacketType.UTF.ordinal()) {
                                byte bytes[] = new byte[buffer.remaining()];
                                buffer.get(bytes);
                                String string = new String(bytes);
                                System.out.println(name + " received str request (" + string + ").");
                                Message message = factory.<String>makeResponse(PacketType.UTF, ResponseCode.SUCCESS, serial, name);
                                System.out.println("sent: " + message.send(node) + " " + message.getPacket().getSize());
                            } else
                                System.out.println(name + "received err.");
                        } else if (code == MessageCode.RESPONSE.ordinal()) {
                            int type = Byte.toUnsignedInt(buffer.get());
                            if (type == PacketType.INT32.ordinal()) {
                                int responseCode = buffer.get();
                                int i = buffer.getInt();
                                System.out.println(name + " received int response (" + i + ") " + ResponseCode.values()[responseCode] + ".");
                                node.fulfillRequest(serial, new GenericPacket(packetData));
                            } else if (type == PacketType.UTF.ordinal()) {
                                int responseCode = buffer.get();
                                byte bytes[] = new byte[buffer.remaining()];
                                buffer.get(bytes);
                                String string = new String(bytes);
                                System.out.println(name + " received str response (" + string + ") " + ResponseCode.values()[responseCode] + ".");
                                node.fulfillRequest(serial, new GenericPacket(packetData));
                            } else
                                System.out.println(name + "received err.");
                        }
                        else
                            System.out.println("well, " + name + " received an unknown whatever the hell this is.");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }, (servr, socket) -> {
            try {
                NodeID nodeID = new NodeIDImpl(socket.getInetAddress(), socket.getPort());
                servr.connect(servr.getParameters().getFactory().makeNode(nodeID, servr, socket));
            } catch (IOException e) {
                e.printStackTrace();
            }
            }, (servr, node, nodeID)->{}, new NodeFactory() {
            @Override
            public Node makeNode(NodeID nodeID, Server server, Socket socket) throws IOException {
                return new Node(nodeID, server, socket);
            }

            @Override
            public Node makeConnection(NodeID nodeID, Server server, Socket socket) throws IOException {
                return new Node(nodeID, server, socket);
            }
        }) {
            @Override
            public boolean getKeepAlive() {
                return isAlive;
            }
        });
    }

    public static class NodeIDImpl extends NodeID {
        private boolean banned = true;
        private int spam = 0;
        private InetAddress address;
        private int port;

        public NodeIDImpl(InetAddress address, int port)
        {
            this.address = address;
            this.port = port;
            this.setShouldBan(false);
        }

        @Override
        public boolean getShouldBan() {
            return banned;
        }

        @Override
        public void setShouldBan(boolean ban) {
            this.banned = ban;
        }

        @Override
        public int getPort() {
            return port;
        }

        @Override
        public InetAddress getAddress() {
            return address;
        }

        @Override
        public Descriptor getDescriptorStruct() {
            return null;
        }

        @Override
        public int getSpam() {
            return spam;
        }

        @Override
        public void incrementSpam() {
            spam ++;
            if (spam > 12)
                setShouldBan(true);
        }

        @Override
        public boolean screen(byte[] object) {
            return false;
        }

        @Override
        public void cache(byte[] object) {
        }
    }

    public static enum PacketType {
        UTF,
        INT32,
    }

    public static class GenericSendPacket extends Packet {
        private long serialNumber;
        private int code;
        private int messageType;
        private byte data[];

        public GenericSendPacket(MessageCode code, PacketType messageType, long serialNumber, byte data[]) {
            this.serialNumber = serialNumber;
            this.code = code.ordinal();
            this.messageType = messageType.ordinal();
            this.data = data;
        }

        @Override
        public int getSize() {
            return 8 + 2 + data.length;
        }

        @Override
        public long getSerialNumber() {
            return serialNumber;
        }

        @Override
        @Deprecated
        public byte[] getMessageData() {
            return new byte[0];
        }

        @Override
        public ByteBuffer getBuffer() {
            ByteBuffer buffer = ByteBuffer.allocate(getSize());
            fill(buffer);
            buffer.flip();
            return buffer;
        }

        @Override
        public void fill(ByteBuffer buffer) {
            buffer.putLong(serialNumber);
            buffer.put((byte) code);
            buffer.put((byte) messageType);
            buffer.put(data);
        }

        @Override
        public void write(DataOutputStream dataOutputStream) throws IOException {
            dataOutputStream.writeLong(serialNumber);
            dataOutputStream.write((byte) code);
            dataOutputStream.write((byte) messageType);
            dataOutputStream.write(data);
        }
    }

    public static class GenericResponsePacket extends Packet {
        private long serialNumber;
        private int code;
        private int responseCode;
        private int messageType;
        private byte data[];

        public GenericResponsePacket(MessageCode code, ResponseCode responseCode, PacketType messageType, long serialNumber, byte data[]) {
            this.serialNumber = serialNumber;
            this.code = code.ordinal();
            this.responseCode = responseCode.ordinal();
            this.messageType = messageType.ordinal();
            this.data = data;
        }

        @Override
        public int getSize() {
            return 3 + 8 + data.length;
        }

        @Override
        public long getSerialNumber() {
            return serialNumber;
        }

        @Override
        @Deprecated
        public byte[] getMessageData() {
            return new byte[0];
        }

        @Override
        public ByteBuffer getBuffer() {
            ByteBuffer buffer = ByteBuffer.allocate(getSize());
            fill(buffer);
            buffer.flip();
            return buffer;
        }

        @Override
        public void fill(ByteBuffer buffer) {
            buffer.putLong(serialNumber);
            buffer.put((byte) code);
            buffer.put((byte) messageType);
            buffer.put((byte) responseCode);
            buffer.put(data);
        }

        @Override
        public void write(DataOutputStream dataOutputStream) throws IOException {
            dataOutputStream.writeLong(serialNumber);
            dataOutputStream.write((byte) code);
            dataOutputStream.write((byte) messageType);
            dataOutputStream.write((byte) responseCode);
            dataOutputStream.write(data);
        }
    }

    public static class StringMessage extends Message {
        protected StringMessage(MessageCode code, long serial, String string) {
            super(new GenericSendPacket(code, PacketType.UTF, serial, string.getBytes()));
        }

        @Override
        public ResponsePacket<String, ResponseCode> sendGetReply(Node node, long timeOut) throws ExecutionException, InterruptedException {
            CompletableFuture<Packet> future = node.sendAndGet(getPacket(), timeOut);

            if (future == null)
                return null;

            Packet packet = null;
            try {
                packet = future.get(timeOut, TimeUnit.MILLISECONDS);
            } catch (TimeoutException e) {
                return null;
            }
            ByteBuffer buffer = packet.getBuffer();
            buffer.getLong(); //serial
            int messageCode = Byte.toUnsignedInt(buffer.get());
            int messageType = Byte.toUnsignedInt(buffer.get());
            int responseCode = Byte.toUnsignedInt(buffer.get());

            String string = "";

//            buffer.putLong(serialNumber);
//            buffer.put((byte) code);
//            buffer.put((byte) messageType);
//            buffer.put((byte) responseCode);

            if (messageCode!=MessageCode.RESPONSE.ordinal())
                return new ResponsePacket<String, ResponseCode>(node, ResponseCode.ERR__NOT_A_RESPONSE, string);
            if (responseCode==ResponseCode.SUCCESS.ordinal())
            {
                byte stringBytes[] = new byte[buffer.remaining()];
                buffer.get(stringBytes);
                string = new String(stringBytes);
            }

            return new ResponsePacket<String, ResponseCode>(node, ResponseCode.values()[responseCode], string);
        }
    }



    public static class IntMessage extends Message {
        protected IntMessage(MessageCode code, long serial, Integer integer) {
            super(new GenericSendPacket(code, PacketType.INT32, serial, new byte[] {
                    (byte) ((integer.intValue() >> 24) & 0xFF),
                    (byte) ((integer.intValue() >> 16) & 0xFF),
                    (byte) ((integer.intValue() >> 8) & 0xFF),
                    (byte) ((integer.intValue()) & 0xFF)}));
        }

        @Override
        public ResponsePacket<Integer, ResponseCode> sendGetReply(Node node, long timeOut) throws ExecutionException, InterruptedException {
            CompletableFuture<Packet> future = node.sendAndGet(getPacket(), timeOut);

            if (future == null)
                return null;

            Packet packet = null;
            try {
                packet = future.get(timeOut, TimeUnit.MILLISECONDS);
            } catch (TimeoutException e) {
                return null;
            }
            ByteBuffer buffer = packet.getBuffer();
            buffer.getLong(); //serial
            int messageCode = Byte.toUnsignedInt(buffer.get());
            int messageType = Byte.toUnsignedInt(buffer.get());
            int responseCode = Byte.toUnsignedInt(buffer.get());

            Integer integer = 0;

            if (messageCode!=MessageCode.RESPONSE.ordinal())
                return new ResponsePacket<Integer, ResponseCode>(node, ResponseCode.ERR__NOT_A_RESPONSE, integer);
            if (responseCode==ResponseCode.SUCCESS.ordinal())
            {
                integer = buffer.getInt();
            }

            return new ResponsePacket<Integer, ResponseCode>(node, ResponseCode.values()[responseCode], integer);
        }
    }

    public static class StringResponseMessage extends Message {
        protected StringResponseMessage(MessageCode code, ResponseCode responseCode, long serial, String string) {
            super(new GenericResponsePacket(code, responseCode, PacketType.UTF, serial, string.getBytes()));
        }

        @Override
        public ResponsePacket<String, ResponseCode> sendGetReply(Node node, long timeOut) throws ExecutionException, InterruptedException {
            return null;
        }
    }



    public static class IntResponseMessage extends Message {
        protected IntResponseMessage(MessageCode code, ResponseCode responseCode, long serial, Integer integer) {
            super(new GenericResponsePacket(code, responseCode, PacketType.INT32, serial, new byte[] {
                    (byte) ((integer.intValue() >> 24) & 0xFF),
                    (byte) ((integer.intValue() >> 16) & 0xFF),
                    (byte) ((integer.intValue() >> 8) & 0xFF),
                    (byte) ((integer.intValue()) & 0xFF)}));
        }

        @Override
        public ResponsePacket<Integer, ResponseCode> sendGetReply(Node node, long timeOut) throws ExecutionException, InterruptedException {
            return null;
        }
    }

    public static class Factory extends MessageFactory<PacketType, ResponseCode> {
        @Override
        public <MessageObject> Message makeMessage(PacketType en, MessageObject arg) throws MessageFactoryException {
            switch (en)
            {
                case UTF:
                    return new StringMessage(MessageCode.MESSAGE, generateSerialNumber(), (String) arg);
                case INT32:
                    return new IntMessage(MessageCode.MESSAGE, generateSerialNumber(), (Integer) arg);
                default:
                    throw new MessageFactoryException("unsupported message type '" + en + "'.");
            }
        }

        @Override
        public <MessageObject> Message makeRequest(PacketType en, MessageObject arg) throws MessageFactoryException {
            switch (en)
            {
                case UTF:
                    return new StringMessage(MessageCode.REQUEST, generateSerialNumber(), (String) arg);
                case INT32:
                    return new IntMessage(MessageCode.REQUEST, generateSerialNumber(), (Integer) arg);
                default:
                    throw new MessageFactoryException("unsupported request type '" + en + "'.");
            }
        }

        @Override
        public <MessageObject> Message makeResponse(PacketType en, ResponseCode er, long serialNumber, MessageObject arg) throws MessageFactoryException {
            switch (en)
            {
                case UTF:
                    return new StringResponseMessage(MessageCode.RESPONSE, er, serialNumber, (String) arg);
                case INT32:
                    return new IntResponseMessage(MessageCode.RESPONSE, er, serialNumber, (Integer) arg);
                default:
                    throw new MessageFactoryException("unsupported response type '" + en + "'.");
            }
        }
    }

}
