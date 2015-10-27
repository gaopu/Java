package com.crawl.comments;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.ConnectionPoolTimeoutException;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.dom4j.Element;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.net.SocketTimeoutException;

/**
 * Created by geekgao on 15-10-19.
 */
public class CrawlComments implements Runnable {
    private Element app;
    private int start;
    private int count;
    private int appId;

    public CrawlComments(Element app, int start, int count, int appId) {
        this.app = app;
        this.start = start;
        this.count = count;
        this.appId = appId;
    }

    private void setAppXml() throws IOException {
        //设置超时
        RequestConfig requestConfig = RequestConfig.custom().setConnectionRequestTimeout(2000).setSocketTimeout(6000).setConnectTimeout(2000).build();
        //建立client
        CloseableHttpClient client = HttpClients.custom().setDefaultRequestConfig(requestConfig).build();

        HttpGet getContentJson = new HttpGet("http://comment.mobilem.360.cn/comment/getComments?baike=" + appId + "&level=0&start=" + start + "&count=" + count + "&fm=home_jingjia_3&m=c1804fc5ca4ded8293acd1151efaf3db&m2=61f3c1e4d105b55aff323b20a8136c4e&v=3.2.50&re=1&nt=1&ch=493041&os=21&model=MX4+Pro&sn=4.66476154040931&cu=m76&ca1=armeabi-v7a&ca2=armeabi&ppi=1536x2560&cpc=1&startCount=4");
        String contentJson = EntityUtils.toString(client.execute(getContentJson).getEntity());

        JSONObject jsonObject = new JSONObject(contentJson);
        JSONArray contentJsonArray = jsonObject.getJSONObject("data").getJSONArray("messages");

        for (int i = 0;i < contentJsonArray.length();i++) {
            JSONObject messageJsonObject = contentJsonArray.getJSONObject(i);

            String userid = messageJsonObject.getString("username");
            String time = messageJsonObject.getString("create_time");
            String score = String.valueOf(messageJsonObject.getInt("score"));
            String review = messageJsonObject.getString("content");
            String agreecount = messageJsonObject.getString("likes");

            Element comment = app.addElement("comment");
            comment.addElement("userid").setText(userid);
            comment.addElement("time").setText(time);
            comment.addElement("score").setText(score);
            comment.addElement("review").setText(review);
            comment.addElement("agreecount").setText(agreecount);
        }
        client.close();
    }

    public void run() {
        try {
            setAppXml();
        } catch (ConnectionPoolTimeoutException e) {
            System.err.println(appId + "号app从" + start + "开始的评论发生-ConnectionPoolTimeoutException");
            return;
        } catch (ClientProtocolException e) {
            e.printStackTrace();
            return;
        } catch (SocketTimeoutException e) {
            System.err.println(appId + "号app从" + start + "开始的评论发生-SocketTimeoutException");
            return;
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
        System.out.println(appId + "号app从" + start + "开始的评论抓取完毕");
    }
}