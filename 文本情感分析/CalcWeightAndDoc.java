package org.geekgao.one;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;

import java.io.*;
import java.util.*;

public class CalcWeightAndDoc {
    //这三个常量是训练文章的存储的地方
    private final String positiveArticlePath = "/home/geekgao/practice/positive";
    private final String negativeArticlePath = "/home/geekgao/practice/negative";
    private final String unsureArticlePath = "/home/geekgao/practice/unsure";

    //这两个是词典的位置
    private final String posiDictPath = "/home/geekgao/朴素贝叶斯/台湾大学情感词典/ntusd-positive.txt";
    private final String negaDictPath = "/home/geekgao/朴素贝叶斯/台湾大学情感词典/ntusd-negative.txt";

    private Map<String,Integer> positiveWord;//存储积极词汇的map
    private Map<String,Integer> negativeWord;//存储消极词汇的map
    private Map<String,Integer> unsureWord;//存储不确定词汇的map

    //这两个存储词典中的词语
    private Set<String> positiveDict;
    private Set<String> negativeDict;

    //需要的全局变量
    private boolean isGroup = false;
    String strTemp;//从xml文件解析词语时用到的临时变量

    public static void main(String[] args) {
        new CalcWeightAndDoc().launch();
    }

    public void launch() {
        positiveDict = new HashSet<String>();
        negativeDict = new HashSet<String>();

        readEmotionWord(positiveDict,posiDictPath);
        readEmotionWord(negativeDict,negaDictPath);

        //这里两个地址是目标地址，生成的文件就在下面两个地址里
        calcDoc("/home/geekgao/doc.xml");
        calcWeight("/home/geekgao/weight.xml");

        System.out.println("执行完毕！");
    }

    public void readEmotionWord(Set<String> Dict, String dictPath) {
        File file = new File(dictPath);
        BufferedReader reader = null;
        try {
            String t;
            reader = new BufferedReader(new FileReader(file));
            while ((t = reader.readLine()) != null) {
                Dict.add(t);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {

                }
            }
        }
    }

