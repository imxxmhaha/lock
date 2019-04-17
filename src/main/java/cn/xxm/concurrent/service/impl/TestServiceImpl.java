package cn.xxm.concurrent.service.impl;

import cn.xxm.concurrent.service.TestService;
import cn.xxm.concurrent.utils.ZookeeperLock;
import org.apache.commons.lang3.StringUtils;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.locks.InterProcessMutex;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

/**
 * @author xxm
 * @create 2019-04-17 19:32
 */
@Service
public class TestServiceImpl implements TestService {



    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    // 方式1
    //@Override
    //public String doSomeServices() {
    //    String value = redisTemplate.opsForValue().get("name");
    //
    //    if (StringUtils.isEmpty(value)) {  // 在高并发情况时,缓存穿透的一刹那,打个比方这里有10个线程走到了这一步
    //        synchronized (this) {  // 加锁,10线程中,只有第一个线程进入
    //            value = redisTemplate.opsForValue().get("name"); // 第一个线程查询,没有值  (当第一个线程跑完之后,另外9个线程,在这里可以获取到值了)
    //            if (StringUtils.isEmpty(value)) { // 第一个线程value没有值,走数据库 (另外9个线程进来时已经有值了,不会走数据库)
    //                try {
    //                    // 模拟数据库操作耗时200ms
    //                    Thread.sleep(200);
    //                    //查询数据库操作
    //                    value = "xxm"; // 模拟查库
    //                    System.out.println("查询数据库...");
    //                    redisTemplate.opsForValue().set("name", value);
    //                } catch (InterruptedException e) {
    //                    e.printStackTrace();
    //                }
    //            }
    //        }
    //    } else {
    //        System.out.println("查询缓存...");
    //    }
    //    return value;
    //}


    //// 方式2
    @Override
    public String doSomeServices() throws Exception {
        String value = redisTemplate.opsForValue().get("name");

        if (StringUtils.isEmpty(value)) {  // 在高并发情况时,缓存穿透的一刹那,打个比方这里有10个线程走到了这一步
            // 创建zookeeper的客户端
            RetryPolicy retryPolicy = new ExponentialBackoffRetry(10,3);
            CuratorFramework client = CuratorFrameworkFactory.newClient("192.168.200.128:2181",retryPolicy);
            client.start();

            // 创建分布式锁,锁空间的根节点路径为/curator/lock
            InterProcessMutex mutex = new InterProcessMutex(client,"/curator/lock");
            mutex.acquire();
            //System.out.println("获得了锁....");
            // 获得了锁,进行业务流程  10线程中,只有第一个线程进入
            value = redisTemplate.opsForValue().get("name"); // 第一个线程查询,没有值  (当第一个线程跑完之后,另外9个线程,在这里可以获取到值了)
            if (StringUtils.isEmpty(value)) { // 第一个线程value没有值,走数据库 (另外9个线程进来时已经有值了,不会走数据库)
                try {
                    // 模拟数据库操作耗时200ms
                    Thread.sleep(200);
                    //查询数据库操作
                    value = "xxm"; // 模拟查库
                    System.out.println("查询数据库...");
                    redisTemplate.opsForValue().set("name", value);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            // 完成业务流程,释放锁
            mutex.release();
            // 关闭客户端
            client.close();
        } else {
            System.out.println("查询缓存...");
        }
        return value;
    }


    //// 方式2
    //@Override
    //public String doSomeServices() throws Exception {
    //    String value = redisTemplate.opsForValue().get("name");
    //
    //    if (StringUtils.isEmpty(value)) {  // 在高并发情况时,缓存穿透的一刹那,打个比方这里有10个线程走到了这一步
    //        // 获得锁
    //        ZookeeperLock.tryGetLock();
    //
    //        System.out.println("获得了锁....");
    //        // 获得了锁,进行业务流程  10线程中,只有第一个线程进入
    //        value = redisTemplate.opsForValue().get("name"); // 第一个线程查询,没有值  (当第一个线程跑完之后,另外9个线程,在这里可以获取到值了)
    //        if (StringUtils.isEmpty(value)) { // 第一个线程value没有值,走数据库 (另外9个线程进来时已经有值了,不会走数据库)
    //            try {
    //                // 模拟数据库操作耗时200ms
    //                Thread.sleep(200);
    //                //查询数据库操作
    //                value = "xxm"; // 模拟查库
    //                System.out.println("查询数据库...");
    //                redisTemplate.opsForValue().set("name", value);
    //            } catch (InterruptedException e) {
    //                e.printStackTrace();
    //            }
    //        }
    //
    //        // 释放锁
    //        ZookeeperLock.releaseLock();
    //    } else {
    //        System.out.println("查询缓存...");
    //    }
    //    return value;
    //}
}
