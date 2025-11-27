package cn.edu.ccst.sims.service;

import cn.edu.ccst.sims.dto.LoginDTO;
import cn.edu.ccst.sims.dto.RegisterDTO;

public interface UserService {
    String login(LoginDTO dto);
    void register(RegisterDTO dto);
}