package com.crawl.comments;

import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by geekgao on 15-10-25.
 */
public class Main {
    public static void main(String[] args) throws IOException, InterruptedException {
        //获取要抓取的app的id
        Set<String> appIds = CrawlUtils.getAppIds("",1);
        //设置超时
        RequestConfig requestConfig = RequestConfig.custom().setConnectionRequestTimeout(2000).setSocketTimeout(2000).setConnectTimeout(2000).build();
        //建立client
        CloseableHttpClient client = HttpClients.custom().setDefaultRequestConfig(requestConfig).build();

        for (String id:appIds) {
            //建立线程池
            ExecutorService executorService = Executors.newFixedThreadPool(30);
            //建立xml根节点
            Element app = DocumentHelper.createDocument().addElement("app");

            //添加appid节点
            String appName = CrawlUtils.getAppName(id);
            if (appName == null) {
                System.out.println(id + "号app名称评论抓取失败,所以跳过抓取评论");
                continue;
            }

            app.addElement("appid").setText(appName);
            System.out.println("开始抓取[" + appName + "],id=" + id);

            //获取app评分和各类型的评论数目信息
            HttpGet getJson = new HttpGet("http://comment.mobilem.360.cn/comment/getCommentTags?objid=" + id + "&fm=home_jingjia_3&m=c1804fc5ca4ded8293acd1151efaf3db&m2=61f3c1e4d105b55aff323b20a8136c4e&v=3.2.50&re=1&nt=1&ch=493041&os=21&model=MX4+Pro&sn=4.66476154040931&cu=m76&ca1=armeabi-v7a&ca2=armeabi&ppi=1536x2560&cpc=1&startCount=4");
            CloseableHttpResponse response = client.execute(getJson);
            String json = EntityUtils.toString(response.getEntity());
            JSONObject jsonObject = new JSONObject(json);

            //获取分数
            double overallrating = (Double.valueOf(jsonObject.getJSONObject("data").getJSONObject("score").getString("score"))) / 10;
            JSONArray jsonArray = jsonObject.getJSONObject("data").getJSONArray("tag");
            String good = String.valueOf(jsonArray.getJSONObject(1).get("num"));
            String neutral = String.valueOf(jsonArray.getJSONObject(2).get("num"));
            String poor = String.valueOf(jsonArray.getJSONObject(3).get("num"));

            app.addElement("overallrating").setText(String.valueOf(overallrating));
            app.addElement("good").setText(String.valueOf(good));
            app.addElement("neutral").setText(neutral);
            app.addElement("poor").setText(poor);

            int commentsCount = CrawlUtils.getCommentCount(Integer.valueOf(id));
            System.out.println("[" + appName + "]总共" + commentsCount + "条评论");
            //每次获取的评论个数
            int count = 25;
            for (int start = 0;start < commentsCount;start += count) {
                //如果最后一次不够count个评论
                if (start + count > commentsCount) {
                    count = commentsCount - start;
                }

//                System.out.println("从第" + start + "个评论开始抓取");
                executorService.submit(new CrawlComments(app, start, count, Integer.valueOf(id)));
            }

            executorService.shutdown();
            while (true) {
                if (executorService.isTerminated()) {
                    break;
                }
                Thread.sleep(1000);
            }

            CrawlUtils.writeXmlToFile(app,"/home/geekgao/comments/" + System.currentTimeMillis() + ".xml");
        }
        client.close();
    }
}
