package com.cooooode.verify.controller;

import com.cooooode.verify.service.RecordService;
import com.cooooode.verify.service.VerifyService;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Base64;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @program: verify
 * @description:
 * @author: vua
 * @create: 2020-02-07 14:16
 */
@Controller
@RequestMapping("/")
public class VerifyController {
    @Autowired
    VerifyService verifyService;
    @PostMapping("")
    @ResponseBody
    @HystrixCommand(fallbackMethod = "fallBack")
    public String crack(@RequestParam("uploadImg")MultipartFile file){
        try {
            return verifyService.proccess(file.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }
    /*降级方法*/
    public String fallBack(@RequestParam("uploadImg")MultipartFile file){
        return "服务器忙,请重试";
    }
    @Autowired
    RecordService recordService;
    @GetMapping("")
    public String home(HttpServletRequest request){
        String ip=getIPAddress(request);
        recordService.record(ip);
        return "home";
    }

    public static String getIPAddress(HttpServletRequest request) {
        String ip = null;

        //X-Forwarded-For：Squid 服务代理
        String ipAddresses = request.getHeader("X-Forwarded-For");

        if (ipAddresses == null || ipAddresses.length() == 0 || "unknown".equalsIgnoreCase(ipAddresses)) {
            //Proxy-Client-IP：apache 服务代理
            ipAddresses = request.getHeader("Proxy-Client-IP");
        }

        if (ipAddresses == null || ipAddresses.length() == 0 || "unknown".equalsIgnoreCase(ipAddresses)) {
            //WL-Proxy-Client-IP：weblogic 服务代理
            ipAddresses = request.getHeader("WL-Proxy-Client-IP");
        }

        if (ipAddresses == null || ipAddresses.length() == 0 || "unknown".equalsIgnoreCase(ipAddresses)) {
            //HTTP_CLIENT_IP：有些代理服务器
            ipAddresses = request.getHeader("HTTP_CLIENT_IP");
        }

        if (ipAddresses == null || ipAddresses.length() == 0 || "unknown".equalsIgnoreCase(ipAddresses)) {
            //X-Real-IP：nginx服务代理
            ipAddresses = request.getHeader("X-Real-IP");
        }

        //有些网络通过多层代理，那么获取到的ip就会有多个，一般都是通过逗号（,）分割开来，并且第一个ip为客户端的真实IP
        if (ipAddresses != null && ipAddresses.length() != 0) {
            ip = ipAddresses.split(",")[0];
        }

        //还是不能获取到，最后再通过request.getRemoteAddr();获取
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ipAddresses)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }
    public String fallBack1(@RequestParam("base64")String base64){
        return "服务器忙,请重试";
    }

    @PostMapping("/base64")
    @ResponseBody
    @HystrixCommand(fallbackMethod = "fallBack1")
    public String base(@RequestParam("base64")String base64){
        if(verifyService.isBase64(base64))
            return verifyService.proccess(Base64.getDecoder().decode(base64));
        return "base64 校验失败";
    }

    @GetMapping("/ip")
    @ResponseBody
    public ConcurrentHashMap ip(){
        return recordService.map;
    }
}
