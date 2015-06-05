import org.dom4j.Element;
import org.dom4j.io.XMLWriter;

import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;

public class WriteBookInfoToFile extends Thread {
    private Element root;
    private String fileAddress;

    public WriteBookInfoToFile(Element root,String fileAddress) {
        this.root = root;
        this.fileAddress = fileAddress;
    }

    @Override
    public void run() {
        Writer fileWriter;
        try {
            fileWriter = new FileWriter(fileAddress);
            XMLWriter xmlWriter = new XMLWriter(fileWriter);
            xmlWriter.write(root);
            xmlWriter.close();
            System.out.println("[" + fileAddress + "]写入成功");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
