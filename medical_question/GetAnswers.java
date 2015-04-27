import org.dom4j.DocumentHelper;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.*;

public class GetAnswers {

    Map<String,String> categoryLink;//获取八个"类别"的名字和链接

    public static void main(String[] args) {
        new GetAnswers().launch();
    }

    public void launch() {
        getCategoryLink();
        getAllAnswer();
    }

    private void getAllAnswer() {
        Set<String> categoryName = categoryLink.keySet();
        for (String name:categoryName) {
            if (name.equals("呼吸内科") || name.equals("内分泌科") || name.equals("肾内科") || name.equals("消化内科") || name.equals("血液科") || name.equals("风湿科")) {
                continue;
            }
            String webAddress = categoryLink.get(name);//“页”的链接

            //链接地址不为空就表明还有下一页(到最后一页后设置为空)
            //循环遍历每一页
            int pageCount = 1;//记录抓取的页数
            int allPageCount = 0;//记录总共需要抓取的网页页数

            try {
                Document tempDom = Jsoup.connect(webAddress).userAgent("Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/38.0.2125.122 Safari/537.36 QIHU 360EE").timeout(5000).get();
                allPageCount = Integer.parseInt(tempDom.select(".pager-last").attr("href").split("=")[1].split("#")[0])/25 + 1;
            } catch (IOException e) {
                e.printStackTrace();
            }

            for (int i = 1;i <= allPageCount;i++) {
                List<String> answerAddressList = new LinkedList<String>();//先获取一页之内每一个有回答的问题的链接,存储在这个List里
                Document dom = null;
                try {
                    dom = Jsoup.connect(webAddress + "?pn=" + (i - 1) * 25 + "#list").userAgent("Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/38.0.2125.122 Safari/537.36 QIHU 360EE").timeout(5000).get();
                    Elements allQuestion = dom.select(".question-list").select(".question-item");
                    for (Element question:allQuestion) {
                        if (!question.select(".title-line").select(".question-answer-num").text().equals("0回答")) {
                            answerAddressList.add(question.select(".title-line").select(".title-container").select(".question-title").attr("abs:href"));
                        }
                    }
                } catch (SocketTimeoutException e) {
                    System.out.println("连接超时:" + webAddress);
                } catch (IOException e) {
                    e.printStackTrace();
                    System.out.println("意外错误:" + webAddress);
                }
                //上面这部分获取了一页上所有问题的链接

                //下面就进每一个问题的页面，抓取信息放到dom4j的Document中,最后用多线程写入文件
                Map<String, org.dom4j.Document> answerDom4jDocument = new HashMap<String, org.dom4j.Document>();//问题题目对应题目的document(题目是文件的文件名)
                for (String answerAddress:answerAddressList) {
                    //System.out.println("正在抓[" + name + "]类别下的" + answerAddress);
                    try {
                        Document answerDom = Jsoup.connect(answerAddress).userAgent("Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/38.0.2125.122 Safari/537.36 QIHU 360EE").timeout(5000).get();
                        String type = name;
                        String title = answerDom.title();
                        String answerNum = answerDom.select("#wgt-answers").select(".hd").select("h2").text().split("条")[0];

                        org.dom4j.Document document = DocumentHelper.createDocument();//这个问题的dom树
                        answerDom4jDocument.put(title,document);
                        org.dom4j.Element root = document.addElement("root");
                        root.addElement("question").addText(title).addAttribute("type",name);
                        org.dom4j.Element answers = root.addElement("answers").addAttribute("number",answerNum);

                        //答案有三种【题主选择的最佳答案，网友选择的最佳答案，其他答案】

                        Elements allAnswer = answerDom.select(".bd.answer");
                        //分别处理每一页的所有答案
                        for (Element answer:allAnswer) {
                            String text = answer.select(".answer-text").text();
                            String username = answer.select(".line.info.f-aid").select(".user-name").text();
                            if (username.equals("")) {
                                username = answer.select(".line.info.f-aid").select(".mavin-name").text();
                            }
                            String grade = answer.select(".line.info.f-aid").text().split(" ")[answer.select(".line.info.f-aid").text().split(" ").length - 1];
                            if (grade.equals("最快回答")) {
                                grade = answer.select(".line.info.f-aid").text().split(" ")[answer.select(".line.info.f-aid").text().split(" ").length - 2];
                            }

                            //不是“*级”那就是一个专家的称号
                            String author = "null";
                            if (!grade.contains("级")) {
                                author = grade;
                                grade = answer.select(".line.info.f-aid").select(".f-orange.f-yahei.ml-5").select("span").text();
                            }

                            String support = answer.select(".line.content").select(".grid-r.f-aid").select(".evaluate").attr("data-evaluate");
                            String unsupport = answer.select(".line.content").select(".grid-r.f-aid").select(".evaluate.evaluate-bad").attr("data-evaluate");

                            org.dom4j.Element ans = answers.addElement("answer").addAttribute("username",username).addAttribute("grade",grade).addAttribute("author",author);
                            ans.addElement("text").addText(text);
                            ans.addElement("support").addText(support);
                            ans.addElement("unsupport").addText(unsupport);

                            if (answer.hasClass("wgt-replyer-best")) {
                                ans.addElement("best_answer").addText("yes");
                            } else {
                                ans.addElement("best_answer").addText("no");
                            }
                        }


                    } catch (SocketTimeoutException e) {
                        System.out.println("连接超时:" + answerAddress);
                    } catch (IOException e) {
                        e.printStackTrace();
                        System.out.println("意外错误:" + answerAddress);
                    }
                }

                Set<String> answerTitle = answerDom4jDocument.keySet();
                //多线程写入文件
                for (String title:answerTitle) {
                    //分类目录不存在时创建文件夹
                    if (!new File("/home/geekgao/medical_question/" + name).exists()) {
                        new File("/home/geekgao/medical_question/" + name).mkdir();
                    }

                    new WriteAnswersToFile("/home/geekgao/medical_question/" + name + "/" + System.currentTimeMillis() + ".xml",answerDom4jDocument.get(title)).start();
                }

                System.out.println("[" + name + "]类别第" + pageCount++ +"页已写入文件.");

            }
        }
    }

    public GetAnswers() {
        categoryLink = new HashMap<String, String>();
    }

    private void getCategoryLink() {
        try {
            Document dom = Jsoup.connect("http://zhidao.baidu.com/browse/790").userAgent("Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/38.0.2125.122 Safari/537.36 QIHU 360EE").timeout(5000).get();
            Element categorys = dom.select(".category-list").first();
            Elements allLi = categorys.select("li");

            for (Element li:allLi) {
                categoryLink.put(li.text(), "http://zhidao.baidu.com" + li.select("a").attr("href"));
            }

        } catch (SocketTimeoutException e) {
            System.out.println("连接超时:http://zhidao.baidu.com/browse/790");
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("连接超时:http://zhidao.baidu.com/browse/790");
        }
    }
}
