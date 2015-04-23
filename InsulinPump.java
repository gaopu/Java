import java.util.Timer;
import java.util.TimerTask;

class InsulinPump extends Thread{
    private double insulinQuantity;//胰岛素的量
    private double battery;//电池电量
    private double bloodSugar;//血糖值
    private double weight;//根据体重计算注射的胰岛素量

    public InsulinPump (double weight) {
        insulinQuantity = 1000;
        battery = 100;
        bloodSugar = 5;//正常情况:3.9--6.1 mmol/L
        this.weight = weight;
    }

    /**
     * 启动胰岛素泵，开启检测
     */
    public void run() {
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            public void run() {
                battery -= 0.1;
            }
        },1000,1000);//10秒减少1个电
    }

    public double getInsulinQuantity() {
        return insulinQuantity;
    }

    public void setInsulinQuantity(double insulinQuantity) {
        this.insulinQuantity = insulinQuantity;
    }

    public double getBattery() {
        return battery;
    }

    public void setBattery(int battery) {
        this.battery = battery;
    }

    public double getBloodSugar() {
        return bloodSugar;
    }

    public void setBloodSugar(double bloodSugar) {
        this.bloodSugar = bloodSugar;
    }

    //调整胰岛素的量
    public double adjust() {
        double quantity = (bloodSugar * 18 - 100) * weight * 6 / 2000;
        insulinQuantity -= quantity;
        return quantity;
    }
}
