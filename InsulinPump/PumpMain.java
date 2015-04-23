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
import java.util.Timer;
import java.util.TimerTask;

public class PumpMain {
    public static void main(String[] args) {

        final Logger log = Logger.getLogger(PumpMain.class);
        try {
            log.addAppender(new FileAppender(new PatternLayout("[%d{yyyy/MM/dd-HH:mm:ss}]-%m%n"), "/home/geekgao/insulinPumpLog", true));
        } catch (IOException e) {
            e.printStackTrace();
        }

        final People people = new People();

        final JFrame jFrame = new JFrame("输入体重");
        final double[] weight = new double[1];
        jFrame.setLayout(new FlowLayout());
        jFrame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                weight[0] = 60;//关闭窗口的话，默认60公斤
                super.windowClosing(e);
            }
        });
        final JTextField textArea = new JTextField("输入您的体重(默认60公斤)");
        jFrame.add(textArea);
        JButton jButton = new JButton("确定");
        jFrame.add(jButton);

        jButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    weight[0] = Double.valueOf(textArea.getText());
                    jFrame.setVisible(false);
                } catch (NumberFormatException e1) {
                    JOptionPane.showMessageDialog(null, "数字格式不对");
                }
            }
        });
        jFrame.pack();
        jFrame.setLocationRelativeTo(null);
        jFrame.setResizable(false);
        jFrame.setVisible(true);

        while (weight[0] == 0 && jFrame.isVisible() == true) {
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        log.info("开机，用户体重为" + weight[0] + "Kg");

        final InsulinPump insulinPump = new InsulinPump(weight[0]);
        final PumpWindow pumpWindow = new PumpWindow(insulinPump);

        people.start();         //人体运行
        insulinPump.start();    //胰岛素泵运行

        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                insulinPump.setBloodSugar(people.getBloodSugar());

                pumpWindow.updateTime();
                pumpWindow.setBattery(insulinPump.getBattery());
                pumpWindow.setBloodSugar(people.getBloodSugar());
                pumpWindow.setinsulinQuantity(insulinPump.getInsulinQuantity());
                pumpWindow.setStatus("无");

                if (insulinPump.getBloodSugar() <= 3.9 || insulinPump.getBloodSugar() >= 6.1) {
                    double insertQuantity = insulinPump.adjust();
                    String insertQuantityStr = String.valueOf(insertQuantity);
                    pumpWindow.setStatus("正在注射" + insertQuantityStr.substring(0, insertQuantityStr.indexOf(".") + 2) + "个单位的胰岛素");
                    log.info("注射" + insertQuantityStr.substring(0, insertQuantityStr.indexOf(".") + 2) + "个单位的胰岛素");
                    pumpWindow.setinsulinQuantity(insulinPump.getInsulinQuantity());
                }

                if (insulinPump.getBattery() <= 0) {
                    pumpWindow.setBattery(0);
                    log.info("因未及时充电，本仪器将自动关机。");
                    JOptionPane.showMessageDialog(null, "因未及时充电，本仪器将自动关机。");
                    System.exit(0);
                }

                if (insulinPump.getInsulinQuantity() <= 0) {
                    pumpWindow.setinsulinQuantity(0);
                    log.info("胰岛素量不足!!!本仪器将自动关机");
                    JOptionPane.showMessageDialog(null, "胰岛素量不足!!!本仪器将自动关机");
                    System.exit(0);
                }
            }
        }, 0, 1000);              //每秒更新一次显示的数据
    }
}
