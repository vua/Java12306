package com.cooooode.verify.util;

/**
 * @program: verify
 * @description:
 * @author: vua
 * @create: 2020-04-27 10:42
 */

import java.net.*;
import java.io.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Crawling {
    static String LOGIN_URL = "https://kyfw.12306.cn/passport/captcha/captcha-image64?login_site=E&module=login&rand=sjrand&1587955704337&callback=jQuery191010038045267657902_1587955637794&_=";
    static final Pattern pattern = Pattern.compile("^.*\"image\"\\:\"(.*)\",\"result_message\".*$");

    public static String getVerifyImageBase64() {
        URL url = null;
        try {
            url = new URL(LOGIN_URL+System.currentTimeMillis());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        String base64 = null;
        try (
                BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()));
        ) {
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
                Matcher m = pattern.matcher(line);
                while (m.matches()) {
                    base64 = m.group(1);
                    return base64;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;

    }

    public static void main(String[] args) {
        System.out.println(getVerifyImageBase64());
    }

}
