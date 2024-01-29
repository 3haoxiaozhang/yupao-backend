package com.yupi.yupao.service;
import java.util.Date;


import com.yupi.yupao.model.domain.Users;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import javax.annotation.Resource;

@SpringBootTest
public class RedisTest {

    @Resource
    private RedisTemplate redisTemplate;

    @Test
    void test() {
        ValueOperations valueOperations = redisTemplate.opsForValue();
        //增
        valueOperations.set("yupiString", "dog");
        valueOperations.set("yupiInt", 1);
        valueOperations.set("yupiDouble", 2.0);
        Users users = new Users();
        users.setId(1L);
        users.setUsername("yupi");
        valueOperations.set("yupi", users);

        //查
        Object yupi = valueOperations.get("yupiString");
        Assertions.assertTrue("dog".equals((String) yupi));
        Object yupiInt = valueOperations.get("yupiInt");
        Assertions.assertTrue(1 == (Integer) yupiInt);
        Object yupiDouble = valueOperations.get("yupiDouble");
        Assertions.assertTrue((Double) yupiDouble == 2.0);
        Object yupi1 = valueOperations.get("yupi");
        System.out.println(yupi1);

        //改
        valueOperations.set("yupiString","dog");

        //删
        redisTemplate.delete("yupiString");

    }



}


