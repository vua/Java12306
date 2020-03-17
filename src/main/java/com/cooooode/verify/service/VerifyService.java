package com.cooooode.verify.service;

import com.cooooode.verify.util.ImageProcessor;
import com.cooooode.verify.util.LabelImage;
//import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Service;
import org.tensorflow.Tensor;

import javax.annotation.PostConstruct;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Pattern;

/**
 * @program: verify
 * @description:
 * @author: vua
 * @create: 2020-02-07 14:03
 */
@Service
public class VerifyService {


    public static int init = 0;
    static final String modelDir = VerifyService.class.getClassLoader().getResource("model").toString();

    static LabelImage labelImage = new LabelImage();
    static List<String> labels;
    static byte[] graphMobileNet;
    static byte[] graphLenet;
    static ThreadGroup group = new ThreadGroup("tensorflow");

    static {
//        graphMobileNet = labelImage.readAllBytesOrExit(Paths.get(modelDir, "train-mobilenet-pic.pb"));
//        graphLenet = labelImage.readAllBytesOrExit(Paths.get(modelDir, "train-lenet-top.pb"));
//        labels = LabelImage.readAllLinesOrExit(Paths.get(modelDir, "label.txt"));

        try (
                ///usr/java/project/verify/src/main/resources/model/mobilenet-pic.pb
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

    @PostConstruct
    public void init() {

        proccess(ImageProcessor.imageToBytes(new BufferedImage(300, 200, BufferedImage.TYPE_INT_RGB)));
    }

    public String predictPicClasses(byte[] imageBytes) {
        //byte[] imageBytes = LabelImage.readAllBytesOrExit(Paths.get(""));

        try (Tensor<Float> image = LabelImage.constructAndExecuteGraphToNormalizeImage(imageBytes, 67, 68, 1)) {

            float[] labelProbabilities = LabelImage.executeInceptionGraph(graphMobileNet, image, "input_1", "act_softmax/Softmax");
            int bestLabelIdx = LabelImage.maxIndex(labelProbabilities);
            return labels.get(bestLabelIdx);
            /*System.out.println(
                    String.format("BEST MATCH: %s (%.2f%% likely)",
                            labels.get(bestLabelIdx),
                            labelProbabilities[bestLabelIdx] * 100f));*/
        }
    }

    public String predictTopClasses(byte[] imageBytes) {
        try (Tensor<Float> image = LabelImage.constructAndExecuteGraphToNormalizeImage(imageBytes, 30, 60, 1)) {

            float[] labelProbabilities = LabelImage.executeInceptionGraph(graphLenet, image, "conv2d_1_input", "dense_2/Softmax");
            int bestLabelIdx = LabelImage.maxIndex(labelProbabilities);
            return labels.get(bestLabelIdx);
            /*System.out.println(
                    String.format("BEST MATCH: %s (%.2f%% likely)",
                            labels.get(bestLabelIdx),
                            labelProbabilities[bestLabelIdx] * 100f));*/
        }
    }

    public boolean isBase64(String str) {
        String base64Pattern = "^([A-Za-z0-9+/]{4})*([A-Za-z0-9+/]{4}|[A-Za-z0-9+/]{3}=|[A-Za-z0-9+/]{2}==)$";
        return Pattern.matches(base64Pattern, str);
    }

    class PicThread extends Thread {
        private BufferedImage[] imgs;
        private StringBuilder response;

        PicThread(BufferedImage[] imgs, StringBuilder response) {
            super(group, "img");
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

    class TopThread extends Thread {
        private BufferedImage top;
        private StringBuilder response;
        //private String name;

        TopThread(BufferedImage top, StringBuilder response) {
            super(group, "top");
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

    public String proccess(byte[] img) {
        BufferedImage[] imgs = ImageProcessor.cutImage(img);
        /*String uuid = UUID.randomUUID().toString().replaceAll("-", "");
        try {
            ImageIO.write(imgs[0], "jpg", new File(uuid + ".jpg"));
        } catch (IOException e) {
            e.printStackTrace();
        }*/

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
            //e.printStackTrace();
        }
        return response2.toString() + "\n" + response1.toString();
    }
}
