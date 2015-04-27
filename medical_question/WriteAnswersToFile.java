import org.dom4j.Document;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;


public class WriteAnswersToFile extends Thread {

    private String address;//输出文件到哪个地址
    private Document dom;//将这个dom放入文件里存储

    /**
     *
     * @param address 文件将存储到这个地址
     * @param dom asd 即将存储到硬盘的xml文件
     */
    public WriteAnswersToFile(String address,Document dom) {
        this.address = address;
        this.dom = dom;
    }

    public void run() {
        OutputFormat outFormat = OutputFormat.createPrettyPrint();
        outFormat.setEncoding("UTF-8");

        try {
            XMLWriter xml = new XMLWriter(new FileWriter(new File(address)),outFormat);
            xml.write(dom);
            xml.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
