import org.apache.http.Consts;
import org.apache.http.NameValuePair;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        RequestConfig requestConfig = RequestConfig.custom().setCookieSpec(CookieSpecs.STANDARD_STRICT).build();
        CloseableHttpClient httpClient = HttpClients.custom().setDefaultRequestConfig(requestConfig).build();

        HttpGet get = new HttpGet("http://www.zhihu.com/");
        try {
            CloseableHttpResponse response = httpClient.execute(get);
            String responseHtml = EntityUtils.toString(response.getEntity());
            String xsrfValue = responseHtml.split("<input type=\"hidden\" name=\"_xsrf\" value=\"")[1].split("\"/>")[0];
            System.out.println("xsrfValue:" + xsrfValue);
            response.close();
            List<NameValuePair> valuePairs = new LinkedList<NameValuePair>();
            valuePairs.add(new BasicNameValuePair("_xsrf" , xsrfValue));
            valuePairs.add(new BasicNameValuePair("email", 用户名));
            valuePairs.add(new BasicNameValuePair("password", 密码));
            valuePairs.add(new BasicNameValuePair("rememberme", "y"));

            UrlEncodedFormEntity entity = new UrlEncodedFormEntity(valuePairs, Consts.UTF_8);
            HttpPost post = new HttpPost("http://www.zhihu.com/login");
            post.setEntity(entity);
            httpClient.execute(post);//登录

            HttpGet g = new HttpGet("http://www.zhihu.com/question/following");//获取“我关注的问题”页面
            CloseableHttpResponse r = httpClient.execute(g);
            System.out.println(EntityUtils.toString(r.getEntity()));
            r.close();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                httpClient.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
