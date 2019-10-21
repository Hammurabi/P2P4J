import java.net.Socket;

public interface ConnectionRequestCallback {
    void onEvent(Server server, Socket socket);
}
