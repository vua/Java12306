package com.cooooode.verify.controller;

import com.cooooode.verify.service.RecordService;
import com.cooooode.verify.service.VerifyService;
import com.cooooode.verify.util.Crawling;
import com.cooooode.verify.util.HttpClient;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixProperty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Base64;
import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @program: verify
 * @description:
 * @author: vua
 * @create: 2020-02-07 14:16
 */
@Controller
@RequestMapping("/")
public class VerifyController {
    static AtomicInteger VAILD_REQUEST_NUMBER=new AtomicInteger();
    @GetMapping("/random")
    @ResponseBody
    public String getBase64(){
        return Crawling.getVerifyImageBase64();
    }
    @Autowired
    VerifyService verifyService;
    @PostMapping("")
    @ResponseBody
    @HystrixCommand(fallbackMethod = "fallBack",commandProperties = {
            @HystrixProperty(name="circuitBreaker.enabled",value = "true"), //断路器使能
            @HystrixProperty(name="circuitBreaker.requestVolumeThreshold",value = "10"),//10次请求中 CLOSE
            @HystrixProperty(name="circuitBreaker.errorThresholdPercentage",value = "60"),//有60%出错 超时则熔断 OPEN
            @HystrixProperty(name="circuitBreaker.sleepWindowInMilliseconds",value = "10000") //10秒后半开尝试 HALF-OPEN
    })
    public String crack(@RequestParam("uploadImg")MultipartFile file){

        VAILD_REQUEST_NUMBER.getAndIncrement();

        try {
            return verifyService.proccess(file.getBytes(),file.getOriginalFilename());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }
    /*降级方法*/

    public String fallBack(@RequestParam("uploadImg")MultipartFile file){
        //关闭线程
        String uuid=file.getOriginalFilename();
        String[] values=verifyService.threadUUIDMap.get(uuid);
        verifyService.consoleThreadMap.get(values[0]).interrupt();
        verifyService.consoleThreadMap.get(values[1]).interrupt();
        verifyService.threadUUIDMap.remove(uuid);
        return "服务器忙,请重试";
    }
    @PostMapping("/image")
    @ResponseBody
    @HystrixCommand(fallbackMethod = "fallBack2",commandProperties = {
            @HystrixProperty(name="circuitBreaker.enabled",value = "true"), //断路器使能
            @HystrixProperty(name="circuitBreaker.requestVolumeThreshold",value = "10"),//10次请求中 CLOSE
            @HystrixProperty(name="circuitBreaker.errorThresholdPercentage",value = "60"),//有60%出错 超时则熔断 OPEN
            @HystrixProperty(name="circuitBreaker.sleepWindowInMilliseconds",value = "10000") //10秒后半开尝试 HALF-OPEN
    })
    public String image(@RequestParam("uploadImg")MultipartFile file,@RequestParam("uuid") String uuid){

        VAILD_REQUEST_NUMBER.getAndIncrement();

        try {
            return verifyService.proccess(file.getBytes(),uuid);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }
    public String fallBack2(@RequestParam("uploadImg")MultipartFile file,@RequestParam("uuid") String uuid){

        String[] values=verifyService.threadUUIDMap.get(uuid);
        verifyService.consoleThreadMap.get(values[0]).interrupt();
        verifyService.consoleThreadMap.get(values[1]).interrupt();
        verifyService.threadUUIDMap.remove(uuid);
        return "服务器忙,请重试";
    }
    @Autowired
    RecordService recordService;
    @GetMapping("")
    public String home(HttpServletRequest request){
        String ip= HttpClient.getIPAddress(request);
        recordService.record(ip);
        return "home";
    }



    @PostMapping("/base64")
    @ResponseBody
    @HystrixCommand(fallbackMethod = "fallBack1",commandProperties = {
            @HystrixProperty(name="circuitBreaker.enabled",value = "true"), //断路器使能
            @HystrixProperty(name="circuitBreaker.requestVolumeThreshold",value = "10"),//10次请求中 CLOSE
            @HystrixProperty(name="circuitBreaker.errorThresholdPercentage",value = "60"),//有60%出错\超时则熔断 OPEN
            @HystrixProperty(name="circuitBreaker.sleepWindowInMilliseconds",value = "10000") //10秒后半开尝试 HALF-OPEN
    })
    public String base(@RequestParam("base64")String base64,@RequestParam("uuid") String uuid)  {
        VAILD_REQUEST_NUMBER.getAndIncrement();
        if(verifyService.isBase64(base64))
            return verifyService.proccess(Base64.getDecoder().decode(base64),uuid);
        return "base64 校验失败";
    }
    public String fallBack1(@RequestParam("base64")String base64,@RequestParam("uuid") String uuid){
        String[] values=verifyService.threadUUIDMap.get(uuid);
        verifyService.consoleThreadMap.get(values[0]).interrupt();
        verifyService.consoleThreadMap.get(values[1]).interrupt();
        verifyService.threadUUIDMap.remove(uuid);
        return "服务器忙,请重试";
    }
    @GetMapping("/ip")
    @ResponseBody
    public ConcurrentHashMap ip(){
        return recordService.map;
    }
    @GetMapping("/vrn")
    @ResponseBody
    public int getVaildRequestNumber(){
        return VAILD_REQUEST_NUMBER.get();
    }
}
