package com.hmdp;

import com.hmdp.utils.RedisIdWorker;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@SpringBootTest
class HmDianPingApplicationTests {

    @Resource
    private RedisIdWorker redisIdWorker;

    private ExecutorService POOL = Executors.newFixedThreadPool(300);

    @Test
    public void IDTest() throws InterruptedException {

        CountDownLatch latch = new CountDownLatch(300);
        Runnable task = () ->{
            for (int i = 0; i < 100; i++) {
                System.out.println(redisIdWorker.nextID("shop"));
            }
            latch.countDown();
        };
        for (int i = 0; i < 300; i++) {
            POOL.submit(task);
        }

        long begin = System.currentTimeMillis();

        latch.await();
        long end = System.currentTimeMillis();

        System.out.println(end -begin);

    }
}
