package com.cooooode.verify.service;

import com.cooooode.verify.util.HttpClient;
import com.google.common.annotations.VisibleForTesting;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @program: verify
 * @description:
 * @author: vua
 * @create: 2020-02-28 11:44
 */
@Service
public class RecordService {
    public static ConcurrentHashMap<String, String> map=new ConcurrentHashMap<>();
    //static SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    public void record(String ip){
        //sdf.format(new Date())
        map.put(ip,address(ip));
    }
    private static  String pattern=".*location\":\"(.*)\",\"titlecont\".*";
    private static Pattern r=Pattern.compile(pattern);
    public String address(String ip){
        String addr="unknown";
        String url="https://sp0.baidu.com/8aQDcjqpAAV3otqbppnN2DJv/api.php?query="+ip+"&co=&resource_id=6006";
        //String url="https://sp0.baidu.com/8aQDcjqpAAV3otqbppnN2DJv/api.php?query="+ip+"&co=&resource_id=6006";
        String response=HttpClient.doGet(url);
        //System.out.println(response);
        //[{"location":"美国","titlecont"
        Matcher m=r.matcher(response);
        if(m.find()) addr=m.group(1);
        return addr;
    }

}
