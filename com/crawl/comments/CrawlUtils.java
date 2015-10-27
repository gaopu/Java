package com.crawl.comments;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.dom4j.Element;
import org.dom4j.io.XMLWriter;
import org.json.JSONObject;

import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by geekgao on 15-10-25.
 */
public class CrawlUtils {
    /**
     *
     * @param id appid
     * @return app名字
     */
    public static String getAppName(String id) throws IOException {
        CloseableHttpClient client = HttpClients.createDefault();

        HttpGet get = new HttpGet("http://zhushou.360.cn/detail/index/soft_id/" + id);
        CloseableHttpResponse response;
        try {
            response = client.execute(get);
        } catch (java.net.UnknownHostException e) {
            return null;
        }
        return EntityUtils.toString(response.getEntity()).split("<title>")[1].split("<")[0];
    }

    /**
     *
     * @param xml xml文档
     * @param fileName 存储到这个地方
     */
    public static void writeXmlToFile(Element xml,String fileName) throws IOException {
        Writer fileWriter = new FileWriter(fileName);
        XMLWriter xmlWriter = new XMLWriter(fileWriter);
        xmlWriter.write(xml);
        xmlWriter.close();
    }

    /**
     * 获取需要下载的app的id
     * @param uri app类别页
     * @param limit 获取前limit个app的评论
     * @return
     */
    public static Set<String> getAppIds(String uri,int limit) throws IOException {
        /*//因为根据网页源码每个appid会匹配到两次，所以获取limit个就必须获取2*limit次
        limit = limit * 2;
        Set<String> appIds = null;

        //获取网页源码，得到appid
        HttpGet get = new HttpGet(uri);
        CloseableHttpResponse response = client.execute(get);
        String html = EntityUtils.toString(response.getEntity());

        Pattern getAppIdRegex = Pattern.compile("(?m)/detail/index/soft_id/(.*?)\"");
        Matcher matcher = getAppIdRegex.matcher(html);

        //至少有一个结果才new一个set
        if (matcher.find()) {
            appIds = new HashSet<String>();
        } else {
            return appIds;
        }

        //控制获取的appid个数
        int count = 0;
        //把所有匹配到的appid加入到结果中
        do {
            if (count < limit) {
                appIds.add(matcher.group(1));
                count++;
            }
        } while (matcher.find());

        return appIds;*/
        Set<String> s = new HashSet<String>();
//        s.add("3581");
//        s.add("778702");
//        s.add("1586");
//        s.add("6276");
//        s.add("122437");
//        s.add("5632");
//        s.add("4107");
//        s.add("98008");
//        s.add("3100672");
//        s.add("2345172");
//        s.add("1343");
//        s.add("3094256");
//        s.add("101594");
//        s.add("1840672");
//        s.add("1643");
//        s.add("893686");
//        s.add("3032510");
        s.add("1936882");
//        s.add("7256");
//        s.add("727030");

        return s;
    }

    public static int getCommentCount(int appId) throws IOException {
        CloseableHttpClient client = HttpClients.createDefault();

        HttpGet getJson = new HttpGet("http://comment.mobilem.360.cn/comment/getComments?baike=" + appId + "&level=0&start=0&count=1&fm=home_jingjia_3&m=c1804fc5ca4ded8293acd1151efaf3db&m2=61f3c1e4d105b55aff323b20a8136c4e&v=3.2.50&re=1&nt=1&ch=493041&os=21&model=MX4+Pro&sn=4.66476154040931&cu=m76&ca1=armeabi-v7a&ca2=armeabi&ppi=1536x2560&cpc=1&startCount=4");
        CloseableHttpResponse response = client.execute(getJson);
        String json = EntityUtils.toString(response.getEntity());
        JSONObject jsonObject = new JSONObject(json);

        return jsonObject.getJSONObject("data").getInt("total");
    }
}