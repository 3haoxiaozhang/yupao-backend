package com.yupi.yupao.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yupi.yupao.model.domain.Team;
import com.yupi.yupao.model.domain.Users;

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
}
