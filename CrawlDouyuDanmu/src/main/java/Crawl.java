import java.io.IOException;
import java.net.Socket;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by geekgao on 16-1-28.
 */
public class Crawl extends Thread {
    //弹幕服务器端口
    List<Integer> ports = new LinkedList<Integer>();
    //房间id
    String rid;
    //弹幕分组
    String gid;
    //与弹幕服务器交互的控制器
    MessageHandler messageHandler;

    public Crawl() throws IOException {
        rid = Utils.getRoomId();
    }

    /**
     *
     * 初始化弹幕服务器地址数据和弹幕分组信息
     */
    public void init() throws IOException {
        String ip = Utils.getServerIP();
        int port = Integer.valueOf(Utils.getServerPort());
        Socket socket = new Socket(ip,port);
        System.out.println("从服务器(" + ip + ":" + port + ")获取弹幕服务器数据");
        MessageHandler messageHandler = new MessageHandler(socket);

        String s = "type@=loginreq/username@=/ct@=0/password@=/roomid@=" + Utils.getRoomId() + "/";
        String time = String.valueOf(System.currentTimeMillis() / 1000);
        String uuid = UUID.randomUUID().toString().replaceAll("-","").toUpperCase();
        String rt = "rt@=" + time + "/";
        String devid = "devid@=" + uuid + "/";
        String vk = "vk@=" + Utils.md5(time + "7oE9nPEG9xXV69phU31FYCLUagKeYtsF" + uuid) + "/";
        String ver = "ver@=20150929/";
        String content = s + devid + rt + vk + ver;

        messageHandler.send(content);
        for (int i = 0;i < 3;i++) {
            byte[] bytes = messageHandler.read();
            String msg = new String(Arrays.copyOfRange(bytes, 8, bytes.length));
            if (msg.startsWith("type@=msgrepeaterlist")) {
                Pattern p = Pattern.compile("@ASip(.*?)@AS@S");
                Matcher m = p.matcher(msg);

                while (m.find()) {
                    String str = m.group(1);
                    Integer po = Integer.valueOf(str.split("@ASport@AA=")[1]);
                    ports.add(po);
                }
            } else if (msg.startsWith("type@=setmsggroup")) {
                gid = msg.split("gid@=")[1].split("/")[0];
            }
        }
        socket.close();
    }

    public void login() throws IOException {
        Socket socket = new Socket("danmu.douyutv.com",ports.get(0));
        System.out.println("连接弹幕服务器(danmu.douyutv.com:" + ports.get(0) + ")");
        messageHandler = new MessageHandler(socket);

        String loginreq = "type@=loginreq/username@=visitor503535/password@=1234567890123456/roomid@=" + rid + "/";
        messageHandler.send(loginreq);
        String joinGroup = "type@=joingroup/rid@=" + rid + "/gid@=" + gid + "/";
        messageHandler.send(joinGroup);
        System.out.println("进入" + gid + "号弹幕分组");
    }

    @Override
    public void run() {
        try {
            System.out.println("房间名:" + Utils.getRoomName());
            System.out.println("主播:" + Utils.getOwnerName());
            if (!Utils.roomIsAlive()) {
                System.out.println("房间未开播，程序结束.");
                return;
            } else {
                System.out.println("状态:正在直播");
            }

            init();
            login();

            long start = System.currentTimeMillis();
            while (true) {
                byte[] bytes = messageHandler.read();
                String msg = new String(Arrays.copyOfRange(bytes,8,bytes.length));

                if (msg.startsWith("type@=chatmessage")) {
                    String nickname = msg.split("@Snick@A=")[1].split("@Srg@A")[0];
                    String content = msg.split("content@=")[1].split("/snick@=")[0];
                    System.out.println("[" + nickname + "]:" + content);
//                    System.out.println(msg);
                }

                long end = System.currentTimeMillis();
                if (end - start > 30000) {
                    messageHandler.send("type=mrkl/");
                }

                Thread.sleep(1);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            try {
                messageHandler.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
