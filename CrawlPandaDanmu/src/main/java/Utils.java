import java.io.IOException;
import java.util.Properties;

/**
 * Created by geekgao on 16-1-29.
 */
public class Utils {
    private static Properties config = new Properties();

    static {
        try {
            config.load(Utils.class.getResourceAsStream("/config.properties"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String getRoomId() {
        return config.getProperty("roomId");
    }

    /**
     * 将包含unicode的字符串 转 中文字符串
     * 将每个unicode编码计算出其值，再强转成char类型，然后将这个字符存储到字符串中
     */
    public static String unicode2String(String str) {
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
