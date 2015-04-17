package sanjieduan;


import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.sql.*;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by hexing on 15-4-1.
 */
public class GetmovieInfo {

    List<Movie> movieclass; //存储电影信息的类
    String pagewebAddress = "http://www.tiantangbbs.com/forum.php?mod=forumdisplay&fid=2&sortid=1&page=";
    String moviewebAddress = "http://www.tiantangbbs.com/";
    int id = 1;


    //构造函数
    public GetmovieInfo(){
        movieclass = new LinkedList<Movie>();
    }

    //获取movieclass类
    public void getInfotoDB() {

        //连接数据库
        String sql = null;
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs;
        int countMovie = 1; //记录电影的数量

        try {
            Class.forName("com.mysql.jdbc.Driver");
            System.out.println("驱动加载成功！！！");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        try {
            conn = DriverManager.getConnection("jdbc:mysql://localhost:3307/Info?"+"user=root&password=hx106107");
            stmt = conn.createStatement();
            System.out.println("成功连接数据库！！！");
        } catch (SQLException e) {
            e.printStackTrace();
        }

        //循环分别获取每一页的链接并分析
        for(int i = 1; i <= 1; i++){
            Document rootDoc = null;
            try {
                rootDoc = Jsoup.connect(pagewebAddress+i).userAgent("Mozilla/5.0 (Windows NT 5.2) AppleWebKit/534.30 (KHTML, like Gecko) Chrome/12.0.742.122 Safari/534.30").timeout(10000).get();
                //     System.out.println(rootDoc);
            } catch (IOException e) {
                e.printStackTrace();
            }

            //获取每页电影的元素信息
            Elements allMovies = rootDoc.getElementsByAttributeValue("style", "width:150px;");
            System.out.println(allMovies.size());
            for(Element movie:allMovies){
                //获取每页中每个电影的网页地址
                String movieLink = moviewebAddress+movie.select("a").attr("href");
                System.out.println("正在获取第" + countMovie++ +"个电影信息: "+movieLink);

                //获取各个电影的信息
                Document movieDoc = null;
                try {
                    movieDoc = Jsoup.connect(movieLink).userAgent("Mozilla/5.0 (Windows NT 5.2) AppleWebKit/534.30 (KHTML, like Gecko) Chrome/12.0.742.122 Safari/534.30").timeout(10000).get();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                Movie movieInfo = new Movie();
                movieInfo.name = movieDoc.select("caption").text().replace("影片信息","");
                movieInfo.name = movieInfo.name.replace('/', ' ').replace('\'', '\"');
                System.out.println(movieInfo.name);
                Element infoBody = movieDoc.getElementsByClass("typeoption").first();
                Elements tr = infoBody.select("tr");

                //画质
                movieInfo.quality = tr.get(0).select("a").text();
                System.out.println(movieInfo.quality);

                //分类
                Elements allClassify = tr.get(1).select("a");
                for(Element a: allClassify){
                    movieInfo.classify.append(a.text()).append("/");
                }
                movieInfo.classify.deleteCharAt(movieInfo.classify.length()-1);//移除最后一个"/"
                System.out.println(movieInfo.classify);

                //地区
                Elements allArea = tr.get(2).select("a");
                for(Element a: allArea){
                    movieInfo.area.append(a.text()).append("/");

                }
                movieInfo.area.deleteCharAt(movieInfo.area.length()-1);
                System.out.println(movieInfo.area);

                //年份
                movieInfo.year = tr.get(3).select("a").text();
                System.out.println(movieInfo.year);

                //导演/编剧
                Elements allDirector = tr.get(4).select("a");
                for(Element a: allDirector){
                    movieInfo.director.append(a.text()).append("/");

                }
                movieInfo.director.deleteCharAt(movieInfo.director.length()-1);
                movieInfo.director= new StringBuffer(movieInfo.director.toString().replace('\'','\"'));
                System.out.println(movieInfo.director);

                //主演
                Elements allActor = tr.get(5).select("a");
                for(Element a: allActor){
                    movieInfo.actor.append(a.text()).append("/");

                }
                movieInfo.actor.deleteCharAt(movieInfo.actor.length()-1);//移除最后一个"/"
                movieInfo.actor= new StringBuffer(movieInfo.actor.toString().replace('\'','\"'));
                System.out.println(movieInfo.actor);

                //豆瓣评分
                movieInfo.grade = tr.get(6).select("td").text();
                movieInfo.grade = movieInfo.grade.replace('\'','\"');
                System.out.println(movieInfo.grade);

               /* //电影简介(未完全处理)
                movieInfo.brief = movieDoc.getElementsByClass("t_fsz").first().getElementsByClass("t_f").first().text();
                movieInfo.brief = movieInfo.brief.replace('\'','\"');*/


               /* //配图地址
                Elements allPicture = movieDoc.select("ignore_is_op");
                for(Element a: allPicture){
                    movieInfo.pictureAddress.add(a.select("img").attr("src"));
                }*/

                //下载网页地址(不太理解)
                movieInfo.movieWebAddress = movieLink;
                System.out.println(movieInfo.movieWebAddress);
                movieclass.add(movieInfo);
            }
        }
       /* //存入数据库
        for(Movie a:movieclass){
            sql = "insert into movieInfo values ('"+id+"', '"+a.name+"','"+a.quality+"', '"+a.classify+"', '"+a.area+"','"+a.year+"','"+a.director+"','"+a.actor+"', '"+a.grade+"','"+a.movieWebAddress+"')";
            try {
                stmt.executeUpdate(sql);
                id++;
            } catch (SQLException e) {
                e.printStackTrace();
            }
            System.out.println("成功存入!");
        }*/
        for(Movie a:movieclass){
            sql = "insert into minfo values ('"+id+"','"+a.name+"')";
            try {
                stmt.executeUpdate(sql);
                id++;
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        try {
            conn.close();
            System.out.println("成功断开数据库！！！");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        GetmovieInfo movie = new GetmovieInfo();
        movie.getInfotoDB();
    }
}
