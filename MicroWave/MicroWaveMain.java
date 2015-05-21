import java.util.Timer;
import java.util.TimerTask;

public class MicroWaveMain {
    public static void main(String[] args) {
        final MicroWave microWave = new MicroWave();
        final MicroWaveWindow microWaveWindow = new MicroWaveWindow(microWave);

        microWave.run();
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                microWaveWindow.setTimeLable(microWave.getHour(), microWave.getMinutes(),microWave.getSecond());
                microWaveWindow.pack();//动态调整窗口大小
            }
        },0,50);
    }
}