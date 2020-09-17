package com.vua.grab;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Test {
    static final int[][] coordinate={{-105,-20},{-35,-20},{40,-20},{110,-20},{-105,50},{-35,50},{40,50},{110,50}};
    public static void main(String[] args) {
        parseResponse("标签从左到右依次是:<br><span>锦旗</span>\t<br>\n" +
                "图片从左到右从上到下依次是:<br><span>拖把</span>\t<span>锦旗</span>\t<span>毛线</span>\t<span>锦旗</span>\t<br><span>拖把</span>\t<span>双面胶</span>\t<span>公交卡</span>\t<span>棉棒</span>\t<br>\n");
    }
    static Pattern p=Pattern.compile("<span>(.*)</span>");
    public static List<Integer> parseResponse(String response) {
        System.out.println(response);
        String[] segments = response.split("\n");
        String[] words=segments[0].split("\t");

        String[] images=segments[1].split("\t");

        Set<String> labels=new HashSet<>();

        List<Integer> hits=new ArrayList<>();
        for (String word:words){

            Matcher m = p.matcher(word);
            while (m.find()) {
                labels.add(m.group(1));
            }
        }
        for (int i=0;i<8;i++){
            Matcher m = p.matcher(images[i]);
            while (m.find()) {
                String clazz=m.group(1);
                if(labels.contains(clazz)) {
                    hits.add(i);
                }
            }
        }
        return hits;

    }
}
