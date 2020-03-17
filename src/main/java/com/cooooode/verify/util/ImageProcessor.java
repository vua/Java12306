package com.cooooode.verify.util;

//import org.opencv.core.*;

//import org.opencv.imgcodecs.Imgcodecs;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
//import java.awt.image.DataBufferByte;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
//import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
//import java.util.UUID;
/*

import static org.opencv.core.Core.REDUCE_AVG;
import static org.opencv.imgproc.Imgproc.COLOR_RGB2GRAY;
*/

/**
 * @program: verify
 * @description:
 * @author: vua
 * @create: 2020-02-07 15:28
 */
public class ImageProcessor {
    static {
        //System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }
    /*切图坐标*/
    static final ArrayList<int[]> position = new ArrayList() {{
        add(new int[]{0, 30, 120, 290});
        add(new int[]{41, 108, 5, 73});
        add(new int[]{41, 108, 77, 145});
        add(new int[]{41, 108, 149, 217});
        add(new int[]{41, 108, 221, 289});
        add(new int[]{113, 180, 5, 73});
        add(new int[]{113, 180, 77, 145});
        add(new int[]{113, 180, 149, 217});
        add(new int[]{113, 180, 221, 289});
    }};
    /*切图*/
    public static BufferedImage[] cutImage(byte[] byteImg) {
        BufferedImage imgs[]=null;
        try (
                ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(byteImg)) {
            BufferedImage image = ImageIO.read(byteArrayInputStream);

            int chunks = 9;
            imgs = new BufferedImage[chunks];
            Image o = image.getScaledInstance(image.getWidth(),
                    image.getHeight(), Image.SCALE_SMOOTH);

            int[] temp;
            for (int i = 0; i < position.size(); i++) {
                temp = position.get(i);
                int h = temp[1] - temp[0];//x
                int w = temp[3] - temp[2];//y

                imgs[i] = new BufferedImage(w, h, image.getType());
                Graphics2D gr = imgs[i].createGraphics();
                gr.drawImage(o, 0, 0, w, h,
                        temp[2], temp[0], temp[3], temp[1], null);
                gr.dispose();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        return imgs;
    }
    /**
     * BufferedImage转byte[]
     *
     * @param bImage BufferedImage对象
     * @return byte[]
     * @auth zhy
     */
    public static byte[] imageToBytes(BufferedImage bImage) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            ImageIO.write(bImage, "png", out);
        } catch (IOException e) {
            //log.error(e.getMessage());
        }
        return out.toByteArray();
    }
    /*public static Mat BufImg2Mat (BufferedImage original, int imgType, int matType) {
        if (original == null) {
            throw new IllegalArgumentException("original == null");
        }

        // Don't convert if it already has correct type
        if (original.getType() != imgType) {

            // Create a buffered image
            BufferedImage image = new BufferedImage(original.getWidth(), original.getHeight(), imgType);

            // Draw the image onto the new buffer
            Graphics2D g = image.createGraphics();
            try {
                g.setComposite(AlphaComposite.Src);
                g.drawImage(original, 0, 0, null);
            } finally {
                g.dispose();
            }
        }

        byte[] pixels = ((DataBufferByte) original.getRaster().getDataBuffer()).getData();
        Mat mat = new Mat(original.getHeight(), original.getWidth(), matType);
        mat.put(0, 0, pixels);
        return mat;
    }
    public static byte[] mat2Byte(Mat matrix) {
        MatOfByte mob = new MatOfByte();
        Imgcodecs.imencode(".jpg", matrix, mob);
        byte[] byteArray = mob.toArray();
        return byteArray;
    }

    public static void cutAgain(BufferedImage top){
        Mat mat=BufImg2Mat(top,BufferedImage.TYPE_3BYTE_BGR, CvType.CV_8UC3);
        Mat avg=new Mat(1,mat.cols(),CvType.CV_64F,new Scalar(0));
        Core.reduce(mat,avg,0,REDUCE_AVG);
        int maxIndex=0;
        double maxVal=0;
        for(int i=30;i<70;i++){
            if(avg.get(0,i)[0]>maxVal){
                maxVal=avg.get(0,i)[0];
                maxIndex=i;
            }
        }
        ArrayList<byte[]> imgs=new ArrayList<>();
        if(maxIndex<60){
            Mat out=new Mat(30,60,CvType.CV_8UC3,new Scalar(255));

            for(int i=0;i<30;i++){
                for(int j=0;j<maxIndex;j++) {
                    out.put(i,j,mat.get(i,j));
                }
            }
            Imgcodecs.imwrite("test-opencv-java.jpg",out);
        }
    }*/
    /*字符分割*/
    public static BufferedImage[] cutAgain(BufferedImage top){
        Image o = top.getScaledInstance(top.getWidth(),
                top.getHeight(), Image.SCALE_SMOOTH);
        int[] sums=new int[top.getWidth()];
        int index=0;
        int sum=0;
        int max=0;
        for(int i=30;i<70;i++){
            sum=0;
            for(int j=0;j<top.getHeight();j++){
                sum+=top.getRGB(i,j)&0xff;
            }
            if(max<sum)
            {
                max=sum;
                index=i;
            }
        }

        if(index>60) index=60;
        BufferedImage[] images=new BufferedImage[2];
        BufferedImage img = new BufferedImage(60,30 , top.getType());
        Graphics2D gr = img.createGraphics();
        gr.setBackground(new Color(255,255,255));
        gr.fillRect(index, 0, 60-index, 30);

        gr.drawImage(o, 0, 0, index, 30,
                0,0,index,30, null);
        gr.dispose();
        images[0]=img;
        int idx=0;
        for(int i=index+30;i<index+70;i++){
            sum=0;
            for(int j=0;j<top.getHeight();j++){
                sum+=top.getRGB(i,j)&0xff;
            }
            if(max<sum)
            {
                max=sum;
                idx=i;
            }
        }
        /*try {
            ImageIO.write(img,"jpg",new File("test.jpg"));
        } catch (IOException e) {
            e.printStackTrace();
        }*/
        if(idx-index<10) return images;
        int diff=idx-index;

        BufferedImage img1 = new BufferedImage(60,30 , top.getType());
        Graphics2D gr1 = img1.createGraphics();
        gr1.setBackground(new Color(255,255,255));
        gr1.fillRect(diff, 0, 60-diff, 30);

        gr1.drawImage(o, 0, 0, diff, 30,
                index,0,idx,30, null);
        gr1.dispose();
        images[1]=img1;
       /* try {
            ImageIO.write(img1,"jpg",new File("test1.jpg"));
        } catch (IOException e) {
            e.printStackTrace();
        }*/
        return images;
        //threshold(img,100);


    }
    /*阈值分割*/
    public static void threshold(BufferedImage image,int val){

        for(int i=0;i<image.getWidth();i++){
            for(int j=0;j<image.getHeight();j++){
                if((image.getRGB(i,j)&0xff)>val){
//                    System.out.println(image.getRGB(i,j)&0xff);
                    image.setRGB(i,j,0x00ffffff);
                }else{

                    image.setRGB(i,j,0x0);
                }
            }
        }
    }
}
