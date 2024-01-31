package com.yupi.yupao.controller;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yupi.yupao.common.BaseResponse;
import com.yupi.yupao.common.ErrorCode;
import com.yupi.yupao.common.ResultUtils;
import com.yupi.yupao.config.RedisTemplateConfig;
import com.yupi.yupao.contant.UserConstant;
import com.yupi.yupao.exception.BusinessException;
import com.yupi.yupao.model.domain.Users;
import com.yupi.yupao.model.domain.request.UserLoginRequest;
import com.yupi.yupao.model.domain.request.UserRegisterRequest;
import com.yupi.yupao.service.UsersService;
import lombok.extern.slf4j.Slf4j;
import org.apache.catalina.User;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/user")
@Slf4j
@CrossOrigin(origins = {"http://localhost:5173"},allowCredentials = "true")
@EnableScheduling
public class UsersController {

     @Resource
     private UsersService usersService;

     @Resource
     private RedisTemplate redisTemplate;

    /**
     * 用户注册
     * @param userRegisterRequest
     * @return
     */
     @PostMapping("/register")
     public BaseResponse<Long> UserRegister(@RequestBody UserRegisterRequest userRegisterRequest){
        if(userRegisterRequest==null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
         String usersAccount = userRegisterRequest.getUserAccount();
         String usersPassword = userRegisterRequest.getUserPassword();
         String checkPassword = userRegisterRequest.getCheckPassword();
         String planetCode = userRegisterRequest.getPlanetCode();
         if(StringUtils.isAnyBlank(usersAccount,usersPassword,checkPassword,planetCode)){
             throw new BusinessException(ErrorCode.PARAMS_ERROR);
         }


         Long id = usersService.usersRegister(usersAccount, usersPassword, checkPassword, planetCode);

         return ResultUtils.success(id);

     }

    /**
     * 用户登录接口
     * @param userLoginRequest
     * @param request
     * @return
     */
     @PostMapping("/login")
    public  BaseResponse<Users> UsersLogin(@RequestBody UserLoginRequest userLoginRequest, HttpServletRequest request){
         if(userLoginRequest==null){
             throw new BusinessException(ErrorCode.PARAMS_ERROR);
         }
         String userAccount = userLoginRequest.getUserAccount();
         String userPassword =userLoginRequest.getUserPassword();

         if(StringUtils.isAnyBlank(userAccount,userPassword)){
             throw new BusinessException(ErrorCode.PARAMS_ERROR);
         }


         Users users = usersService.usersLogin(userAccount, userPassword, request);
         return ResultUtils.success(users);
     }

    /**
     *  用户注销接口
     * @param request
     * @return
     */
    @PostMapping("/logout")
    public BaseResponse<Integer> usersLLogout(HttpServletRequest request){
         if(request==null){
             throw new BusinessException(ErrorCode.NOT_LOGIN);
         }
        return ResultUtils.success(usersService.usersLogout(request));
    }


    /**
     * 当前用户
     * @param request
     * @return
     */
     @GetMapping("/current")
     public BaseResponse<Users> currentUser( HttpServletRequest request){
        //先从session中将用户给取出来
          // JSESSIONID=187D11B95F7B7261A4578DB2D598D050; Idea-6a8752bd=a3589175-2f0c-4ab7-a2c4-f068a85ed130
         //  JSESSIONID=187D11B95F7B7261A4578DB2D598D050; Idea-6a8752bd=a3589175-2f0c-4ab7-a2c4-f068a85ed130
         Object usersLogin = request.getSession().getAttribute(UserConstant.USER_LOGIN_STATE);
        if(usersLogin==null){
            throw new BusinessException(ErrorCode.NOT_LOGIN);
        }
         //先将类型进行转换
         Users currentUser=(Users)usersLogin;
         Long userId = currentUser.getId();
         //todo 校验用户是否合法
         Users user = usersService.getById(userId);
         Users safetyUser = usersService.getSafetyUser(user);

         return ResultUtils.success(safetyUser);
     }

    /**
     * 查询用户接口
     * @param request
     * @return
     */
     @GetMapping("/search")
     public BaseResponse<List<Users>> getUserLists(String username,HttpServletRequest request){
         if(usersService.isAdmin(request)){
             throw new BusinessException(ErrorCode.PARAMS_ERROR);
         }
         QueryWrapper<Users> queryWrapper=new QueryWrapper<>();
         if(StringUtils.isNotBlank(username)){
             queryWrapper.like("username",username);
         }

         List<Users> list = usersService.list(queryWrapper);
         List<Users> usersList = list.stream().map(users -> usersService.getSafetyUser(users))
                 .collect(Collectors.toList());
         return ResultUtils.success(usersList);

     }

    /**
     * 用户删除接口
     * @param
     * @param request
     * @return
     */
     @PostMapping("/delete")
     public BaseResponse<Boolean> usersdelete(@RequestBody Long id,HttpServletRequest request){
         if(!usersService.isAdmin(request)){
             throw new BusinessException(ErrorCode.NO_AUTH);
         }
         if(id<=0){
             throw new BusinessException(ErrorCode.PARAMS_ERROR);
         }
         boolean result = usersService.removeById(id);
         return ResultUtils.success(result);
     }


    /**
     * 根据标签查询用户
     * @param tagNameList
     * @return
     */
    @GetMapping("/search/tags")
     public BaseResponse<List<Users>>  searchUsersByTags(@RequestParam(required = false) List<String> tagNameList){
        if (CollectionUtils.isEmpty(tagNameList)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
         List<Users> users = usersService.searchUsersByTags(tagNameList);
         return ResultUtils.success(users);
     }

    /**
     * 主页根据标签推荐用户
     * @param
     */
    @GetMapping("/recommend")
    public BaseResponse<Page<Users>>  recommendUsers(Long pageSize,Long pageNum,HttpServletRequest request){

        Users loginUser=usersService.getLoginUser(request);
        String redisKey=String.format("yupao.user.recommend:%s",loginUser.getId());
        ValueOperations operations = redisTemplate.opsForValue();

        //如果有缓存，直接读缓存
        Page<Users> usersPage = (Page<Users>) operations.get(redisKey);
        if(usersPage!=null){
            return ResultUtils.success(usersPage);
        }
        //如果没有缓存，将读出数据写入缓存
        QueryWrapper<Users> queryWrapper=new QueryWrapper();
        Page<Users> userList = usersService.page(new Page<>(pageNum,pageSize), queryWrapper);
        //写缓存
        try {
            operations.set(redisKey,userList,30000, TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            log.error("redis set key error",e);
        }

        return ResultUtils.success(userList);
    }


    /**
     * 用户更新
     * @param users
     * @param request
     * @return
     */
    @PostMapping("/update")
    public BaseResponse<Integer> updateUser(@RequestBody Users users,HttpServletRequest request){
        //1.检验参数是否为空
        if(users==null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        Users loginUser = usersService.getLoginUser(request);

        int result = usersService.updateUser(users,loginUser);
        return ResultUtils.success(result);
    }




}
