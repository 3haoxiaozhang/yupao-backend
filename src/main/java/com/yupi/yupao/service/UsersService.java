package com.yupi.yupao.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.yupi.yupao.model.domain.Users;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
* @author 张家霖
* @description 针对表【users(用户)】的数据库操作Service
* @createDate 2024-01-24 22:26:34
*/
public interface UsersService extends IService<Users>{

    /**
     * 用户注册功能
     * @param usersAccount
     * @param usersPassword
     * @param checkPassword
     * @return
     */
    Long usersRegister(String usersAccount,String usersPassword,String checkPassword,String PlanetCodes);


    /**
     * 用户登录功能
     * @param usersAccount
     * @param usersPassword
     * @param request
     * @return
     */
    Users usersLogin(String usersAccount, String usersPassword, HttpServletRequest request);

    /**
     * 用户脱敏
     * @param users
     * @return
     */
    Users getSafetyUser(Users users);

    /**
     * 用户注销接口
     * @param request
     * @return
     */
    int usersLogout(HttpServletRequest request);


    /**
     *根据标签查询
     * @param tagNameList
     * @return
     */
    List<Users> searchUsersByTags(List<String> tagNameList);

    /**
     * 更新用户信息
     * @param users
     * @return
     */
    int updateUser(Users users,Users loginUser);

    /**
     * 获取当前登录用户信息
     * @return
     */
    Users getLoginUser(HttpServletRequest request);

    /**
     * 判断是否为管理员
     * @param request
     * @return
     */
    boolean isAdmin(HttpServletRequest request);

    /**
     * 判断是否为管理员
     * @param users
     * @return
     */
    boolean isAdmin(Users users);

}
