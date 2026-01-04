import java.util.Timer;
import java.util.TimerTask;

public class MicroWave extends Thread{
    private int hour;//小时数
    private int minutes;//分钟数
    private int second;//秒数
    private boolean isRun;//是否正在运行
    private boolean isPaused;//是否暂停

    public MicroWave() {
        hour = 0;
        minutes = 0;
        second = 0;
        isRun = false;
        isPaused = false;
    }

    @Override
    public void run() {
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                //如果正在运行且未暂停就改变时间
                if (isRun && !isPaused) {
                    if (second >= 1) {
                        second--;
                    } else {
                        second = 59;
                        if (minutes >= 1) {
                            minutes--;
                        } else {
                            minutes = 59;
                            if (hour >= 1) {
                                hour--;
                            } else {
                                hour = 0;
                                minutes = 0;
                                second = 0;
                                isRun = false;
                            }
                        }
                    }
                }
            }
        },0,1000);
    }

    // 新增：切换暂停/恢复状态
    public void togglePause() {
        isPaused = !isPaused;
    }

    // 新增：设置暂停状态
    public void setPaused(boolean paused) {
        this.isPaused = paused;
    }

    // 新增：获取暂停状态
    public boolean isPaused() {
        return isPaused;
    }

    public int getHour() {
        return hour;
    }

    public void addHour(int hour) {
        this.hour += hour;
    }

    public int getMinutes() {
        return minutes;
    }

    public void addMinutes(int minutes) {
        this.minutes += minutes;

        if (this.minutes == 60) {
            this.minutes = 0;
            this.hour++;
        }
    }

    public int getSecond() {
        return second;
    }

    public void setHour(int hour) {
        this.hour = hour;
    }

    public void setMinutes(int minutes) {
        this.minutes = minutes;
    }

    public void setSecond(int second) {
        this.second = second;
    }

    public void addSecond(int second) {

        this.second += second;

        if (this.second == 60) {
            this.second = 0;
            minutes++;
            if (minutes == 60) {
                minutes = 0;
                hour++;
            }
        }
    }

    public void setIsRun(boolean isRun) {
        this.isRun = isRun;
    }

    public boolean isRun() {
        return isRun;
    }
}
