import org.apache.http.HttpHeaders;
import org.apache.http.HttpHost;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Main {

    CloseableHttpClient httpClient;
    static int bookId = 496;
    Map<String,Integer> proxyMap;//ip->端口
    List<String> ipList;//从这个list中读出ip，再由ip从map中读出端口
    int i = 0;//根据这个从list中取出ip，换上对应的代理

    public static void main(String[] args) {
        Main m = new Main();

//        List<String> tagList = m.getTagList();
        List<String> tagList = new LinkedList<String>();
//        tagList.add("经典");
//        tagList.add("日本文学");
//        tagList.add("散文");
//        tagList.add("中国文学");
//        tagList.add("算法");
//        tagList.add("童话");
//        tagList.add("外国文学");
//        tagList.add("文学");
//        tagList.add("小说");
//        tagList.add("漫画");
//        tagList.add("诗词");
//        tagList.add("心理学");
        tagList.add("摄影");
        tagList.add("理财");
        tagList.add("经济学");
        m.pullAndWrite(tagList,10);
    }

    public Main() {
//        HttpHost proxy = new HttpHost("122.225.106.35",80);
//        httpClient = HttpClients.custom().setProxy(proxy).build();
        httpClient = HttpClients.createDefault();
        setProxyMap();
    }

    public void setProxyMap() {
        proxyMap = new HashMap<String, Integer>();
        ipList = new LinkedList<String>();
        proxyMap.put("211.68.122.171",80);ipList.add("211.68.122.171");
    }

    public List<String> getTagList() {
        HttpGet getTag = new HttpGet("http://book.douban.com/tag/");
        getTag.addHeader(HttpHeaders.USER_AGENT, "Mozilla/5.0 (Windows NT 5.2) AppleWebKit/534.30 (KHTML, like Gecko) Chrome/12.0.742.122 Safari/534.30");
        CloseableHttpResponse tagPageResponse = null;
        String tagPageCode = null;//网页源码
        try {
            tagPageResponse = httpClient.execute(getTag);
            tagPageCode = EntityUtils.toString(tagPageResponse.getEntity());
            tagPageResponse.close();
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
        Pattern bookAddressRegex = Pattern.compile("href=\"(.*?)\" class=\"title\" target=\"_blank\">(.*?)</a>");   //获取具体书籍网址的正则
        Pattern bookAuthorRegex = Pattern.compile("(?s)<span class=\"pl\"> 作者</span>:.*?>(.*?)</a>");//匹配作者
        Pattern bookPublishRegex = Pattern.compile("<span class=\"pl\">出版社:</span> (.*?)<br/>");
        Pattern bookIsbnRegex = Pattern.compile("<span class=\"pl\">ISBN:</span> (.*?)<br/>");
        Pattern bookImgRegex = Pattern.compile("<img src=\"(.*?)\" title=\"点击看大图\"");

        //分别抓取每一种类别的书籍
        for (String tag:tagList) {
            int nowPageNum = 0;//目前正在抓取的页数
            Document newDocument = DocumentHelper.createDocument();
            Element rootElement = newDocument.addElement("root");

            while (nowPageNum < maxPageNum) {
                System.out.println(1);
                String nowPageAddress = "http://www.douban.com/tag/" + tag + "/book?start=" + nowPageNum * 15;//当前页的网址
                HttpGet getBooksPage = new HttpGet(nowPageAddress);
                getBooksPage.addHeader(HttpHeaders.USER_AGENT, "Mozilla/5.0 (Windows NT 5.2) AppleWebKit/534.30 (KHTML, like Gecko) Chrome/12.0.742.122 Safari/534.30");
                CloseableHttpResponse booksPageResponse;
                Matcher m = null;
                try {
                    System.out.println(2);
                    booksPageResponse = httpClient.execute(getBooksPage);
                    System.out.println(3);
                    m = bookAddressRegex.matcher(EntityUtils.toString(booksPageResponse.getEntity()));
                    booksPageResponse.close();
                    if (booksPageResponse.getStatusLine().getStatusCode() != 200) {
                        System.out.println("抓 " + nowPageAddress + " 时出错:");
                        System.out.println("错误信息:" + booksPageResponse.getStatusLine());
                        changeProxy();
                        continue;//换个代理继续爬当前页
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                //具体每一本书，具体抓取
                int findCount = 0;//找到的书籍的数目
                List<Thread> threadList = new LinkedList<Thread>();
                while (m.find()) {
                    threadList.add(new GetBookInfoThread(httpClient, m.group(1), m.group(2), rootElement, bookAuthorRegex, bookPublishRegex, bookIsbnRegex,bookImgRegex));
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
            //一个类别爬完了再写入
            new WriteBookInfoToFile(rootElement,"/home/geekgao/book/" + tag + ".xml").start();  //另开一个线程写入文件

        }
    }

    private void changeProxy() {
        if (i >= ipList.size()) {
            System.out.println("代理用完了,退出");
            System.exit(0);
        }
        String ip = ipList.get(i++);
        httpClient = HttpClients.custom().setProxy(new HttpHost(ip,proxyMap.get(ip))).build();
        System.out.println("换代理啦,使用代理:" + ip + "，端口:" + proxyMap.get(ip));
    }

}
