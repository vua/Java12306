package com.cooooode.verify;



/**
 * @program: verify
 * @description:
 * @author: vua
 * @create: 2020-02-16 18:58
 */
public class ThreadDemo {
    static class Inner{
        static  final String i="";
        static {
            System.out.println("初始化");
        }
    }
    public static void main(String[] args) {
        System.out.println(Inner.i);
        /*
        Thread thread=new Thread(new Runnable() {
            @Override
            public void run() {
                while (!Thread.currentThread().isInterrupted()){

                }
                System.out.println("线程中断");
                System.out.println(Thread.interrupted());
                System.out.println("恢复中断");
                while (!Thread.currentThread().isInterrupted()){

                }
            }
        },"testThread");
        thread.start();
        thread.interrupt();

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println(thread.isInterrupted());
        */
    }
}
