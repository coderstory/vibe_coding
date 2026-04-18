package cn.coderstory.springboot.test;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.locks.ReentrantLock;

public class MyThread {
    private static ThreadPoolExecutor service = (ThreadPoolExecutor) Executors.newFixedThreadPool(7);

    public static void main(String[] args) throws ExecutionException, InterruptedException {

        service.execute(() -> {
            // 同步调用，放到队列的第一个，然后执行，会抛出异常
            throw new RuntimeException("execute");
        });
        Future<Object> submit = service.submit(() -> {
            throw new RuntimeException("submit");
        });
        try {
            // 需要get方法才能拿到异常，不会自动打印日常
            submit.get();
        } catch (InterruptedException | ExecutionException e) {

        }

        // 线程池需要手动关闭 一般写到 try-resources里
        service.close();
        FutureTask<String> aaa = new FutureTask<>(()->{
            System.out.printf("xxxx");
            return "asaa";
        });
        new Thread(aaa).start();
       System.out.println( aaa.get());
    }

    public static void main2() {

        // 可以锁一个对象
        synchronized (MyThread.class){

        }
        // 也可以锁一段代码（的返回值）
        synchronized (System.out.printf("hello")){

        }

        ReentrantLock lock =  new ReentrantLock();

    }





}
