import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Arrays;
import java.util.List;

/**
 * Created by geekgao on 16-1-29.
 */
public class Crawl extends Thread {

    //获取弹幕需要发送的内容
    private String rid;
    private String appid;
    private String k = "1";
    private String t = "300";
    private String ts;
    private String sign;
    private String authType;

    //与弹幕服务器联系的socket
    private Socket socket;
    //弹幕服务器ip
    private String serverIp;
    //弹幕服务器端口
    private int port;

    /**
     *
     * @return 返回结果表示是否初始化成功
     * @throws IOException
     */
    public boolean init() throws IOException {
        String roomId = Utils.getRoomId();
        String time = String.valueOf(System.currentTimeMillis());

        String url1 = "http://www.panda.tv/ajax_chatroom?roomid=" + roomId + "&_=" + time;
        Document doc1 = Jsoup.connect(url1).header("User-Agent","Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/46.0.2490.86 Safari/537.36").ignoreContentType(true).get();
        JSONObject jsonObject1 = new JSONObject(doc1.toString().split("<body>",2)[1].split("</body>",2)[0]);

        String _sign;
        String _roomid;
        String _rid;
        String _ts;

        int errno = jsonObject1.getInt("errno");
        if (errno != 0) {
            System.out.println("-----------------------");
            System.out.println("第一步获取数据出错,程序将退出");
            System.out.println("url:" + url1);
            System.out.println("json数据:");
            System.out.println(jsonObject1);
            System.out.println("-----------------------");
            return false;
        } else {
            JSONObject j = jsonObject1.getJSONObject("data");
            _sign = j.getString("sign");
            _roomid = String.valueOf(j.getLong("roomid"));
            _rid = String.valueOf(j.getLong("rid"));
            _ts = String.valueOf(j.getLong("ts"));
        }

        String url2 = "http://api.homer.panda.tv/chatroom/getinfo?rid=" + _rid + "&roomid=" + _roomid + "&retry=0&sign=" + _sign + "&ts=" + _ts + "&_=" + System.currentTimeMillis();
        Document doc2 = Jsoup.connect(url2).header("User-Agent","Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/46.0.2490.86 Safari/537.36").ignoreContentType(true).get();
        JSONObject jsonObject2 = new JSONObject(doc2.toString().split("<body>",2)[1].split("</body>",2)[0]);

        errno = jsonObject2.getInt("errno");
        if (errno != 0) {
            System.out.println("-----------------------");
            System.out.println("第二步获取数据出错,程序将退出");
            System.out.println("url:" + url2);
            System.out.println("json数据:");
            System.out.println(jsonObject2);
            System.out.println("-----------------------");
            return false;
        } else {
            JSONObject j = jsonObject2.getJSONObject("data");
            rid = String.valueOf(j.getLong("rid"));
            appid = j.getString("appid");
            ts = String.valueOf(j.getLong("ts"));
            sign = j.getString("sign");
            authType = j.getString("authType");

            JSONArray chat_addr_list = j.getJSONArray("chat_addr_list");
            for (Object o:chat_addr_list) {
                serverIp = ((String) o).split(":",2)[0];
                port = Integer.valueOf(((String) o).split(":", 2)[1]);
                break;
            }
        }

        return true;
    }

    /**
     * 与弹幕服务器取得联系,相当于登录弹幕服务器
     */
    public void login() throws IOException {
        socket = new Socket(serverIp,port);
        System.out.println("连接弹幕服务器:" + serverIp + ":" + port);
        ByteArrayOutputStream byteArray = new ByteArrayOutputStream();
        byte[] b = new byte[]{0x00, 0x06, 0x00, 0x02, 0x00, 0x60, 0x75, 0x3a};
        byteArray.write(b);

        String msg = rid + "@" + appid + "\n" +
                "k:" + k + "\n" +
                "t:" + t + "\n" +
                "ts:" + ts + "\n" +
                "sign:" + sign + "\n" +
                "authtype:" + authType;
        byteArray.write(msg.getBytes("ISO-8859-1"));
        OutputStream outputStream = socket.getOutputStream();
        outputStream.write(byteArray.toByteArray());

        b = new byte[]{0x00, 0x06, 0x00, 0x00};
        outputStream.write(b);
    }

    @Override
    public void run() {
        MessageHandler messageHandler;
        OutputStream outputStream;

        try {
            init();
            login();

            messageHandler = new MessageHandler(socket);
            outputStream = socket.getOutputStream();

            long start = System.currentTimeMillis();
            while (true) {
                List<String> msgs = messageHandler.read();
                for (String s:msgs) {
                    String type = s.split("\\{\"type\":\"",2)[1].split("\"",2)[0];
                    //发言弹幕
                    if (type.equals("1")) {
                        String nickname = s.split("nickName\":\"",2)[1].split("\"")[0];
                        String content = s.split("content\":\"",2)[1].split("\"",2)[0];
                        System.out.println("[" + nickname + "]:" + content);
                    }
                }

                long end = System.currentTimeMillis();
                //心跳包
                if (end - start > 60000) {
                    outputStream.write(new byte[]{0x00, 0x06, 0x00, 0x00});
                }

                Thread.sleep(1);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
