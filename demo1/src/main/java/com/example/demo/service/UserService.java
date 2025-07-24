package com.example.demo.service;

import com.example.demo.entity.User;
import com.example.demo.resposity.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    public boolean registerUser(User user) {
        try {
            userRepository.save(user);
            return true; // 注册成功
        } catch (Exception e) {
            return false; // 注册失败
        }
    }

    public boolean authenticateUser(String username, String password) {
        User user = userRepository.findByUsername(username);
        return user != null && user.getPassword().equals(password); // 检查用户名和密码
    }

    public User getUserByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public User getUserById(int id) {
        return userRepository.findById(id).orElse(null);
    }

    public boolean updateUserInfo(int id, String sex, Integer age, String email) {
        try {
            User user = userRepository.findById(id).orElse(null);
            if (user == null) {
                return false; // 用户不存在
            }
            // 更新用户信息
            if (sex != null) {
                user.setSex(sex);
            }
            if (age != null) {
                user.setAge(age);
            }
            if (email != null) {
                user.setEmail(email);
            }
            userRepository.save(user); // 保存更新后的用户
            return true;
        } catch (Exception e) {
            return false; // 更新失败
        }
    }
}
