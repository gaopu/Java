import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

class People extends Thread {
    private double bloodSugar;  //人体血糖值
    private Random random;      //血糖数随机,3--8之间

    public People() {
        random = new Random(System.currentTimeMillis());
        bloodSugar = 3.9 + random.nextDouble() * 5;
    }

    @Override
    public void run() {
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                bloodSugar = 3.9 + random.nextDouble() * 5;
            }
        },0,1000);//每1秒改变一次血糖值
    }

    public double getBloodSugar() {
        return bloodSugar;
    }
}