    //参数是生成的xml文件的路径与名字
    public void calcDoc(String resultPath) {
        File negative[] = new File(negativeArticlePath).listFiles();
        File positive[] = new File(positiveArticlePath).listFiles();
        File unsure[] = new File(unsureArticlePath).listFiles();
        double negCount = 0;
        double posCount = 0;
        double unsCount = 0;

        try {
            for (File file : negative) {
                if (file.isFile()) {
                    negCount++;
                }
            }

            for (File file : positive) {
                if (file.isFile()) {
                    posCount++;
                }
            }

            for (File file : unsure) {
                if (file.isFile()) {
                    unsCount++;
                }
            }
        } catch(NullPointerException e){
            System.out.println("程序因为空引用结束！");
            System.exit(1);
        }

        //建立document对象
        try {
            Document document = DocumentHelper.createDocument();

            Element root = document.addElement("root");//添加文档根
            Element request = root.addElement("prior"); //添加root的子节点
            request.addAttribute("pNegative", String.valueOf(negCount/(negCount + posCount + unsCount)));
            request.addAttribute("pPositive", String.valueOf(posCount/(negCount + posCount + unsCount)));
            request.addAttribute("pUnsure", String.valueOf(unsCount/(negCount + posCount + unsCount)));

            OutputFormat format = OutputFormat.createPrettyPrint();
            format.setEncoding("UTF-8");//根据需要设置编码
            // 输出全部原始数据，并用它生成新的我们需要的XML文件
            XMLWriter writer2 = new XMLWriter(new FileWriter(new File(resultPath)), format);
            writer2.write(document); //输出到文件
            writer2.close();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //参数是生成的xml文件的路径与名字
    public void calcWeight(String resultPath) {
        positiveWord = new HashMap<String, Integer>();
        negativeWord = new HashMap<String, Integer>();
        unsureWord = new HashMap<String, Integer>();

        //计算各自类别所有文章中每个词汇出现的次数
        getWordMap(positiveWord,positiveArticlePath);
        getWordMap(negativeWord,negativeArticlePath);
        getWordMap(unsureWord,unsureArticlePath);

        //存储计算后验概率公式中的分母的第一部分，第二部分等于1
        double allPosWeight = 0;
        double allNegWeight = 0;
        double allUnsWeight = 0;

        //保留各个Map的情感词汇
        keepEmotionWord(positiveWord);
        keepEmotionWord(negativeWord);
        keepEmotionWord(unsureWord);

        /*System.out.println(positiveWord);
        System.out.println(negativeWord);
        System.out.println(unsureWord);*/

        /*
        （1）遍历positiveWord这个Map，得到里面的各个词语在积极词汇中的次数，再在其他两个Map中查看是否有这个词语，有，就把其他的那个
            次数加到当前Map的当前词语的value上，并且删除那个Map中的当前词语；没有这个词的话，那么在那个；类别中出现的次数就是0.
        （2）遍历negativeWord，不用看positiveWord了，只需看unsureWord，处理方法同上。
        （3）遍历unsureWord，这些词在其他两个类别中都是0，直接得到在当前类别中的值
         */

        try {
            Document xmlFile = DocumentHelper.createDocument();//建立一个xml文档
            Element root = xmlFile.addElement("root");

            Set<String> word = positiveWord.keySet();
            for (Iterator it = word.iterator();it.hasNext();) {
                String tmp = (String)it.next();
                Integer count = positiveWord.get(tmp);
                allPosWeight += count;
            }

            word = negativeWord.keySet();
            for (Iterator it = word.iterator();it.hasNext();) {
                String tmp = (String)it.next();
                Integer count = negativeWord.get(tmp);
                allNegWeight += count;
            }

            word = unsureWord.keySet();
            for (Iterator it = word.iterator();it.hasNext();) {
                String tmp = (String)it.next();
                Integer count = unsureWord.get(tmp);
                allUnsWeight += count;
            }

            word = positiveWord.keySet();
            for (Iterator it = word.iterator(); it.hasNext(); ) {
                Element wd = root.addElement("word");//建立新的词语节点
                String tmp = (String) it.next();
                wd.addAttribute("data",tmp);
                Integer count;

                count = positiveWord.get(tmp);
                wd.addAttribute("pPositive",String.valueOf(count / (allPosWeight + 1)));

                if (negativeWord.containsKey(tmp)) {
                    count = negativeWord.get(tmp);
                    negativeWord.remove(tmp);
                    wd.addAttribute("pNegative",String .valueOf(count / (allNegWeight + 1)));
                } else {
                    wd.addAttribute("pNegative","0");
                }

                if (unsureWord.containsKey(tmp)) {
                    count = unsureWord.get(tmp);
                    unsureWord.remove(tmp);
                    wd.addAttribute("pUnsure",String.valueOf(count / (allUnsWeight + 1)));
                } else {
                    wd.addAttribute("pUnsure","0");
                }
            }

            word = negativeWord.keySet();
            for (Iterator it = word.iterator(); it.hasNext(); ) {
                Element wd = root.addElement("word");//建立新的词语节点
                String tmp = (String) it.next();
                wd.addAttribute("data",tmp);
                Integer count;

                wd.addAttribute("pPositive","0");
                count = negativeWord.get(tmp);
                wd.addAttribute("pNegative",String .valueOf(count / (allNegWeight + 1)));

                if (unsureWord.containsKey(tmp)) {
                    count = unsureWord.get(tmp);
                    unsureWord.remove(tmp);
                    wd.addAttribute("pUnsure",String.valueOf(count / (allUnsWeight + 1)));
                } else {
                    wd.addAttribute("pUnsure","0");
                }
            }

            word = unsureWord.keySet();
            for (Iterator it = word.iterator(); it.hasNext(); ) {
                Element wd = root.addElement("word");//建立新的词语节点
                String tmp = (String) it.next();
                wd.addAttribute("data",tmp);
                Integer count;

                wd.addAttribute("pPositive","0");
                wd.addAttribute("pNegative","0");
                count = unsureWord.get(tmp);
                wd.addAttribute("pUnsure",String.valueOf(count / (allUnsWeight + 1)));
            }

            //输出全部原始数据，在编译器中显示
            OutputFormat format = OutputFormat.createPrettyPrint();
            format.setEncoding("UTF-8");//根据需要设置编码
            // 输出全部原始数据，并用它生成新的我们需要的XML文件
            XMLWriter writer2 = new XMLWriter(new FileWriter(new File(resultPath)), format);
            writer2.write(xmlFile); //输出到文件
            writer2.close();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void getWordMap(Map<String,Integer> wordMap,String articlePath) {
        File articleArray[] = new File(articlePath).listFiles();//将文件夹中的文件都读取进来，下面就一个个的分析

        for (int i = 0;i < articleArray.length;i++) {
            calcFreauency(wordMap,articleArray[i]);
        }
    }

    //解析出文章中的词语，并且映射上频数
    public void calcFreauency(Map<String,Integer> wordMap,File article) {
        try {
            //取得dom4j的解析器
            SAXReader reader = new SAXReader();
            //取得代表文档的Document对象
            Document document = reader.read(article);
            //取得根结点
            Element root = document.getRootElement();//取得根节点<document>

            List<?> list1 = root.elements();//取得<document>的子节点
            List<?> sentence_list = ((Element)list1.get(0)).elements();//<content>下的<sentence>集合

            List<?> tok_list;//<sentence>下的<tok>集合
            //Dom4jDemo t = new Dom4jDemo();
            //遍历<sentence>节点
            for (int i = 0; i < sentence_list.size(); i++) {
                tok_list = ((Element)sentence_list.get(i)).elements();//获得每个sentence的tok集合
                for (int j = 0;j < tok_list.size();j++) {
                    setWordMap((Element)tok_list.get(j),wordMap);
                }
            }
        } catch (DocumentException e) {
            e.printStackTrace();
        }
    }

    public void setWordMap(Element tok,Map<String,Integer> wordMap) {
        String type,text;
        List<?> list;

        if (!(tok.getName().equals("tok"))) {//如果不是tok节点，那么就不用处理了
            return ;
        }
        //获取属性type
        type = tok.attributeValue("type");
        //只访问原子节点
        if (type.equals("atom") && isGroup) {
            text = tok.getText();
            text = text.replace("\t", "");
            text = text.replace("\n", "");
			/*System.out.print(text + " ");*/
            strTemp = strTemp + text;
        } else if (type.equals("group")) {
            isGroup = true;
            strTemp = "";
            list = tok.elements();
            for (int k = 0,size3 = list.size();k < size3;k++) {
                tok = (Element)list.get(k);
                setWordMap(tok,wordMap);
            }
            Integer count = wordMap.get(strTemp);//计算当前map里面的当前text对应的次数
            wordMap.put(strTemp,count == null?1:count + 1);
            isGroup = false;
        }
    }

    public void keepEmotionWord(Map<String,Integer> wordMap) {
        Set<String> word = wordMap.keySet();

        for (Iterator it = word.iterator();it.hasNext();) {
            String tmp = (String)it.next();
            //两个情感词典都不包含这个词语，那么就把这个词语去掉
            if (!positiveDict.contains(tmp) && !negativeDict.contains(tmp)) {
                it.remove();
            }
        }
    }
}
