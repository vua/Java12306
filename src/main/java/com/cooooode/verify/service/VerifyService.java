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
import java.util.concurrent.*;
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

    static final LabelImage labelImage = new LabelImage();
    static List<String> labels;
    static byte[] graphMobileNet;
    static byte[] graphLenet;
    static final ThreadGroup group = new ThreadGroup("tensorflow");

    static final ExecutorService pool = new ThreadPoolExecutor(0, 1 << 16, 60, TimeUnit.SECONDS,
            new SynchronousQueue<Runnable>());

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
    public void init() throws InterruptedException {

        proccess(ImageProcessor.imageToBytes(new BufferedImage(300, 200, BufferedImage.TYPE_INT_RGB)),"");
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

    /*class PicCallable implements Callable<String> {
        private BufferedImage[] imgs;

        PicCallable(BufferedImage[] imgs) {
            this.imgs = imgs;
        }

        @Override
        public String call() throws Exception {
            StringBuilder response = new StringBuilder();
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
            return response.toString();
        }
    }*/

    class PicThread extends Thread {
        private BufferedImage[] imgs;
        private StringBuilder response;
        private CountDownLatch latch;

        PicThread(BufferedImage[] imgs, StringBuilder response, CountDownLatch latch) {
            super(group, "img");
            this.imgs = imgs;
            this.response = response;
            this.latch = latch;
        }

        @Override
        public void run() {
            response.append("图片从左到右从上到下依次是:<br>");
            if (imgs != null && imgs.length > 0) {
                for (int i = 1; i < imgs.length; i++) {
                    if(Thread.currentThread().isInterrupted()) {
                        latch.countDown();
                        return;
                    }
                    response.append("<span>"+predictPicClasses(ImageProcessor.imageToBytes(imgs[i])) + "</span>\t");
                    if (i == 4)
                        response.append("<br>");
                }
            } else {
                response.append("image handling exception");
            }
            response.append("<br>");
            latch.countDown();
        }
    }

    /*class TopCallable implements Callable<String> {
        private BufferedImage top;

        TopCallable(BufferedImage top) {
            this.top = top;
        }

        @Override
        public String call() throws Exception {
            StringBuilder response = new StringBuilder();
            response.append("标签从左到右依次是:<br>");
            BufferedImage[] imgs = ImageProcessor.cutAgain(top);
            if (imgs != null && imgs.length > 0) {
                for (int i = 0; i < imgs.length; i++) {
                    if (imgs[i] != null) {
                        response.append("<span>"+predictTopClasses(ImageProcessor.imageToBytes(imgs[i])) + "</span>\t");
                    }
                }
            } else {
                response.append("image handling exception");
            }
            response.append("<br>");
            return response.toString();
        }
    }*/

    class TopThread extends Thread {
        private BufferedImage top;
        private StringBuilder response;
        //private String name;
        private CountDownLatch latch;

        TopThread(BufferedImage top, StringBuilder response, CountDownLatch latch) {
            super(group, "top");
            this.top = top;
            this.response = response;
            this.latch = latch;
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

            response.append("标签从左到右依次是:<br>");
            BufferedImage[] imgs = ImageProcessor.cutAgain(top);
            if (imgs != null && imgs.length > 0) {
                for (int i = 0; i < imgs.length; i++) {
                    if(Thread.currentThread().isInterrupted()) {
                        latch.countDown();
                        return;
                    }
                    if (imgs[i] != null) {
                        response.append("<span>"+predictTopClasses(ImageProcessor.imageToBytes(imgs[i])) + "</span>\t");
                    }
                }
            } else {
                response.append("image handling exception");
            }
            response.append("<br>");
            latch.countDown();

        }
    }
    public HashMap<String,String[]> threadUUIDMap=new HashMap<>();
    public HashMap<String,Thread> consoleThreadMap=new HashMap<>();
    public String proccess(byte[] img,String uuid)  {
        BufferedImage[] imgs = ImageProcessor.cutImage(img);
        /*String uuid = UUID.randomUUID().toString().replaceAll("-", "");
        try {
            ImageIO.write(imgs[0], "jpg", new File(uuid + ".jpg"));
        } catch (IOException e) {
            e.printStackTrace();
        }*/

        CountDownLatch latch = new CountDownLatch(2);
        StringBuilder response1 = new StringBuilder();
        Thread picThread=new PicThread(imgs, response1,latch);
        String picThreadUUID=UUID.randomUUID().toString();
        consoleThreadMap.put(picThreadUUID,picThread);
//        picThread.start();
        StringBuilder response2 = new StringBuilder();
        Thread topThread=new TopThread(imgs[0], response2,latch);
        String topThreadUUID=UUID.randomUUID().toString();
        consoleThreadMap.put(topThreadUUID,topThread);
//        topThread.start();
        threadUUIDMap.put(uuid,new String[]{picThreadUUID,topThreadUUID});

        pool.execute(picThread);
        pool.execute(topThread);

        try {
            latch.await();
        } catch (InterruptedException e) {

        }


        threadUUIDMap.remove(uuid);
        return response2.toString() + "\n" + response1.toString();
    }
}
