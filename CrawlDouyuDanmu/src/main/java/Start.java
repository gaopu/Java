import java.io.IOException;

public class Start {
    public static void main(String[] args) throws IOException, InterruptedException {
        DouyuBarrageHandler server = new DouyuBarrageHandler("288016");

        while (true) {
            System.out.println(server.read());
            Thread.sleep(1);
        }
    }
}
