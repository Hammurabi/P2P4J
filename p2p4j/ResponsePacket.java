public class ResponsePacket<T, EnumR extends Enum<EnumR>> {
    private Node    issuer;
    private EnumR   code;
    private T       reply;

    public ResponsePacket(Node issuer, EnumR code, T reply)
    {
        this.issuer = issuer;
        this.code = code;
        this.reply = reply;
    }

    public Node getIssuer() {
        return issuer;
    }

    public EnumR getCode() {
        return code;
    }

    public T getReply() {
        return reply;
    }

    @Override
    public String toString() {
        return code + " " + reply;
    }
}
