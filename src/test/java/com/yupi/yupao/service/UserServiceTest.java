package com.yupi.yupao.service;

import com.yupi.yupao.model.domain.Users;
import org.junit.Assert;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


@SpringBootTest
class UserServiceTest {


    @Resource
    private UsersService userService;


    @Test
    public void test(){
        Users users=new Users();
        users.setUsername("zzz");
        users.setUserAccount("123");
        users.setAvatarUrl("");
        users.setGender(0);
        users.setUserPassword("456");
        users.setPhone("1321");
        users.setEmail("465");
        users.setUserStatus(0);

       userService.save(users);

    }

    @Test
    void userRegister() {

        //检验非空
        String userAccount="yupi";
        String userPassword="";
        String checkPassword="123456";
        String planetCode="1";
        long result = userService.usersRegister(userAccount, userPassword, checkPassword,planetCode);
        Assertions.assertEquals(-1,result);

        //账户不小于四位
        userAccount="yu";
        result = userService.usersRegister(userAccount, userPassword, checkPassword,planetCode);
        Assertions.assertEquals(-1,result);

        //密码不小于8位
        userPassword="123456";
        userAccount="yupi";
        result = userService.usersRegister(userAccount, userPassword, checkPassword,planetCode);
        Assertions.assertEquals(-1,result);

        //不包含特殊字符
        userAccount="yu pi";
        userPassword="12345678";
        checkPassword="12345678";
        result = userService.usersRegister(userAccount, userPassword, checkPassword,planetCode);
        Assertions.assertEquals(-1,result);

        //密码和校验密码相同
        checkPassword="123456789";
        result = userService.usersRegister(userAccount, userPassword, checkPassword,planetCode);
        Assertions.assertEquals(-1,result);

        //账户不能相同
        userAccount="dogyupi";
        userPassword="12345678";
        checkPassword="12345678";
        result = userService.usersRegister(userAccount, userPassword, checkPassword,planetCode);
        Assertions.assertEquals(-1,result);


        userAccount="aibiancheng";
        userPassword="12345678";
        checkPassword="12345678";
        result = userService.usersRegister(userAccount, userPassword, checkPassword,planetCode);
        Assertions.assertEquals(-1,result);




    }


    @Test
    public void testsearchUsersByTags(){
        List<String> tagNameList= Arrays.asList("java","python");
        List<Users> usersList = userService.searchUsersByTags(tagNameList);
        Assert.assertNotNull(usersList);
    }


}