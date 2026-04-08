package org.backend.service;

import org.backend.Model.User;
import org.springframework.stereotype.Service;

@Service
public interface UserService {
    boolean authenticate(String username, String password);
    boolean register(String username, String password, String email);
    
    // 新增：认证并返回用户对象
    User authenticateAndGetUser(String username, String password);
    
    // 新增：注册并返回用户对象
    User registerAndGetUser(String username, String password, String email);
}
