import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Main {

    CloseableHttpClient httpClient;
    static int bookId = 1;

    public static void main(String[] args) {
        Main m = new Main();

//        List<String> tagList = m.getTagList();
        List<String> tagList = new LinkedList<String>();
        tagList.add("算法");
        m.pullAndWrite(tagList,1);
    }

    public Main() {
        httpClient = HttpClients.createDefault();
    }

    public List<String> getTagList() {
        HttpGet getTag = new HttpGet("http://book.douban.com/tag/");
        CloseableHttpResponse tagPageResponse = null;
        String tagPageCode = null;//网页源码
        try {
            tagPageResponse = httpClient.execute(getTag);
            tagPageCode = EntityUtils.toString(tagPageResponse.getEntity());
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                tagPageResponse.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        Pattern p = Pattern.compile("class=\"tag\">(.*?)</a>");
        Matcher m = p.matcher(tagPageCode);
        List<String> resultTagList = new LinkedList<String>();
        while (m.find()) {
            resultTagList.add(m.group(1));
        }

        return resultTagList;
    }

    /**
     *
     * @param tagList  要抓的图书的类别
     * @param maxPageNum 每种图书最多抓取的页数
     */
    public void pullAndWrite(List<String> tagList,int maxPageNum) {
        //分别抓取每一种类别的书籍
        Pattern bookAddressRegex = Pattern.compile("href=\"(.*?)\" class=\"title\" target=\"_blank\">(.*?)</a>");   //获取具体书籍网址的正则
        Pattern bookAuthorRegex = Pattern.compile("(?s)<span class=\"pl\"> 作者</span>:.*?>(.*?)</a>");//匹配作者
        Pattern bookPublishRegex = Pattern.compile("<span class=\"pl\">出版社:</span> (.*?)<br/>");
        Pattern bookIsbnRegex = Pattern.compile("<span class=\"pl\">ISBN:</span> (.*?)<br/>");

        for (String tag:tagList) {
            int nowPageNum = 0;//目前正在抓取的页数
            Document newDocument = DocumentHelper.createDocument();
            Element rootElement = newDocument.addElement("root");
            while (nowPageNum < maxPageNum) {
                String nowPageAddress = "http://www.douban.com/tag/" + tag + "/book?start=" + nowPageNum * 15;//当前页的网址
                HttpGet getBooksPage = new HttpGet(nowPageAddress);
                CloseableHttpResponse booksPageResponse;
                Matcher m = null;
                try {
                    booksPageResponse = httpClient.execute(getBooksPage);
                    m = bookAddressRegex.matcher(EntityUtils.toString(booksPageResponse.getEntity()));
                } catch (IOException e) {
                    e.printStackTrace();
                }
                //具体每一本书，具体抓取
                int findCount = 0;//找到的书籍的数目
                List<Thread> threadList = new LinkedList<Thread>();
                while (m.find()) {
                    threadList.add(new GetBookInfoThread(httpClient, m.group(1), m.group(2), rootElement, bookAuthorRegex, bookPublishRegex, bookIsbnRegex));
                    findCount++;
                }
                //没有知道到代表这种类别的书都找完了，那么直接退出此类书籍的查找
                if (findCount == 0) {
                    break;
                }

                for (Thread thread:threadList) {
                    thread.start();
                }
                for (Thread thread:threadList) {
                    try {
                        thread.join();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                nowPageNum++;
            }

            new WriteBookInfoToFile(rootElement,"/home/geekgao/book/" + tag + ".xml").start();  //另开一个线程写入文件

        }
    }
}
