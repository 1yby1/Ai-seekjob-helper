package org.backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.backend.Mapper.UserMapper;
import org.backend.Model.User;
import org.backend.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserMapper userMapper;

    @Override
    public boolean authenticate(String username, String password) {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("username", username).eq("password", password);
        return userMapper.selectCount(queryWrapper) > 0;
    }

    @Override
    public boolean register(String username, String password, String email) {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("username", username);
        if (userMapper.selectCount(queryWrapper) > 0) {
            return false; // User already exists
        }

        User user = new User();
        user.setUsername(username);
        user.setPassword(password); // Note: In a real app, you should hash the password!
        user.setEmail(email);
        return userMapper.insert(user) > 0;
    }
    
    @Override
    public User authenticateAndGetUser(String username, String password) {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("username", username).eq("password", password);
        return userMapper.selectOne(queryWrapper);
    }
    
    @Override
    public User registerAndGetUser(String username, String password, String email) {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("username", username);
        if (userMapper.selectCount(queryWrapper) > 0) {
            return null; // User already exists
        }

        User user = new User();
        user.setUsername(username);
        user.setPassword(password); // Note: In a real app, you should hash the password!
        user.setEmail(email);
        
        int result = userMapper.insert(user);
        if (result > 0) {
            return user; // MyBatis-Plus会自动填充ID
        }
        return null;
    }
}
