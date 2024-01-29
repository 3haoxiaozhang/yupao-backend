package com.yupi.yupao;


import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.util.DigestUtils;

import java.security.NoSuchAlgorithmException;


@SpringBootTest
class YuPaoCenterApplicationTests {

    @Test
    void testDigest() throws NoSuchAlgorithmException {
        String s = DigestUtils.md5DigestAsHex(("abcd" + "mypassword").getBytes());
        System.out.println(s);
    }


    @Test

    void contextLoads() {

    }

}
