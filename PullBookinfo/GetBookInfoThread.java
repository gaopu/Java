import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.dom4j.Element;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GetBookInfoThread extends Thread{
    private CloseableHttpClient httpClient;
    private String webAddress;
    private Element rootElement;
    private Pattern bookAuthorRegex;
    private Pattern bookPublishRegex;
    private Pattern bookIsbnRegex;
    private String bookName;

    /**
     *
     * @param httpClient    用这个操作抓取
     * @param webAddress    这个是抓取的网址
     * @param rootElement   这个是一个xml文档的根节点,用这个来操作加入新的子节点
     */
    public GetBookInfoThread(CloseableHttpClient httpClient,String webAddress,String bookName,Element rootElement,Pattern bookAuthorRegex,Pattern bookPublishRegex,Pattern bookIsbnRegex) {
        this.httpClient = httpClient;
        this.webAddress = webAddress;
        this.rootElement = rootElement;
        this.bookAuthorRegex = bookAuthorRegex;
        this.bookPublishRegex = bookPublishRegex;
        this.bookIsbnRegex = bookIsbnRegex;
        this.bookName = bookName;
    }

    @Override
    public void run() {
        HttpGet getBookInfo = new HttpGet(webAddress);
        CloseableHttpResponse bookInfoResponse = null;
        String bookInfoCode = null;//书籍具体信息网页源码
        try {
            bookInfoResponse = httpClient.execute(getBookInfo);
            bookInfoCode = EntityUtils.toString(bookInfoResponse.getEntity());
        } catch (IOException e) {
            e.printStackTrace();
        }

        Matcher bookAuthorMatcher = bookAuthorRegex.matcher(bookInfoCode);
        Matcher bookPublishMatcher = bookPublishRegex.matcher(bookInfoCode);
        Matcher bookIsbnMatcher = bookIsbnRegex.matcher(bookInfoCode);

        String bookName = this.bookName;
        String bookAuthor = "";
        String bookPublish = "";
        String bookIsbn = "";
        String bookLink = webAddress;

        if (bookAuthorMatcher.find()) {
            bookAuthor = bookAuthorMatcher.group(1);
        }
        if (bookPublishMatcher.find()) {
            bookPublish = bookPublishMatcher.group(1);
        }
        if (bookIsbnMatcher.find()) {
            bookIsbn = bookIsbnMatcher.group(1);
        }

//                    System.out.println(bookName + "-" + bookAuthor + "-" + bookPublish + "-" + bookIsbn);

        Element bookElement = rootElement.addElement("book");//新建一个书的标签
        bookElement.addAttribute("id",String.valueOf(Main.bookId++));
        bookElement.addElement("name").setText(bookName);
        bookElement.addElement("author").setText(bookAuthor);
        bookElement.addElement("publish").setText(bookPublish);
        bookElement.addElement("isbn").setText(bookIsbn);
        bookElement.addElement("count").setText("5");
        bookElement.addElement("link").setText(bookLink);

        System.out.println("抓取了:" + webAddress + " " + bookName);
    }
}
