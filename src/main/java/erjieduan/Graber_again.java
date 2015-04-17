package erjieduan;

/**
 * Created by hexing on 15-3-31.
 */
import com.thoughtworks.xstream.XStream;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.*;
import java.util.LinkedList;
import java.util.List;

class news{
    String title; //文章标题
    String url; //新闻地址
    String date; //新闻时间
    String forward; //来源媒体
    String content; //新闻正文
    List<String> img; //图片地址
}


class Getnews extends Thread {
    private List<news> newsList;  //存储抓取的新闻内容
    private List<String> newsLinks; //存储新闻的地址
    private int number;  //多线程根据这个处理各篇文章

    //构造函数
    public Getnews() {
        newsList = new LinkedList<news>();
        number = -1;
    }

    //抓取各篇新闻存储在news类
    public List<news> GetnewsList(){
        Document rootDoc = null;
        try {
            rootDoc = Jsoup.connect("http://news.163.com/domestic/").get();
        } catch (IOException e) {
            e.printStackTrace();
        }

        //获取所有页面的链接
        List<String> pageLinks = new LinkedList<String>();
        Element pagelinkselement = rootDoc.getElementsByClass("list-page").first();
        Elements allA = pagelinkselement.select("a");

        int count = -1;
        for (Element a : allA) {
            if (count == -1) {
                count++;
                continue;
            }
            pageLinks.add(a.attr("abs:href"));
            count++;
            if (count == 10) {
                break;
            }
        }

        //获取所有页面中各个文章的地址
        Document pageDoc = null;
        newsLinks = new LinkedList<String>();
        for (int i = 0; i < pageLinks.size(); i++) {
            try {
                pageDoc = Jsoup.connect(pageLinks.get(i)).get();
            } catch (IOException e) {
                e.printStackTrace();
            }

            Elements divs = pageDoc.getElementsByClass("item-top");
            for (Element div : divs) {
                Element a = div.select("a").first();
                newsLinks.add(a.attr("abs:href"));
            }
        }

        //然后分别处理每一篇文章存储到news类中
        for (int i = 0; i < newsLinks.size(); i++) {
            Article t = new Article();
            t.start();
            try {
                t.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return newsList;
    }

    class Article extends Thread{
        public void run(){
            number++;
            news singalNews = new news();
            String articleLink = newsLinks.get(number);
            Document articleDoc = null;

            singalNews.url = articleLink;
            try {
                articleDoc = Jsoup.connect(articleLink).get();
            }catch (IOException e){
                System.out.println("获取网页源码错误1："+articleLink);
                e.printStackTrace();
            }catch (NullPointerException e){
                System.out.println("获取源码错误2："+articleLink);
            }

            //获取新闻标题
            String title = null;
            try {
                title = articleDoc.title();
            }catch (NullPointerException e){
                e.printStackTrace();
            }
            singalNews.title = title;

            //获取正文及时间
            String time = null;
            Elements allp = null;
            try {
                Element articlewen = null;
                if(articleLink.startsWith("http://news") || articleLink.startsWith("http://war")){
                    articlewen = articleDoc.getElementById("endText");
                    time = articleDoc.getElementsByClass("ep-time-soure").first().text();
                    singalNews.date = time.substring(0,19); //时间
                    singalNews.forward = time.substring(20);//来源
                }else if (articleLink.startsWith("http://view")){
                    articlewen = articleDoc.getElementsByClass("feed-text").first();
                    time = articleDoc.getElementById("ptime").text();
                    singalNews.date = time.substring(0,19); //时间
                    singalNews.forward = null;
                }else if(articleLink.startsWith("http://focus.news")){
                    articlewen = articleDoc.getElementById("endText");
                    singalNews.date = null;
                    singalNews.forward = null;
                }else {
                    System.out.println("新类型："+articleLink);
                }
                allp = articlewen.select("p");
            }catch (NullPointerException e){
                System.out.println("文章正文出错："+articleLink);
                e.printStackTrace();
            }

            StringBuffer content = new StringBuffer();
            for(Element p:allp){
                content.append(p.text());
            }

            singalNews.content = content.toString(); //正文

            List<String> imgList = new LinkedList<String>();
            Elements allJpg = articleDoc.getElementsByClass("f-center");
            for (Element jpg:allJpg){
                Element img = jpg.select("img").first();
                imgList.add(img.attr("src"));
            }
            singalNews.img = imgList;
            newsList.add(singalNews);
        }
    }
}


class writeNews extends Thread{
    private news singleNew;

    public writeNews(news singleNew){
        this.singleNew = singleNew;
    }

    public void run(){
        XStream Stream = new XStream();
        String newxml = Stream.toXML(singleNew);

        File dir = new File("/home/hexing/news_again");
        if (!dir.exists()) dir.mkdir();
        File file = new File(dir, singleNew.title);
        try {
            if (!file.exists())
                file.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        FileOutputStream fw = null;
        OutputStreamWriter ow = null;
        try {
            fw = new FileOutputStream(file);
            ow = new OutputStreamWriter(fw);
            ow.write(new String(newxml.getBytes(), ow.getEncoding()));
            ow.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

public class Graber_again  {
    public static void main(String[] args) {
        List<news> newsList = new Getnews().GetnewsList();
        for(news singlenews:newsList){
            writeNews t = new writeNews(singlenews);
            t.start();
        }

    }
}