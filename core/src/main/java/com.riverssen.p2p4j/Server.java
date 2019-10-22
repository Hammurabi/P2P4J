package com.riverssen.p2p4j;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;

public class Server implements Runnable {
    private Map<NodeID, Node>               nodes;
    private ServerSocket                    socket;
    private Set<Message>                    messages;
    private Set<NodeID>                     dropConnects;
    private Set<NodeID>                     makeConnects;
    private ServerParameters                serverParameters;

    public Server(ServerParameters parameters) throws IOException {
        this.serverParameters = parameters;
        this.nodes = Collections.synchronizedMap(new HashMap<>());
        this.messages = Collections.synchronizedSet(new LinkedHashSet<>());
        this.dropConnects = Collections.synchronizedSet(new LinkedHashSet<>());
        this.makeConnects = Collections.synchronizedSet(new LinkedHashSet<>());
        Server self = this;

        socket        = new ServerSocket(serverParameters.getPort());

        this.serverParameters.getThreadPool().execute(()->{
            while (serverParameters.getKeepAlive())
            {
                if (getNetworkSize() < serverParameters.getMaxConnections()) {
                    try {
                        Socket sock = socket.accept();
                        serverParameters.getConnectionRequestCallback().onEvent(self, sock);
                    } catch (IOException e) {
                    }
                }
            }
        });
    }

    public void dropConnection(NodeID nodeID)
    {
        dropConnects.add(nodeID);
    }

    public void connect(NodeID nodeID)
    {
        makeConnects.add(nodeID);
    }

    public void broadcast(Message message)
    {
        messages.add(message);
    }

    public int getNetworkSize() {
        return nodes.size();
    }

    public void getNodes(List<Node> nodes) {
        synchronized (nodes) {
            nodes.addAll(this.nodes.values());
        }
    }

    @Override
    public void run() {
        while (serverParameters.getKeepAlive()) {
            synchronized (dropConnects) {
                Set<NodeID> remove = new LinkedHashSet<>();
                Iterator<NodeID> nodeIterator = dropConnects.iterator();

                while (nodeIterator.hasNext()) {
                    NodeID nodeID = nodeIterator.next();

                    if (nodes.containsKey(nodeID))
                    {
                        Node node = nodes.get(nodeID);

                        try {
                            node.dropConnection();
                        } catch (IOException e) {
                        }

                        remove.add(nodeID);
                    }

                    nodeIterator.remove();
                }

                for (NodeID nodeID : remove)
                    nodes.remove(nodeID);
            }

            synchronized (makeConnects) {
                Iterator<NodeID> nodeIterator = makeConnects.iterator();

                while (nodeIterator.hasNext()) {

                    NodeID nodeID = nodeIterator.next();
                    if (!nodes.containsKey(nodeID))
                    {
                        try {
                            Socket socketChannel = new Socket(nodeID.getAddress(), nodeID.getPort());

                            Node node = serverParameters.getFactory().makeConnection(nodeID, this, socketChannel);
                            nodes.put(nodeID, node);
                            getParameters().getThreadPool().execute(node);
                        } catch (IOException e) {
                        }
                    }

                    nodeIterator.remove();
                }
            }

            Collection<Node> nodeCollection = null;
            synchronized (nodes) {
                nodeCollection = new LinkedHashSet<>(nodes.values());
            }

            synchronized (messages) {
                Iterator<Message> messageIterator = messages.iterator();
                while (messageIterator.hasNext()) {
                    Message message = messageIterator.next();
                    sendMessage(nodeCollection, message);
                    messageIterator.remove();
                }
            }
        }
    }

    private void sendMessage(Collection<Node> nodeCollection, Message message)
    {
        for (Node node : nodeCollection) message.send(node);
    }

    public ServerParameters getParameters() {
        return serverParameters;
    }

    public Map<NodeID, Node> getNodeMap() {
        return nodes;
    }

    public void connect(Node node) {
        if (!nodes.containsKey(node.getNodeID()))
        {
            nodes.put(node.getNodeID(), node);
            getParameters().getThreadPool().execute(node);
        }
    }
}
