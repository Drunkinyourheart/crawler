package com.yeepay.bigdata.crawler.extractor;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Created by yp-tc-m-2505 on 11/10/14.
 */
public class ABC {

    public static void main(String[] args) throws IOException {
        Document document = Jsoup.connect("http://news.sina.com.cn/c/2014-11-04/080631090122.shtml").get();

        String tmp = "http://www.huxiu.com/article/";
        String b = "/1.html";
        File file = new File("abc.txt");

        file.createNewFile();
        FileWriter fileWriter = new FileWriter(file);
        for (int i = 1; i < 1008; ++i) {
            String ss = tmp + i + b;
            fileWriter.write(ss + "," + "\n");
        }
        if (fileWriter != null) {
            fileWriter.close();
        }

    }

}
