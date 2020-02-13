# Java12306
Java调用tensorflow pb模型进行图像分类做验证码识别，其它基本图像处理如图片切割、reszie、类型转换等由java完成

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
在线识别Demo
=
[http://167.179.114.177:8080/](http://167.179.114.177:8080/)
<p align="left">
	<img src="https://github.com/vua/Java12306/blob/master/image/%E5%9C%A8%E7%BA%BF%E9%AA%8C%E8%AF%81%E5%9B%BE.png" alt="Sample"  width="300" height="500">
</p>
