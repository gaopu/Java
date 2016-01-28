import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.net.Socket;
import java.security.MessageDigest;
import java.util.*;

/**
 * Created by geekgao on 16-1-26.
 */
public class Utils {
    private static String roomSrc;
    private static Properties config = new Properties();

    static {
        try {
            config.load(Utils.class.getResourceAsStream("/config.properties"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String getRoomId() throws IOException {
        if (roomSrc == null) {
            Document roomDoc = Jsoup.connect(getRoomUrl()).get();
            roomSrc = roomDoc.toString();
        }

        return roomSrc.split("room_id\":")[1].split(",")[0];
    }

    public static boolean roomIsAlive() throws IOException {
        Document roomDoc = Jsoup.connect(getRoomUrl()).get();
        roomSrc = roomDoc.toString();

        String status = roomSrc.split("show_status\":")[1].split(",")[0];

        return status.equals("1");
    }

    public static String getRoomName() throws IOException {
        if (roomSrc == null) {
            Document roomDoc = Jsoup.connect(getRoomUrl()).get();
            roomSrc = roomDoc.toString();
        }

        String name =  roomSrc.split("room_name\":\"")[1].split("\",")[0];
        return unicode2String(name);
    }

    public static String getOwnerName() throws IOException {
        if (roomSrc == null) {
            Document roomDoc = Jsoup.connect(getRoomUrl()).get();
            roomSrc = roomDoc.toString();
        }

        String name =  roomSrc.split("owner_name\":\"")[1].split("\",")[0];
        return unicode2String(name);
    }

    public static String getRoomUrl() throws IOException {
        String url = config.getProperty("url");

        if (url.startsWith("http://")) {
            return url;
        } else {
            return "http://" + url;
        }
    }

    /**
     *
     * @return 返回值代表是否开启海量弹幕模式
     */
    public static boolean isSeaMode() {
        return config.getProperty("seaMode").equals("true");
    }

    public static String getServerIP() throws IOException {
        return config.getProperty("serverIP");
    }

    public static String getServerPort() throws IOException {
        return config.getProperty("serverPort");
    }

    public static String md5(String s) {
        char hexDigits[] = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};
        try {
            // 获得MD5摘要算法的 MessageDigest 对象
            MessageDigest mdInst = MessageDigest.getInstance("MD5");
            // 使用指定的字节更新摘要, 获得密文
            byte[] md = mdInst.digest(s.getBytes());
            // 把密文转换成十六进制的字符串形式
            int j = md.length;
            char str[] = new char[j * 2];
            int k = 0;
            for (byte b : md) {
                str[k++] = hexDigits[b >>> 4 & 0xf];
                str[k++] = hexDigits[b & 0xf];
            }
            return new String(str).toLowerCase();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 将包含unicode的字符串 转 中文字符串
     * 将每个unicode编码计算出其值，再强转成char类型，然后将这个字符存储到字符串中
     */
    private static String unicode2String(String str) {
        StringBuilder result = new StringBuilder();
        for (int i = 0;i < str.length();) {
            if (str.charAt(i) == '\\' && str.charAt(i + 1) == 'u') {
                String unicode = str.substring(i + 2, i + 6);
                //确定是unicode编码
                if (unicode.matches("[0-9a-fA-F]{4}")) {
                    //将得到的数值按照16进制解析为十进制整数，再強转为字符
                    char ch = (char) Integer.parseInt(unicode, 16);
                    //用得到的字符替换编码表达式
                    result.append(ch);
                    i += 6;
                } else {
                    result.append("\\u");
                    i += 2;
                }
            } else {
                result.append(str.charAt(i));
                i++;
            }
        }

        return result.toString();
    }

}
