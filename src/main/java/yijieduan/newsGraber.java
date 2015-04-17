package yijieduan;

/**
 * Created by hexing on 15-3-31.
 */
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * Created by hexing on 15-3-11.
 */
public class newsGraber {
    public static void main(String[] args) {

        Document rootDoc = null;
        try {
            rootDoc = Jsoup.connect("http://news.163.com/domestic/").get();
        } catch (IOException e) {
            e.printStackTrace();
        }

        //首先获取多个页面的链接
        List<String> pageLinks = new LinkedList();
        Element pageLinksElement = rootDoc.getElementsByClass("list-page").first();
        Elements allA = pageLinksElement.select("a");
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

        //然后获取各个页面的文章的链接
        Set<String> newLinks = new HashSet<String>();
        Document doc = null;
        for (int i = 0; i < pageLinks.size(); i++) {
            try {
                doc = Jsoup.connect(pageLinks.get(i)).get();
            } catch (IOException e) {
                e.printStackTrace();
            }
            Elements divs = doc.getElementsByClass("item-top");
            for (Element div : divs) {
                Element a = div.select("a").first();
                newLinks.add(a.attr("abs:href"));
            }
        }

        //然后对各个文章进行处理
        for (String articleLink : newLinks) {
            Document articleDoc = null;
            try {
                articleDoc = Jsoup.connect(articleLink).get();
            } catch (IOException e) {
                System.out.println("获取网页源码失败1：" + articleLink);
                e.printStackTrace();
            } catch (NullPointerException e) {
                System.out.println("获取网页源码失败2:" + articleLink);
                e.printStackTrace();
            }

            //获取新闻标题
            String title = null;
            try {
                title = articleDoc.title();
            } catch (NullPointerException e) {
                System.out.println("获取文章标题失败：" + articleLink);
                e.printStackTrace();
            }
            System.out.println(title);

            //获取各个文章的图片地址
            List<String> jpgAddr = new LinkedList<String>();
            Elements allJpgs = articleDoc.getElementsByClass("f-center");
            for (Element jpg : allJpgs) {
                Element img = jpg.select("img").first();
                try {
                    jpgAddr.add(img.attr("src"));
                } catch (NullPointerException e) {
                    System.out.println("获取图片地址失败：" + articleLink);
                    e.printStackTrace();
                }
            }

            //获取时间及正文
            String time = null;
            try {
                Elements allp = null;
                try {
                    Element articlewen = null;
                    if (articleLink.startsWith("http://news")) {
                        articlewen = articleDoc.getElementById("endText");
                        time = articleDoc.getElementsByClass("ep-time-soure").first().text();
                    } else if (articleLink.startsWith("http://view")) {
                        articlewen = articleDoc.getElementsByClass("feed-text").first();
                        time = articleDoc.getElementById("ptime").text();
                    } else {
                        System.out.println("新类型：" + articleLink);
                        continue;
                    }
                    allp = articlewen.select("p");
                } catch (NullPointerException e) {
                    System.out.println("文章正文解析出错：" + articleLink);
                    e.printStackTrace();
                }

                //存放新闻
                BufferedWriter bw= null;
                File dir = new File("/home/hexing/news");
                if (!dir.exists()) dir.mkdir();
                File file = new File(dir, title);
                try {
                    if (!file.exists())
                        file.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                try {
                    bw = new BufferedWriter(new FileWriter(file));
                    bw.write("题目：" + title);
                    bw.newLine();
                    bw.write("时间：" + time);
                    bw.newLine();
                    for (Element p : allp) {
                        bw.write(p.text());
                    }
                    bw.newLine();
                    bw.write("文章地址: " + articleLink);
                    for (String jj : jpgAddr) {
                        bw.newLine();
                        bw.write("图片地址：" + jj);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    try {
                        bw.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            } catch (NullPointerException e) {
                e.printStackTrace();
            }

        }
    }
}