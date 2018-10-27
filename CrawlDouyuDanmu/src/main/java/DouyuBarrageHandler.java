import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class DouyuBarrageHandler {
    private static String host = "openbarrage.douyutv.com";

    private static int port = 8601;

    private Socket serverSocket;

    private String roomId;

    public DouyuBarrageHandler(String roomId) {
        this.roomId = roomId;

        try {
            connect();
            login();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void connect() throws IOException {
        serverSocket = new Socket(host, port);

        new Thread(new Runnable() {
            public void run() {
                while (true) {
                    try {
                        send("type@=mrkl");
                        Thread.sleep(30000);
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }

    private void login() throws IOException {
        send("type@=loginreq/roomid@=" + roomId);
        send("type@=joingroup/rid@=" + roomId + "/gid@=-9999");
    }

    public String read() throws IOException {
        int msgSize = ByteBuffer.wrap(getBytes(4)).order(ByteOrder.LITTLE_ENDIAN).getInt();
        byte[] msgBytes =getBytes(msgSize);

        return new String(msgBytes, 8, msgSize - 9);
    }

    public void send(String msg) throws IOException {
        serverSocket.getOutputStream().write(getSendBytes(msg));
        serverSocket.getOutputStream().flush();
    }

    private byte[] getBytes(int byteCount) throws IOException {
        byte[] result = new byte[byteCount];
        int alreadyReadSize = 0;

        while (alreadyReadSize != byteCount) {
            alreadyReadSize += serverSocket.getInputStream().read(result, alreadyReadSize, byteCount - alreadyReadSize);
        }

        return result;
    }

    private byte[] getSendBytes(String msg) throws IOException {
        ByteArrayOutputStream outBytes = new ByteArrayOutputStream(getPacketSize(msg));
        outBytes.write(intToggle(getPacketSize(msg)));
        outBytes.write(intToggle(getPacketSize(msg)));
        outBytes.write(shortToggle(MessageType.SEND.getCode()));
        outBytes.write(0);
        outBytes.write(0);
        outBytes.write(msg.getBytes());
        outBytes.write(0);

        return outBytes.toByteArray();
    }

    private int getPacketSize(String msg) {
        return 9 + msg.length();
    }

    private byte[] intToggle(int value) {
        byte[] result = new byte[4];
        result[3] = (byte) ((value >> 24) & 0xFF);
        result[2] = (byte) ((value >> 16) & 0xFF);
        result[1] = (byte) ((value >> 8) & 0xFF);
        result[0] = (byte) (value & 0xFF);

        return result;
    }

    private byte[] shortToggle(short value) {
        byte[] result = new byte[2];
        result[1] = (byte) ((value >> 8) & 0xFF);
        result[0] = (byte) (value & 0xFF);

        return result;
    }
}
