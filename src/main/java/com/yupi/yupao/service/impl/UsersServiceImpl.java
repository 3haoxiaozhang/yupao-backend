package com.yupi.yupao.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.yupi.yupao.common.ErrorCode;
import com.yupi.yupao.contant.UserConstant;
import com.yupi.yupao.exception.BusinessException;
import com.yupi.yupao.model.domain.Users;
import com.yupi.yupao.service.UsersService;
import com.yupi.yupao.mapper.UsersMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.DigestUtils;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
* @author 张家霖
* @description 针对表【users(用户)】的数据库操作Service实现
* @createDate 2024-01-24 22:26:34
*/
@Service
@Slf4j
public class UsersServiceImpl extends ServiceImpl<UsersMapper, Users>
    implements UsersService{

    @Resource
    private UsersMapper usersMapper;


    /**
     * 盐值：混淆密码
     */
    private static final String SALT = "yupi";


    /**
     * 用户注册
     * @param usersAccount
     * @param usersPassword
     * @param checkPassword
     * @param planetCodes
     * @return
     */
    @Override
    public Long usersRegister(String usersAccount, String usersPassword, String checkPassword,String planetCodes) {
        //先判断是否为空
        if(StringUtils.isAnyBlank(usersAccount,usersPassword,checkPassword,planetCodes)){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"参数为空");
        }
        //账号长度不小于4
        if(usersAccount.length()<4){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"用户账号小于4位");
        }
        if (usersPassword.length() < 8 || checkPassword.length() < 8) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"用户密码过短");
        }

        //星球编号过长
        if(planetCodes.length()>6){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"星球编号过长");
        }
        //检验账户包含特殊字符
        String regex = "^[a-zA-Z0-9_]+$";
        Pattern pattern = Pattern.compile(regex);
        boolean isMatch = pattern.matcher(usersAccount).matches();
        if (!isMatch) {
           return null;
        }
        //两次密码是否相同
        if(!usersPassword.equals(checkPassword)){
            return null;
        }
        //账号是否唯一
        QueryWrapper queryWrapper=new QueryWrapper();
        queryWrapper.eq("userAccount",usersAccount);
        Long count = usersMapper.selectCount(queryWrapper);
        if(count>0){
            throw new BusinessException(ErrorCode.USER_EXIST,"账户已存在");
        }

        //星球编号不能重复
        queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("planetCode", planetCodes);
        count = usersMapper.selectCount(queryWrapper);
        if (count > 0) {
            throw new BusinessException(ErrorCode.USER_EXIST,"账户已存在");
        }

        //先对密码进行md5加密处理
        String encryptPassword = DigestUtils.md5DigestAsHex((SALT + usersPassword).getBytes());


        //如果唯一  则插入
        Users users=new Users();

        users.setUserAccount(usersAccount);
        users.setUserPassword(encryptPassword);
        users.setGender(0);
        users.setPlanetCode(planetCodes);
        boolean save = this.save(users);
        if (!save) {
            throw new BusinessException(ErrorCode.USER_EXIST,"账户已存在");
        }

        return users.getId();
    }

    /**
     * 用户登录
     * @param usersAccount
     * @param usersPassword
     * @param request
     * @return
     */
    @Override
    public Users usersLogin(String usersAccount, String usersPassword, HttpServletRequest request) {
       //先检验账号密码是否为空
        if(StringUtils.isAnyBlank(usersAccount,usersPassword)){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"不能为空");
       }

        if (usersAccount.length() < 4) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"账户长度不能小于");
        }
        if (usersPassword.length() < 8) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"密码长度不能小于8");
        }

        //检验账户包含特殊字符
        String regex = "^[a-zA-Z0-9_]+$";
        Pattern pattern = Pattern.compile(regex);
        boolean isMatch = pattern.matcher(usersAccount).matches();
        if (!isMatch) {
            return null;
        }

        //先对密码进行md5加密处理
        String SALT="yupi";
        String encryptPassword = DigestUtils.md5DigestAsHex((SALT + usersPassword).getBytes());


        //对帐号密码检验
        QueryWrapper queryWrapper=new QueryWrapper();
        queryWrapper.eq("userAccount",usersAccount);
        queryWrapper.eq("userPassword",encryptPassword);
        Users users = usersMapper.selectOne(queryWrapper);
        if(users==null){
            log.info("user login failed,userAccount cannot match userPassword");
            throw new BusinessException(ErrorCode.USER_NOT_EXIST,"账号密码错误");
        }


       //用户脱敏
        Users safetyUser = getSafetyUser(users);


        //将用户信息存储到登录态中
        request.getSession().setAttribute(UserConstant.USER_LOGIN_STATE,users);

        return safetyUser;

}

    /**
     * 用户脱敏
     * @param users
     * @return
     */
    @Override
    public Users getSafetyUser(Users users) {

        if(users==null){
            return null;
        }
        Users safetyUser=new Users();

        safetyUser.setId(users.getId());
        safetyUser.setUsername(users.getUsername());
        safetyUser.setUserAccount(users.getUserAccount());
        safetyUser.setAvatarUrl(users.getAvatarUrl());
        safetyUser.setGender(users.getGender());
        safetyUser.setPhone(users.getPhone());
        safetyUser.setUserRole(users.getUserRole());
        safetyUser.setEmail(users.getEmail());
        safetyUser.setPlanetCode(users.getPlanetCode());
        safetyUser.setUserStatus(users.getUserStatus());
        safetyUser.setCreateTime(users.getCreateTime());
        safetyUser.setTags(users.getTags());
        return safetyUser;
    }

    /**
     * 用户注销
     * @param request
     * @return
     */
    @Override
    public int usersLogout(HttpServletRequest request) {
        request.getSession().removeAttribute(UserConstant.USER_LOGIN_STATE);
        return 1;
    }



    /**
     * 根据标签查询用户（内存版）
     * @param tagNameList
     * @return
     */
    @Override
    public List<Users> searchUsersByTags(List<String> tagNameList) {
        //   内存中查询
        //1.先查询所有的用户
        QueryWrapper queryWrapper = new QueryWrapper<>();
        List<Users> usersList = usersMapper.selectList(queryWrapper);
        Gson gson = new Gson();

        //2.在内存中判断是否包含要求的标签
//        for (Users users : usersList) {
//            String tagsStr = users.getTags();
//        if (StringUtils.isBlank(tagsStr)) {
//            return false;
//        }
//            Set<String> tempTagNameSet = gson.fromJson(tagsStr, new TypeToken<List<String>>() {
//            }.getType());
//            for (String tagName : tempTagNameSet) {
//                if (!tempTagNameSet.contains(tagName)) {
//                    return false;
//                }
//            }
//            return true;
//        }
        return usersList.stream().filter(users -> {
            String tagsStr = users.getTags();
            Set<String> tempTagNameSet = gson.fromJson(tagsStr, new TypeToken<Set<String>>() {}.getType());
            tempTagNameSet = Optional.ofNullable(tempTagNameSet).orElse(new HashSet<>());
            for (String tagName : tagNameList) {
                if (!tempTagNameSet.contains(tagName)) {
                    return false;
                }
            }
            return true;
        }).map(users -> getSafetyUser(users)).collect(Collectors.toList());

    }


    /**
     * 用户更新
     * @param users
     * @return
     */
    @Override
    public int updateUser(Users users,Users loginUser) {
        Long id = users.getId();
        if(id<=0){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
      //1.如果是管理员，允许更新任意用户
      //2.如果不是管理员，只允许更新当前（自己）的信息
        if(!isAdmin(loginUser)&&id!=loginUser.getId()){
            throw new BusinessException(ErrorCode.NO_AUTH);
        }
        Users oldUser = usersMapper.selectById(id);
        if(oldUser==null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        return  usersMapper.updateById(users);
    }

    /**
     * 查询用户登录信息
     * @param request
     * @return
     */
    @Override
    public Users getLoginUser(HttpServletRequest request) {
       if(request==null){
         return null;
       }
       Object userObj = request.getSession().getAttribute(UserConstant.USER_LOGIN_STATE);
        if (userObj==null){
            throw new BusinessException(ErrorCode.NO_AUTH);
        }
       return (Users)userObj;

    }
    /**
     * 判断是否为管理员
     * @param request
     * @return
     */
    public boolean isAdmin(HttpServletRequest request){
        //先从session中将用户给取出来
        Object usersLogin = request.getSession().getAttribute(UserConstant.USER_LOGIN_STATE);
        if(usersLogin==null){
            return false;
        } //先将类型进行转换
        Users users=(Users) usersLogin;
        Integer userRole = users.getUserRole();
        if(userRole==0){
            return false;
        }
        return true;
    }

    /**
     * 判断是否为管理员
     * @param loginUser
     * @return
     */
    public boolean isAdmin(Users loginUser){
        Integer userRole = loginUser.getUserRole();
        if(userRole==0){
            return false;
        }
        return true;
    }





    /**
     * 根据标签搜索用户  (SQL 内存版)
     * @param tagNameList
     * @return
     */
    @Deprecated
    private List<Users> searchUsersBySQL(List<String> tagNameList) {
        if (CollectionUtils.isEmpty(tagNameList)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        //1.拼接  查询
        QueryWrapper<Users> queryWrapper=new QueryWrapper();
        for (String tagName : tagNameList) {
            queryWrapper = queryWrapper.like("tags", tagName);
        }
        List<Users> usersList = usersMapper.selectList(queryWrapper);
        return usersList.stream().map(users -> getSafetyUser(users)).collect(Collectors.toList());

    }
}




