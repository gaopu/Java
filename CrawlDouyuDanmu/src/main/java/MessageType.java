public enum MessageType {
    SEND(689), RECV(690);

    private short code;

    MessageType(int code) {
        this.code = (short) code;
    }

    public short getCode() {
        return code;
    }
}
