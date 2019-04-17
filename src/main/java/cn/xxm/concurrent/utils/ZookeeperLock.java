package cn.xxm.concurrent.utils;

import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.locks.InterProcessMutex;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author xxm
 * @create 2019-04-17 21:40
 */
public class ZookeeperLock {
    private static ConcurrentHashMap<String,Object> targetMap = new ConcurrentHashMap<>();

    public static void tryGetLock(){
        // 创建zookeeper的客户端
        RetryPolicy retryPolicy = new ExponentialBackoffRetry(10,3);
        CuratorFramework client = CuratorFrameworkFactory.newClient("192.168.200.128:2181",retryPolicy);
        targetMap.put("client",client);
        client.start();

        // 创建分布式锁,锁空间的根节点路径为/curator/lock
        InterProcessMutex  mutex = new InterProcessMutex(client,"/curator/lock");
        targetMap.put("mutex",mutex);
        try {
            mutex.acquire();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void releaseLock(){
        // 完成业务流程,释放锁
        try {
            InterProcessMutex mutex = (InterProcessMutex) targetMap.remove("mutex");
            mutex.release();
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            // 关闭客户端
            CuratorFramework client = (CuratorFramework) targetMap.remove("client");
            client.close();
        }

    }
}
