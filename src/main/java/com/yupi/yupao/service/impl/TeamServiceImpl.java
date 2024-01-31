package com.yupi.yupao.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yupi.yupao.common.ErrorCode;
import com.yupi.yupao.exception.BusinessException;
import com.yupi.yupao.mapper.TeamMapper;
import com.yupi.yupao.model.domain.Team;
import com.yupi.yupao.model.domain.UserTeam;
import com.yupi.yupao.model.domain.Users;
import com.yupi.yupao.model.domain.enums.TeamStatusEnum;
import com.yupi.yupao.service.TeamService;

import com.yupi.yupao.service.UserTeamService;
import lombok.EqualsAndHashCode;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Date;
import java.util.Optional;

/**
* @author 张家霖
* @description 针对表【team(队伍)】的数据库操作Service实现
* @createDate 2024-01-30 18:50:29
*/
@Service
public class TeamServiceImpl extends ServiceImpl<TeamMapper, Team>
    implements TeamService {

    @Resource
    private UserTeamService userTeamService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public long addTeam(Team team, Users loginUser) {
        //1.请求参数是否为空
        if(team==null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        //2.是否登录
        if(loginUser==null){
            throw new BusinessException(ErrorCode.NOT_LOGIN);
        }

        final Long userId = loginUser.getId();
       //---3.校验信息---
        //1.队伍人数>1  且  <=20
        int maxNum = Optional.ofNullable(team.getMaxNum()).orElse(0);
        if(maxNum<1||maxNum>20){
            throw  new BusinessException(ErrorCode.PARAMS_ERROR,"队伍人数不满足要求");
        }
        //2.队伍标题<=20
        String name = team.getName();
        if(StringUtils.isBlank(name)||name.length()>20){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"标题不符合要求");
        }
        //3.描述<=512
        String description = team.getDescription();
        if(StringUtils.isNotBlank(description)&&description.length()>512){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"队伍描述过长");
        }
        //4.status  是否为公开
        int status = Optional.ofNullable(team.getStatus()).orElse(0);
        TeamStatusEnum statusEnum = TeamStatusEnum.getEnumByValue(status);
        if(statusEnum==null){
            throw  new BusinessException(ErrorCode.PARAMS_ERROR,"队伍状态不满足要求");
        }
        //5. 如果status是加密状态，一定要有密码，且密码<=32
        String password = team.getPassword();
        if(TeamStatusEnum.SECRET.equals(statusEnum)&&(StringUtils.isBlank(password)||password.length()>32)){
            throw  new BusinessException(ErrorCode.PARAMS_ERROR,"密码不正确");
         }
        //6.超时时间大于当前时间
        Date expireTime = team.getExpireTime();
        if(new Date().after(expireTime)){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"超时时间>当前时间");
        }
        //7.校验用户最多创建5个队伍
        QueryWrapper<Team> queryWrapper=new QueryWrapper<>();
        queryWrapper.eq("userId",userId);
        long hasTeamNumber = this.count(queryWrapper);
        if(hasTeamNumber>=5){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"用户最多创建5个队伍");
        }
        //8.插入队伍信息到队伍表
        team.setId(null);
        team.setUserId(userId);
        boolean result = this.save(team);
        Long teamId = team.getId();
        if(true){
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"创建队伍失败");
        }
        if(!result||teamId==null){
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"创建队伍失败");
        }

        //9.插入用户 =>队伍关系到关系表
        UserTeam userTeam=new UserTeam();
        userTeam.setUserId(userId);
        userTeam.setTeamId(teamId);
        userTeam.setJoinTime(new Date());

        result = userTeamService.save(userTeam);
        if(!result){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"创建队伍失败");
        }
        return teamId;

    }






}




