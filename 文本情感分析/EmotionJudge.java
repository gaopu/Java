package org.geekgao.one;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class EmotionJudge {
    private double priorPositive;//积极先验概率
    private double priorNegative;//消极先验概率
    private double priorUnsure;//不确定先验概率

    private Map<String,Double> backPositive;//词语的后验概率
    private Map<String,Double> backNegative;//同上
    private Map<String,Double> backUnsure;//同上

    private boolean isGroup = false;
    private String strTemp;
    private Map<String,Integer> articleWordMap;

    //这两个是词典的位置
    private final String posiDictPath = "/home/geekgao/朴素贝叶斯/台湾大学情感词典/ntusd-positive.txt";
    private final String negaDictPath = "/home/geekgao/朴素贝叶斯/台湾大学情感词典/ntusd-negative.txt";

    //这两个存储词典中的词语
    private Set<String> positiveDict;
    private Set<String> negativeDict;

    public static void main(String [] args) {
        new EmotionJudge().launch();
    }

    public void launch() {
        getPrior();
        getBack();

        positiveDict = new HashSet<String>();
        negativeDict = new HashSet<String>();
        readEmotionWord(positiveDict, posiDictPath);
        readEmotionWord(negativeDict, negaDictPath);
        calc();
    }

    //获得先验概率
    public void getPrior() {
        SAXReader sax = new SAXReader();
        try {
            //从这读取doc的值
            Document document = sax.read(new File("/home/geekgao/doc.xml"));
            Element root = document.getRootElement();
            List<Element> prior = root.elements();

            priorPositive = Double.valueOf(prior.get(0).attributeValue("pPositive"));
            priorNegative = Double.valueOf(prior.get(0).attributeValue("pNegative"));
            priorUnsure = Double.valueOf(prior.get(0).attributeValue("pUnsure"));

        } catch (DocumentException e) {
            e.printStackTrace();
        }
    }

    //获得后验概率
    public void getBack() {
        SAXReader sax = new SAXReader();
        try {
            //从这读取weight的值
            Document document = sax.read(new File("/home/geekgao/weight.xml"));
            Element root = document.getRootElement();
            List<Element> back = root.elements();

            backNegative = new HashMap<String, Double>();
            backPositive = new HashMap<String, Double>();
            backUnsure = new HashMap<String, Double>();

            double backPos;//积极后验概率
            double backNeg;//消极后验概率
            double backUns;//不确定后验概率
            String word;

            for (int i = 0;i < back.size();i++) {
                backPos = Double.valueOf(back.get(i).attributeValue("pPositive"));
                backNeg = Double.valueOf(back.get(i).attributeValue("pNegative"));
                backUns = Double.valueOf(back.get(i).attributeValue("pUnsure"));
                word = back.get(i).attributeValue("data");

                backPositive.put(word,backPos);
                backNegative.put(word,backNeg);
                backUnsure.put(word,backUns);
            }
        } catch (DocumentException e) {
            e.printStackTrace();
        }
    }

    public void calc() {
        articleWordMap = new HashMap<String, Integer>();

        //读取文章
        calcFreauency(articleWordMap,new File("/home/geekgao/朴素贝叶斯/500trainblogxml/positiveout/1377331000713.txt"));
        keepEmotionWord(articleWordMap);

        double allBackPos = 1;
        double allBackNeg = 1;
        double allBackUns = 1;

        Set<String> word = articleWordMap.keySet();

        for (Iterator it = word.iterator();it.hasNext();) {
            String tmp = (String)it.next();
            double back;
            if (backPositive.containsKey(tmp)) {
                back = backPositive.get(tmp);
                allBackPos = Math.pow(back,articleWordMap.get(tmp)) * allBackPos;
            }

            if (backNegative.containsKey(tmp)) {
                back = backNegative.get(tmp);
                allBackNeg= Math.pow(back,articleWordMap.get(tmp)) * allBackNeg;
            }

            if (backUnsure.containsKey(tmp)) {
                back = backUnsure.get(tmp);
                allBackUns = Math.pow(back,articleWordMap.get(tmp)) * allBackUns;
            }
        }

        double resultPositive;
        double resultNegative;
        double resultUnsure;

        resultPositive = priorPositive * allBackPos;
        resultNegative = priorNegative * allBackNeg;
        resultUnsure = priorUnsure * allBackUns;

        System.out.println("积极：" + resultPositive);
        System.out.println("消极：" + resultNegative);
        System.out.println("不确定：" + resultUnsure);
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
}
