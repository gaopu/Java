import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

/**
 * Created by geekgao on 16-1-27.
 */
public class MessageHandler {
    private Socket socket;

    public MessageHandler(Socket socket) {
        this.socket = socket;
    }

    public void send(String content) throws IOException {
        Message message = new Message(content);
        OutputStream out = socket.getOutputStream();
        out.write(message.getBytes());
    }

    public byte[] read() throws IOException {
        InputStream inputStream = socket.getInputStream();
        //下条信息的长度
        int contentLen = 0;

        //读取前4个字节，得到数据长度
        for (int i = 0;i < 4;i++) {
            int tmp = inputStream.read();
            contentLen += tmp * Math.pow(16,2 * i);
        }

        int len = 0;
        int readLen = 0;
        byte[] bytes = new byte[contentLen];
        ByteArrayOutputStream byteArray = new ByteArrayOutputStream();
        while ((len = inputStream.read(bytes,0,contentLen - readLen)) != -1) {
            byteArray.write(bytes);
            readLen += len;
            if (readLen == contentLen) {
                break;
            }
        }

        return byteArray.toByteArray();
    }

    public void close() throws IOException {
        socket.close();
    }
}
