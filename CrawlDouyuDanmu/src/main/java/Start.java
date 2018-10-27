import java.io.IOException;

public class Start {
    public static void main(String[] args) throws IOException, InterruptedException {
        DouyuBarrageServerHandler server = new DouyuBarrageServerHandler("288016");

        while (true) {
            System.out.println(server.read());
            Thread.sleep(1);
        }
    }
}
