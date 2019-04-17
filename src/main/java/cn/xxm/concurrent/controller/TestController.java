package cn.xxm.concurrent.controller;

import cn.xxm.concurrent.service.TestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author xxm
 * @create 2019-01-07 23:07
 */
@RestController
public class TestController {

    @Autowired
    private TestService testService;

    @GetMapping("/hehe")
    public String test(){
        // 创建线程池的时候,一般根据服务器cpu个数来设置,线程池可以设置为8,或者2倍
        ExecutorService executorService = Executors.newFixedThreadPool(8*2);
        for (int i = 0; i <10000 ; i++) {
            executorService.submit(new Runnable() {
                @Override
                public void run() {
                    try {
                        String value = testService.doSomeServices();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        }

        return "呵呵哒";
    }

}
