package com.yupi.yupao.once;
import java.util.Date;


import com.yupi.yupao.mapper.UsersMapper;
import com.yupi.yupao.model.domain.Users;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;

import javax.annotation.Resource;

@Component
public class InsertUsers {

    @Resource
    private UsersMapper usersMapper;

    /**
     * 批量插入用户
     */
    //@Scheduled(initialDelay = 5000,fixedRate = Long.MAX_VALUE)
    public void doInsertUser(){
        StopWatch stopWatch=new StopWatch();
        stopWatch.start();
        System.out.println("good");
        final int INSERT_NUM=1000;
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

            usersMapper.insert(users);
        }

        stopWatch.stop();
        System.out.println( stopWatch.getTotalTimeMillis());

    }


}
