package org.backend.Controller;

import org.backend.Model.User;
import org.backend.service.UserService;
import org.backend.utils.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private UserService userService;

    @PostMapping("/login")
    public Map<String, Object> login(@RequestBody User user) {
        Map<String, Object> response = new HashMap<>();
        User authenticatedUser = userService.authenticateAndGetUser(user.getUsername(), user.getPassword());
        
        if (authenticatedUser != null) {
            // 生成JWT Token
            String token = JwtUtil.generateToken(authenticatedUser.getId());
            response.put("success", true);
            response.put("message", "登录成功");
            response.put("token", token);
            response.put("userId", authenticatedUser.getId());
            response.put("username", authenticatedUser.getUsername());
        } else {
            response.put("success", false);
            response.put("message", "登录失败，用户名或密码错误");
        }
        
        return response;
    }

    @PostMapping("/register")
    public Map<String, Object> register(@RequestBody User user) {
        Map<String, Object> response = new HashMap<>();
        User registeredUser = userService.registerAndGetUser(user.getUsername(), user.getPassword(), user.getEmail());
        
        if (registeredUser != null) {
            // 注册成功后也生成Token，方便用户直接登录
            String token = JwtUtil.generateToken(registeredUser.getId());
            response.put("success", true);
            response.put("message", "注册成功");
            response.put("token", token);
            response.put("userId", registeredUser.getId());
            response.put("username", registeredUser.getUsername());
        } else {
            response.put("success", false);
            response.put("message", "注册失败，用户名可能已存在");
        }
        
        return response;
    }
}
