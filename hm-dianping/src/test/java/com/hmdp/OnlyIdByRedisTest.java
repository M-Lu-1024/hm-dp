package com.hmdp;

import com.hmdp.utils.RedisIdWorker;
import org.junit.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@SpringBootTest(classes = HmDianPingApplication.class)
public class OnlyIdByRedisTest {
    @Resource
    private RedisIdWorker redisIdWorker;

    private ExecutorService POOL = Executors.newFixedThreadPool(100);

    @Test
    public void IDTest() throws InterruptedException {
//
//        CountDownLatch latch = new CountDownLatch(100);
//        Runnable task = () ->{
//            for (int i = 0; i < 100; i++) {
//                System.out.println(onlyIdByRedis.nextID("shop"));
//            }
//            latch.countDown();
//        };
//        for (int i = 0; i < 100; i++) {
//            POOL.submit(task);
//        }
//        System.out.println("begin");
//        latch.await();
//        System.out.println("end");

         for (int i = 0; i < 100; i++) {
                System.out.println(redisIdWorker.nextID("shop"));
            }
    }
}
