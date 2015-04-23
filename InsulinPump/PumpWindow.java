import org.apache.log4j.FileAppender;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

class PumpWindow extends JFrame{
    private JLabel time;
    private JLabel battery;
    private JLabel bloodSugar;
    private JLabel insulinQuantity;
    private JLabel status;

    private JButton charge;
    private JButton insertInsulin;

    private InsulinPump pump;

    private SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy/MM/dd-HH:mm:ss");
    private Logger log;

    public void setBloodSugar(double  bloodSugar) {
        String bloodSugarStr = String.valueOf(bloodSugar);
        this.bloodSugar.setText("血糖值:" + bloodSugarStr.substring(0, bloodSugarStr.indexOf(".") + 2) + "mmol/L");
    }

    public void setinsulinQuantity(double insulinQuantity) {
        String insulinQuantityStr = String.valueOf(insulinQuantity);
        this.insulinQuantity.setText("胰岛素量:" + insulinQuantityStr.substring(0, insulinQuantityStr.indexOf(".") + 2) + "单位");
    }

    public void setStatus(String status) {
        this.status.setText("当前状态:" + status);
    }

    public void setBattery(double battery) {
        String batteryStr = String.valueOf(battery);
        this.battery.setText("电量:" + batteryStr.substring(0, batteryStr.indexOf(".") + 2));//只获取小数点后1位
    }

    public void updateTime() {
        this.time.setText("时间:" + simpleDateFormat.format(new Date()));
    }

    //参数是这个窗口显示的泵子的引用
    public PumpWindow(final InsulinPump pump) {
        log = Logger.getLogger(PumpWindow.class);
        try {
            log.addAppender(new FileAppender(new PatternLayout("[%d{yyyy/MM/dd-HH:mm:ss}]-%m%n"), "/home/geekgao/insulinPumpLog", true));
        } catch (IOException e) {
            e.printStackTrace();
        }
        this.pump = pump;

        this.setLayout(new BorderLayout());
        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                log.info("用户关机");
                System.exit(0);
            }
        });

        JPanel upPanel = new JPanel();
        upPanel.setLayout(new BorderLayout());
        this.add(upPanel, BorderLayout.NORTH);
        time = new JLabel();
        battery = new JLabel();
        upPanel.add(time, BorderLayout.WEST);
        upPanel.add(battery, BorderLayout.EAST);


        JPanel downPanel = new JPanel();
        this.add(downPanel, BorderLayout.SOUTH);
        charge = new JButton("充电");
        charge.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                log.info("充电完毕");
                pump.setBattery(100);
            }
        });
        downPanel.add(charge);
        insertInsulin = new JButton("加满胰岛素");
        insertInsulin.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                log.info("加满胰岛素");
                pump.setInsulinQuantity(1000);
            }
        });
        downPanel.add(insertInsulin);

        JPanel midPanel = new JPanel();
        midPanel.setLayout(new GridLayout(3, 1, 0, 0));
        this.add(midPanel, BorderLayout.CENTER);
        bloodSugar = new JLabel();
        midPanel.add(bloodSugar);
        insulinQuantity = new JLabel();
        midPanel.add(insulinQuantity);
        status = new JLabel();
        midPanel.add(status);

        this.setSize(400, 250);
        this.setResizable(false);
        this.setLocationRelativeTo(null);
        this.setVisible(true);
    }
}
