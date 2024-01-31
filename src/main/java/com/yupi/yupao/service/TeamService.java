package com.yupi.yupao.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yupi.yupao.model.domain.Team;
import com.yupi.yupao.model.domain.Users;
import com.yupi.yupao.model.dto.TeamQuery;
import com.yupi.yupao.model.request.TeamJoinRequest;
import com.yupi.yupao.model.request.TeamUpdateRequest;
import com.yupi.yupao.model.vo.TeamUserVo;

import java.util.List;

/**
* @author 张家霖
* @description 针对表【team(队伍)】的数据库操作Service
* @createDate 2024-01-30 18:50:29
*/
public interface TeamService extends IService<Team> {

    /**
     * 创建队伍
     * @param team
     * @param loginUser
     * @return
     */
    long addTeam(Team team, Users loginUser);


    /**
     * 获取队伍信息
     * @param teamQuery
     * @param isAdmin
     * @return
     */
    List<TeamUserVo> listTeams(TeamQuery teamQuery,Boolean isAdmin);

    /**
     * 更新队伍
     * @param teamUpdateRequest
     * @return
     */
    boolean updateTeam(TeamUpdateRequest teamUpdateRequest,Users loginUser);

    /**
     * 加入队伍
     * @param teamJoinRequest
     * @return
     */
    Boolean joinTeam(TeamJoinRequest teamJoinRequest,Users loginUser);
}
