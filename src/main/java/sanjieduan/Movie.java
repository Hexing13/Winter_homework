package sanjieduan;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by hexing on 15-4-1.
 */
//电影信息类
public class Movie {

    String name;   //名称
    String quality; //画质
    StringBuffer classify; //分类
    StringBuffer area; //地区
    String year; //年份
    StringBuffer director; //导演/编剧
    StringBuffer actor; //主演
    String grade; //豆瓣评分
    // String picture; //电影海报地址
    //  String brief; //电影简介
    //  List<String> pictureAddress; //电影配图地址
    // String downloadAddress; //种子地址
    String movieWebAddress; //网页地址

    public Movie(){
        classify = new StringBuffer();
        area = new StringBuffer();
        director = new StringBuffer();
        actor = new StringBuffer();
    }


}