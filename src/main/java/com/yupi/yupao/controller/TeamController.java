package com.yupi.yupao.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.yupi.yupao.common.BaseResponse;
import com.yupi.yupao.common.ErrorCode;
import com.yupi.yupao.common.ResultUtils;
import com.yupi.yupao.exception.BusinessException;
import com.yupi.yupao.model.domain.Team;
import com.yupi.yupao.model.domain.Users;
import com.yupi.yupao.model.domain.dto.TeamQuery;
import com.yupi.yupao.model.domain.request.TeamAddRequest;
import com.yupi.yupao.service.TeamService;
import com.yupi.yupao.service.UsersService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.bind.annotation.*;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;


@RestController
@RequestMapping("/team")
@Slf4j
@CrossOrigin(origins = {"http://localhost:5173"},allowCredentials = "true")
@EnableScheduling
public class TeamController {

     @Resource
     private UsersService usersService;

     @Resource
     private TeamService teamService;

    @PostMapping("/add")
    public BaseResponse<Long> addTeam(@RequestBody TeamAddRequest teamAddRequest, HttpServletRequest request){
        if(teamAddRequest==null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Users loginUser = usersService.getLoginUser(request);
        Team team=new Team();
        BeanUtils.copyProperties(teamAddRequest,team);
        long teamId = teamService.addTeam(team, loginUser);
        return ResultUtils.success(teamId);
    }


    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteTeam(@RequestBody Long id){
         if(id<0){
             throw new BusinessException(ErrorCode.PARAMS_ERROR);
         }
         boolean result = teamService.removeById(id);
         if(!result){
             throw  new BusinessException(ErrorCode.SYSTEM_ERROR,"删除失败");
         }

         return ResultUtils.success(true);
    }

    @PostMapping("/update")
    public BaseResponse<Boolean> updateTeam(@RequestBody Team team){
        if(team==null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        boolean result = teamService.updateById(team);
        if(!result){
            throw  new BusinessException(ErrorCode.SYSTEM_ERROR,"删除失败");
        }
        return ResultUtils.success(true);
    }



    @GetMapping("/get")
    public BaseResponse<Team> getTeamById(long id){
        if(id<=0){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Team team = teamService.getById(id);
        if(team==null){
            throw new BusinessException(ErrorCode.NULL_ERROR);
        }
        return ResultUtils.success(team);
    }

    @GetMapping("/list")
    public BaseResponse<List<Team>> listTeams(TeamQuery teamQuery){
         if(teamQuery==null){
             throw new BusinessException(ErrorCode.PARAMS_ERROR);
         }
         Team team=new Team();
         BeanUtils.copyProperties(team,teamQuery);
        QueryWrapper<Team> queryWrapper=new QueryWrapper<>(team);
        List<Team> teamList = teamService.list(queryWrapper);
        return ResultUtils.success(teamList);
    }

    @GetMapping("/list/page")
    public BaseResponse<Page<Team>> listTeamsByPage(TeamQuery teamQuery){
        if(teamQuery==null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Team team=new Team();
        BeanUtils.copyProperties(team,teamQuery);

        Page<Team> page=new Page<>(teamQuery.getPageNum(),teamQuery.getPageSize());

        QueryWrapper<Team> queryWrapper=new QueryWrapper<>(team);
        Page<Team> pageResult = teamService.page(page,queryWrapper);
       return ResultUtils.success(pageResult);
    }



}
