package com.yupi.yupao.service;

import com.baomidou.mybatisplus.annotation.TableField;
import com.yupi.yupao.mapper.UsersMapper;
import com.yupi.yupao.model.domain.Users;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.util.StopWatch;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

@SpringBootTest
public class InsertUserTest {

    @Resource
    private UsersService usersService;

    private ExecutorService executorService=new ThreadPoolExecutor(20,1000,10000, TimeUnit.MINUTES,new ArrayBlockingQueue<>(10000));
    /**
     * 批量插入用户
     */
    @Test
    public void doInsertUser(){
        StopWatch stopWatch=new StopWatch();
        stopWatch.start();
        final int INSERT_NUM=1000;
        List<Users> list=new ArrayList<>();
        for (int i = 0; i < INSERT_NUM; i++) {
            Users users=new Users();

            users.setUsername("假用户");
            users.setUserAccount("fakeyupi");
            users.setAvatarUrl("https://ts1.cn.mm.bing.net/th/id/R-C.57384e4c2dd256a755578f00845e60af?rik=uy9%2bvT4%2b7Rur%2fA&riu=http%3a%2f%2fimg06file.tooopen.com%2fimages%2f20171224%2ftooopen_sy_231021357463.jpg&ehk=whpCWn%2byPBvtGi1%2boY1sEBq%2frEUaP6w2N5bnBQsLWdo%3d&risl=&pid=ImgRaw&r=0");
            users.setGender(0);
            users.setUserPassword("12345678");
            users.setPhone("123");
            users.setEmail("123@qq.com");
            users.setUserStatus(0);
            users.setUserRole(0);
            users.setPlanetCode("111111");
            users.setTags("[]");

            list.add(users);
        }
        usersService.saveBatch(list,100);
        stopWatch.stop();
        System.out.println( stopWatch.getTotalTimeMillis());
    }


    /**
     * 并发批量插入用户
     */
    @Test
    public void doConcurrencyInsertUser(){
        StopWatch stopWatch=new StopWatch();
        stopWatch.start();
        final int INSERT_NUM=100000;
        //分成10组  每组一万条
        List<CompletableFuture<Void>> futureList=new ArrayList<>();

        int j=0;
        for (int i = 0; i < 20; i++) {
            List<Users> list=new ArrayList<>();
           while (true){
                j++;
                Users users=new Users();
                users.setUsername("假用户");
                users.setUserAccount("fakeyupi");
                users.setAvatarUrl("https://ts1.cn.mm.bing.net/th/id/R-C.57384e4c2dd256a755578f00845e60af?rik=uy9%2bvT4%2b7Rur%2fA&riu=http%3a%2f%2fimg06file.tooopen.com%2fimages%2f20171224%2ftooopen_sy_231021357463.jpg&ehk=whpCWn%2byPBvtGi1%2boY1sEBq%2frEUaP6w2N5bnBQsLWdo%3d&risl=&pid=ImgRaw&r=0");
                users.setGender(0);
                users.setUserPassword("12345678");
                users.setPhone("123");
                users.setEmail("123@qq.com");
                users.setUserStatus(0);
                users.setUserRole(0);
                users.setPlanetCode("111111");
                users.setTags("[]");
                list.add(users);
                if(j%500==0){
                    break;
                }
            }

            CompletableFuture<Void> future=CompletableFuture.runAsync(()->{
                System.out.println("threadName:"+Thread.currentThread().getName());
                usersService.saveBatch(list,500);
            },executorService);
            futureList.add(future);
        }
        CompletableFuture.allOf(futureList.toArray(new CompletableFuture[]{})).join();

        stopWatch.stop();
        System.out.println( stopWatch.getTotalTimeMillis());
    }



}
