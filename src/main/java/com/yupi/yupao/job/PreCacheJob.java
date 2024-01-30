package com.yupi.yupao.job;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yupi.yupao.mapper.UsersMapper;
import com.yupi.yupao.model.domain.Users;
import com.yupi.yupao.service.UsersService;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 *  缓存预热任务
 */
@Component
@Slf4j
public class PreCacheJob {

    @Resource
    private UsersService usersService;

    @Resource
    private RedisTemplate<String,Object> redisTemplate;

    @Resource
    private RedissonClient redissonClient;

    //重点用户
    private List<Long> mainUserList= Arrays.asList(1L);

    //每天执行，加载预热用户
    @Scheduled(cron = "0 12 16 * * *")
    public void doCacheRecommenUser(){
        RLock lock = redissonClient.getLock("yupao:precachejob:docache:lock");
        try {
            //只有一个线程能获取到锁
            if(lock.tryLock(0, -1, TimeUnit.MILLISECONDS)) {
                System.out.println("getlock:"+Thread.currentThread().getId());

                for (Long userId : mainUserList) {
                    QueryWrapper<Users> queryWrapper=new QueryWrapper();
                    Page<Users> userList = usersService.page(new Page<>(1,20), queryWrapper);

                    String redisKey=String.format("yupao.user.recommend:%s",userId);
                    ValueOperations<String,Object> operations = redisTemplate.opsForValue();


                    //写缓存
                    try {
                        operations.set(redisKey,userList,30000, TimeUnit.MILLISECONDS);
                    } catch (Exception e) {
                        log.error("redis set key error",e);
                    }
                }

            }
        } catch (InterruptedException e) {
           log.error("doCacheRecommenUser Error",e);
        }finally {
            //只能释放自己的锁
            if(lock.isHeldByCurrentThread()){
                lock.unlock();
            }

        }
    }



}

