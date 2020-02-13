package com.cooooode.java12306.service;

import com.cooooode.java12306.util.ImageProcessor;
import com.cooooode.java12306.util.LabelImage;
import org.tensorflow.Tensor;
import org.tensorflow.TensorFlow;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.Base64;
import java.util.List;
import java.util.regex.Pattern;

/**
 * @program: verify
 * @description:
 * @author: vua
 * @create: 2020-02-07 14:03
 */

public class VerifyService {
    private static final String modelDir = VerifyService.class.getClassLoader().getResource("model").toString();

    private static LabelImage labelImage = new LabelImage();
    private static List<String> labels;
    private static byte[] graphMobileNet;
    private static byte[] graphLenet;

    static {
//        graphMobileNet = labelImage.readAllBytesOrExit(Paths.get(modelDir, "train-mobilenet-pic.pb"));
//        graphLenet = labelImage.readAllBytesOrExit(Paths.get(modelDir, "train-lenet-top.pb"));
//        labels = LabelImage.readAllLinesOrExit(Paths.get(modelDir, "label.txt"));

        try (
                InputStream mobilenet = VerifyService.class.getClassLoader().getResourceAsStream("model/mobilenet-pic.pb");
                InputStream lenet = VerifyService.class.getClassLoader().getResourceAsStream("model/lenet-top.pb");
                InputStream label = VerifyService.class.getClassLoader().getResourceAsStream("model/label.txt");
        ) {
            graphMobileNet = labelImage.readAllBytesOrExit(mobilenet);
            graphLenet = labelImage.readAllBytesOrExit(lenet);
            labels = LabelImage.readAllLinesOrExit(label);
        } catch (IOException e) {

            e.printStackTrace();
        }


    }

    private static String predictPicClasses(byte[] imageBytes) {
        //byte[] imageBytes = LabelImage.readAllBytesOrExit(Paths.get(""));

        try (Tensor<Float> image = LabelImage.constructAndExecuteGraphToNormalizeImage(imageBytes, 67, 68)) {

            float[] labelProbabilities = LabelImage.executeInceptionGraph(graphMobileNet, image, "input_1", "act_softmax/Softmax");
            int bestLabelIdx = LabelImage.maxIndex(labelProbabilities);
            return labels.get(bestLabelIdx);
            /*System.out.println(
                    String.format("BEST MATCH: %s (%.2f%% likely)",
                            labels.get(bestLabelIdx),
                            labelProbabilities[bestLabelIdx] * 100f));*/
        }
    }

    private static String predictTopClasses(byte[] imageBytes) {
        try (Tensor<Float> image = LabelImage.constructAndExecuteGraphToNormalizeImage(imageBytes, 30, 60)) {

            float[] labelProbabilities = LabelImage.executeInceptionGraph(graphLenet, image, "conv2d_1_input", "dense_2/Softmax");
            int bestLabelIdx = LabelImage.maxIndex(labelProbabilities);
            return labels.get(bestLabelIdx);
            /*System.out.println(
                    String.format("BEST MATCH: %s (%.2f%% likely)",
                            labels.get(bestLabelIdx),
                            labelProbabilities[bestLabelIdx] * 100f));*/
        }
    }

    private static boolean isBase64(String str) {
        String base64Pattern = "^([A-Za-z0-9+/]{4})*([A-Za-z0-9+/]{4}|[A-Za-z0-9+/]{3}=|[A-Za-z0-9+/]{2}==)$";
        return Pattern.matches(base64Pattern, str);
    }

    private static class PicThread extends Thread {
        private BufferedImage[] imgs;
        private StringBuilder response;

        PicThread(BufferedImage[] imgs, StringBuilder response) {
            this.imgs = imgs;
            this.response = response;
        }

        @Override
        public void run() {
            response.append("图片从左到右从上到下依次是:\n");
            if (imgs != null && imgs.length > 0) {
                for (int i = 1; i < imgs.length; i++) {

                    response.append(predictPicClasses(ImageProcessor.imageToBytes(imgs[i])) + "\t");
                    if (i == 4)
                        response.append("\n");
                }
            } else {
                response.append("image handling exception");
            }
            response.append("\n");
        }
    }

    private static class TopThread extends Thread {
        private BufferedImage top;
        private StringBuilder response;
        //private String name;

        TopThread(BufferedImage top, StringBuilder response) {
            this.top = top;
            this.response = response;
            //this.name = name;
        }

        @Override
        public void run() {
            /*System.out.println("top thread");
            String[] arguments = new String[] {"python", "/usr/java/project/verify/img.py",name};
            Process process = null;
            try {
                process = Runtime.getRuntime().exec(arguments);
            } catch (IOException e) {
                e.printStackTrace();
            }
            System.out.println("top stop")*/
            ;
            response.append("标签从左到右依次是:\n");
            BufferedImage[] imgs = ImageProcessor.cutAgain(top);
            if (imgs != null && imgs.length > 0) {
                for (int i = 0; i < imgs.length; i++) {
                    if (imgs[i] != null) {
                        response.append(predictTopClasses(ImageProcessor.imageToBytes(imgs[i])) + "\t");
                    }
                }
            } else {
                response.append("image handling exception");
            }
            response.append("\n");

        }
    }
    private static String same(BufferedImage[] imgs){
        StringBuilder response1 = new StringBuilder();
        StringBuilder response2 = new StringBuilder();
        PicThread picThread = new PicThread(imgs, response1);
        TopThread topThread = new TopThread(imgs[0], response2);
        picThread.start();
        topThread.start();
        try {
            picThread.join();
            topThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return response2.toString() + "\n" + response1.toString();
    }
    private static String process(BufferedImage image){
        BufferedImage[] imgs = ImageProcessor.cutImage(image);
        return same(imgs);
    }
    private static String process(byte[] img) {
        BufferedImage[] imgs = ImageProcessor.cutImage(img);
        /*String uuid = UUID.randomUUID().toString().replaceAll("-", "");
        try {
            ImageIO.write(imgs[0], "jpg", new File(uuid + ".jpg"));
        } catch (IOException e) {
            e.printStackTrace();
        }*/

        return same(imgs);
    }
    private static void printUsage(PrintStream s) {
        s.println("Usage: java -jar java12306.jar image image_path");
        s.println("       java -jar java12306.jar base64 base64_code");
    }
    public static void main(String[] args) {
        printUsage(System.err);

        if(args.length!=2){
            System.err.println("参数数目错误,请查看Usage");
            System.exit(0);
        }
        String type=args[0];
        String value=args[1];
        switch (type){
            case "image":{
                File file=new File(value);
                if(!file.exists()){
                    System.err.println("文件不存在");
                    System.exit(0);
                }
                BufferedImage image=null;
                try {
                    image=ImageIO.read(file);
                } catch (IOException e) {
                    System.err.println("文件读取失败");
                    System.exit(0);
                }
                if(image!=null)
                    System.out.println(process(image));

            }break;
            case "base64":{
                if(!VerifyService.isBase64(value)){
                    System.err.println("base64 code错误");
                    System.exit(0);
                }
                System.out.println(process(Base64.getDecoder().decode(value)));
            }break;
            default:{
                System.err.println("参数值错误,请查看Usage");
                System.exit(0);
            }
        }

    }
}
