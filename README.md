# Java12306
Java调用tensorflow pb模型进行图像分类做验证码识别，其它基本图像处理由java完成

模型.pb文件
=
模型文件已上传可以直接clone

  - 字符分类使用lenet(src/main/resource/model/lennt-top.pb)   
  
  - 图片分类使用mobilenet(src/main/resource/model/mobilenet-pic.pb)

maven依赖
=
```
<!-- https://mvnrepository.com/artifact/org.tensorflow/tensorflow -->
<dependency>
    <groupId>org.tensorflow</groupId>
    <artifactId>tensorflow</artifactId>
    <version>1.14.0</version>
</dependency>
```
使用方法
=
  - 执行 com.cooooode.java12306.service.VerifyService 的mian方法
  
  ```
  Usage: java VerifyService image image_path
      or java VerifyService base64 base64_code
 ```
Online Demo
=
[demo演示地址](http://www.dill.fun/)

[demo源码地址](https://github.com/vua/Java12306/tree/web)
<p align="left">
	<img src="https://github.com/vua/Java12306/blob/master/image/dill.fun.png" alt="Sample" >
</p>
