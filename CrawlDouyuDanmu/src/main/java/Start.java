import java.io.IOException;

public class Start {
    public static void main(String[] args) throws IOException, InterruptedException {
        DouyuBarrageHandler server = new DouyuBarrageHandler("288016");

        int lineCount = 0;
        while (true) {
            System.out.println(server.read());
            lineCount++;
            Thread.sleep(1);
        }
    }
}
