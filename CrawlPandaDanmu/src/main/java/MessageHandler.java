import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by geekgao on 16-1-27.
 */
public class MessageHandler {
    private Socket socket;
    private InputStream inputStream;

    public MessageHandler(Socket socket) {
        this.socket = socket;
    }

    /**
     *
     * @return 返回人可阅读的json串
     * @throws IOException
     */
    public List<String> read() throws IOException {
        if (inputStream == null) {
            inputStream = socket.getInputStream();
        }
        byte[] typeBytes = new byte[4];

        //读取前4个字节，得到数据类型信息
        for (int i = 0;i < 4;i++) {
            int tmp = inputStream.read();
            typeBytes[i] = (byte) tmp;
        }

        //最终的结果
        List<String> result = new LinkedList<String>();
        //这是一条弹幕信息
        if (typeBytes[0] == 0x00 && typeBytes[1] == 0x06 && typeBytes[2] == 0x00 && typeBytes[3] == 0x03) {
            //越过前面没用的字节，跳到标记内容长度的字节
            inputStream.skip(7);
            //下条内容的长度
            int contentLen = 0;
            //读取4个字节，得到数据长度
            for (int i = 3;i >= 0;i--) {
                int tmp = inputStream.read();
                contentLen += tmp * Math.pow(16,2 * i);
            }

            int len;
            int readLen = 0;
            byte[] bytes = new byte[contentLen];
            ByteArrayOutputStream byteArray = new ByteArrayOutputStream();
            while ((len = inputStream.read(bytes,0,contentLen - readLen)) != -1) {
                byteArray.write(bytes,0,len);
                readLen += len;
                if (readLen == contentLen) {
                    break;
                }
            }

            bytes = byteArray.toByteArray();
            byte[] b = Arrays.copyOfRange(bytes, 8, 12);
            //找到人可识别的字符串放入结果集中
            for (int i = 0;i < bytes.length;) {
                //一段弹幕内容的开头
                if (bytes[i] == b[0] && bytes[i+1] == b[1] && bytes[i+2] == b[2] && bytes[i+3] == b[3]) {
                    i += 4;
                    //一段弹幕json字符串的长度
                    int length = 0;
                    //读取4个字节，得到弹幕数据长度
                    for (int j = 0,k = 3;j < 4;j++,k--) {
                        int n = bytes[i + j];
                        /*
                          原数据一个字节可保存0~255的数,但是byte范围是-128~127,所以要变回原来的真实数据
                          后面的数据不变是因为后面的字符串都是ascii字符,都在0~127之内
                         */
                        if (n < 0) {
                            n = 256 + bytes[i + j];
                        }

                        length += n * Math.pow(16,2 * k);
                    }
                    i += 4;

                    result.add(Utils.unicode2String(new String(Arrays.copyOfRange(bytes,i,i + length))));
                    i += length;
                } else {
                    i++;
                }
            }

        } else if ((typeBytes[0] == 0x00 && typeBytes[1] == 0x06 && typeBytes[2] == 0x00 && typeBytes[3] == 0x06)) {
            //下条内容的长度
            int contentLen = 0;
            //读取2个字节，得到数据长度
            for (int i = 1;i >= 0;i--) {
                int tmp = inputStream.read();
                contentLen += tmp * Math.pow(16,2 * i);
            }

            inputStream.skip(contentLen);
        }

        return result;
    }

    public void close() throws IOException {
        socket.close();
    }
}
