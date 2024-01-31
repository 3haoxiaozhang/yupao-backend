package com.yupi.yupao.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yupi.yupao.common.ErrorCode;
import com.yupi.yupao.exception.BusinessException;
import com.yupi.yupao.mapper.TeamMapper;
import com.yupi.yupao.model.domain.Team;
import com.yupi.yupao.model.domain.UserTeam;
import com.yupi.yupao.model.domain.Users;
import com.yupi.yupao.model.dto.TeamQuery;
import com.yupi.yupao.model.enums.TeamStatusEnum;
import com.yupi.yupao.model.request.TeamJoinRequest;
import com.yupi.yupao.model.request.TeamUpdateRequest;
import com.yupi.yupao.model.vo.TeamUserVo;
import com.yupi.yupao.model.vo.UserVo;
import com.yupi.yupao.service.TeamService;

import com.yupi.yupao.service.UserTeamService;
import com.yupi.yupao.service.UsersService;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.ehcache.core.util.CollectionUtil;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
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

    @Resource
    private UsersService usersService;

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

    @Override
    public List<TeamUserVo> listTeams(TeamQuery teamQuery,Boolean isAdmin) {
        QueryWrapper<Team> queryWrapper=new QueryWrapper<>();
        //组合查询条件
        if(teamQuery!=null){
            Long id = teamQuery.getId();
            if(id!=null&&id>0){
                queryWrapper.eq("id",id);
            }
            String searchText = teamQuery.getSearchText();
            if (StringUtils.isNotBlank(searchText)) {
                queryWrapper.and(qw->qw.like("name",searchText).or().like("description",searchText));
            }
            String name = teamQuery.getName();
            if(StringUtils.isNotBlank(name)){
                queryWrapper.like("name",name);
            }
            String description = teamQuery.getDescription();
            if(StringUtils.isNotBlank(description)){
                queryWrapper.like("description",description);
            }
            //查询最大人数相等的
            Integer maxNum = teamQuery.getMaxNum();
            if(maxNum!=null&&maxNum>0){
                queryWrapper.eq("maxNum",maxNum);
            }
            //根据创建人查询
            Long userId = teamQuery.getUserId();
            if(userId!=null&&userId>0){
                queryWrapper.eq("userId",userId);
            }
            //根据状态查询
            Integer status = teamQuery.getStatus();
            TeamStatusEnum statusEnum = TeamStatusEnum.getEnumByValue(status);
            if(statusEnum==null){
                statusEnum=TeamStatusEnum.PUBLIC;
            }
            if(!isAdmin&&statusEnum.equals(TeamStatusEnum.PUBLIC)){
                throw new BusinessException(ErrorCode.NO_AUTH);
            }
                queryWrapper.eq("status",statusEnum.getValue());
        }
        //不展示已过期的队伍
        //expireTime is  null or expireTime > now()
        queryWrapper.and(qw->qw.gt("expireTime",new Date()).or().isNull("expireTime"));
        List<Team> teamList = this.list(queryWrapper);
        if(CollectionUtils.isEmpty(teamList)){
            return new ArrayList<>();
        }
        List<TeamUserVo> teamUserVoList=new ArrayList<>();
        //关联查询创建人的用户信息
        for (Team team : teamList) {
            Long userId = team.getUserId();
            if(userId==null){
                continue;
            }
            Users user = usersService.getById(userId);
            TeamUserVo teamUserVo=new TeamUserVo();
            BeanUtils.copyProperties(team,teamUserVo);
            //对用户信息脱敏
            if(user!=null) {
                UserVo userVo=new UserVo();
                BeanUtils.copyProperties(user, userVo);
                teamUserVo.setCreateUser(userVo);
            }
            teamUserVoList.add(teamUserVo);
        }
        return teamUserVoList;

    }

    @Override
    public boolean updateTeam(TeamUpdateRequest teamUpdateRequest,Users loginUser) {
           if(teamUpdateRequest==null){
               throw  new BusinessException(ErrorCode.PARAMS_ERROR);
           }
        Long id = teamUpdateRequest.getId();
         if(id==null||id<=0){
             throw  new BusinessException(ErrorCode.PARAMS_ERROR);
         }
        Team oldTeam = this.getById(id);
         if(oldTeam==null){
             throw new BusinessException(ErrorCode.NULL_ERROR,"没找到该组");
         }
         //只有管理者或者队伍的管理员可以修改
         if(oldTeam.getUserId()!=loginUser.getId() && !usersService.isAdmin(loginUser)){
             throw new BusinessException(ErrorCode.NO_AUTH);
         }
        TeamStatusEnum odlEnumByValue = TeamStatusEnum.getEnumByValue(oldTeam.getStatus());
        TeamStatusEnum enumByValue = TeamStatusEnum.getEnumByValue(teamUpdateRequest.getStatus());
         if(!odlEnumByValue.equals(TeamStatusEnum.SECRET)&&enumByValue.equals(TeamStatusEnum.SECRET)){
            if(StringUtils.isBlank(teamUpdateRequest.getPassword())){
                throw new BusinessException(ErrorCode.PARAMS_ERROR,"加密房间必须要有密码");
            }
         }
        Team updateTeam=new Team();
         BeanUtils.copyProperties(teamUpdateRequest,updateTeam);
         boolean result = this.updateById(updateTeam);
         return result;
    }

    @Override
    public Boolean joinTeam(TeamJoinRequest teamJoinRequest,Users loginUser) {
        if(teamJoinRequest==null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }


        Long userId = loginUser.getId();
        Long teamId = teamJoinRequest.getTeamId();

        if(teamId==null||teamId<=0){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        Team team = this.getById(teamId);
        if(team==null){
            throw new BusinessException(ErrorCode.NULL_ERROR,"队伍不存在");
        }

        Date expireTime = team.getExpireTime();
        if(expireTime!=null&&expireTime.before(new Date())){
            throw new BusinessException(ErrorCode.NULL_ERROR,"队伍已过期");
        }

        Integer status = team.getStatus();
        TeamStatusEnum enumByValue = TeamStatusEnum.getEnumByValue(status);
        if(TeamStatusEnum.PRIVATE.equals(enumByValue)){
            throw new BusinessException(ErrorCode.NULL_ERROR,"禁止加入私有队伍");
        }

        String password = teamJoinRequest.getPassword();
        if(enumByValue.equals(TeamStatusEnum.SECRET)){
            if(StringUtils.isBlank(password)||!password.equals(team.getPassword())){
                throw new BusinessException(ErrorCode.NULL_ERROR,"密码错误");
            }
        }

        //该用户已加入的队伍数量
        QueryWrapper<UserTeam> queryWrapper=new QueryWrapper<>();
        queryWrapper.eq("userId",userId);
        long hasJoinNum = userTeamService.count(queryWrapper);
        if(hasJoinNum>5){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"最多创建和加入五个队伍");
        }
        //不能重复加入已加入的队伍
        queryWrapper=new QueryWrapper<>();
        queryWrapper.eq("userId",userId);
        queryWrapper.eq("teamId",teamId);
        long hasUserJoinTeam = userTeamService.count(queryWrapper);
        if(hasUserJoinTeam>0){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"用户已加入该队伍");
        }


        //已加入队伍的人数
        queryWrapper=new QueryWrapper<>();
        queryWrapper.eq("teamId",teamId);
        long teamHasJoinNum = userTeamService.count(queryWrapper);
        if(teamHasJoinNum>=team.getMaxNum()){
            throw new BusinessException(ErrorCode.NULL_ERROR,"队伍已满");
        }

        //新增队伍--用户关联信息
        UserTeam userTeam=new UserTeam();
        userTeam.setUserId(userId);
        userTeam.setTeamId(teamId);
        userTeam.setJoinTime(new Date());
        boolean save = userTeamService.save(userTeam);


        return save;
    }


}




