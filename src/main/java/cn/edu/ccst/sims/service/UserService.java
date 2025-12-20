package cn.edu.ccst.sims.service;

import cn.edu.ccst.sims.dto.LoginDTO;
import cn.edu.ccst.sims.dto.RegisterDTO;

import java.util.Map;

public interface UserService {
    String login(LoginDTO dto);
    void register(RegisterDTO dto);
    // 新增：获取用户详细信息
    Map<String, Object> getUserInfo(Integer userId);
    // 新增：从 token 中获取用户ID
    Integer getUserIdFromToken(String token);

}