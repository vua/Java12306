package com.vua.grab;

import com.alibaba.fastjson.JSON;
import okhttp3.*;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeDriverService;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.remote.service.DriverService;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main {
    private static final Logger log = LoggerFactory.getLogger(Main.class);
    static final int[][] coordinate = {{-105, -20}, {-35, -20}, {40, -20}, {110, -20}, {-105, 50}, {-35, 50}, {40, 50}, {110, 50}};

    private static ChromeDriver driver;

    static {
        System.setProperty("webdriver.chrome.driver", "C:\\Program Files (x86)\\Google\\Chrome\\Application\\chromedriver.exe");

        driver = new ChromeDriver();
        driver.executeScript(
                " Object.defineProperty(navigator, 'webdriver', {get: () => undefined }) ");
    }

    public static void main(String[] args) {

        //85.0.4183.102
        login("531218020@qq.com", "****");

    }

    public static void login(String userName, String password) {
        driver.get("https://kyfw.12306.cn/otn/resources/login.html");
        WebElement accountBtn = driver.findElement(By.xpath("/html/body/div[2]/div[2]/ul/li[2]"));
        accountBtn.click();
        WebElement userNameInp = driver.findElement(By.xpath("//*[@id=\"J-userName\"]"));
        userNameInp.sendKeys(userName);
        WebElement passwordInp = driver.findElement(By.xpath("/html/body/div[2]/div[2]/div[1]/div[2]/div[2]/input"));
        passwordInp.sendKeys(password);

        WebElement loginImg = null;
        String[] srcSegs = null;
        do {
            loginImg = driver.findElement(By.xpath("/html/body/div[2]/div[2]/div[1]/div[2]/div[3]/div/div[4]/img"));
            String src = loginImg.getAttribute("src");
            srcSegs = src.split(",");
        } while (srcSegs == null || srcSegs.length != 2);
        String base64 = srcSegs[1];
        List<Integer> hits = parseResponse(verify(base64));
        Actions action = new Actions(driver);


        for (int hit : hits) {
            action.moveToElement(loginImg)
                    .moveByOffset(coordinate[hit][0], coordinate[hit][1]).click().perform();
        }

        WebElement loginBtn = driver.findElement(By.xpath("/html/body/div[2]/div[2]/div[1]/div[2]/div[5]/a"));
        action.moveToElement(loginBtn).moveByOffset(5, 5).click().perform();
        driver.manage().timeouts().implicitlyWait(2, TimeUnit.SECONDS);
        WebElement slideBtn = driver.findElement(By.xpath("/html/body/div[5]/div[2]/div/div[3]/div/div[1]/span"));


        action.dragAndDropBy(slideBtn, 300, 0).perform();
    }

    private static final String HTTP_JSON = "application/json; charset=utf-8";
    private static final OkHttpClient okHttpClient = new OkHttpClient.Builder()
            .connectTimeout(120, TimeUnit.SECONDS)
            .readTimeout(120, TimeUnit.SECONDS)
            .writeTimeout(120, TimeUnit.SECONDS)
            .build();

    public static String verify(String base64) {
        String url = "http://www.dill.fun/base64";
        MediaType JSON = MediaType.parse(HTTP_JSON);
        Map<String, String> map = new HashMap<>();
        map.put("base64", base64);
        map.put("uuid", UUID.randomUUID().toString());
        String json = com.alibaba.fastjson.JSON.toJSONString(map);
        System.out.println(json);

        RequestBody body = new MultipartBody.Builder()
                .addFormDataPart("base64", base64)
                .addFormDataPart("uuid", map.get("uuid")).build();
        Request request = new Request.Builder().url(url).post(body).build();
        try {
            Response response = okHttpClient.newCall(request).execute();
            if (response.code() == 200) {
                log.info("http Post 请求成功; [url={}, requestContent={}]", url, json);
                return response.body().string();
            } else {
                log.warn("Http POST 请求失败; [ errorCode = {}, url={}, param={}]", response.code(), url, json);
                return verify(base64);
            }
        } catch (IOException e) {
            throw new RuntimeException("同步http请求失败,url:" + url, e);
        }

    }

    static Pattern p = Pattern.compile("<span>(.*)</span>");

    public static List<Integer> parseResponse(String response) {
        System.out.println(response);
        String[] segments = response.split("\n");
        String[] words = segments[0].split("\t");

        String[] images = segments[1].split("\t");

        Set<String> labels = new HashSet<>();

        List<Integer> hits = new ArrayList<>();
        for (String word : words) {

            Matcher m = p.matcher(word);
            while (m.find()) {
                labels.add(m.group(1));
            }
        }
        for (int i = 0; i < 8; i++) {
            Matcher m = p.matcher(images[i]);
            while (m.find()) {
                String clazz = m.group(1);
                if (labels.contains(clazz)) {
                    hits.add(i);
                }
            }
        }
        return hits;
    }


    //public static
}
