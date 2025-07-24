package com.example.demo.controller;

import com.example.demo.entity.User;
import com.example.demo.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "http://10.23.10.58:8080")
public class UserController {

    @Autowired
    private UserService userService;

    @PostMapping("/register")
    public ResponseEntity<Boolean> registerUser(@Valid @RequestBody User user, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(false);
        }
        boolean isRegistered = userService.registerUser(user);
        if (isRegistered) {
            return ResponseEntity.ok(true);
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(false);
        }
    }

    @PostMapping("/login")
    public ResponseEntity<User> loginUser(@RequestBody User user) {
        boolean isAuthenticated = userService.authenticateUser(user.getUsername(), user.getPassword());
        if (isAuthenticated) {
            User authenticatedUser = userService.getUserByUsername(user.getUsername());
            return ResponseEntity.ok(authenticatedUser); // 登录成功，返回用户信息
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null); // 登录失败
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<User> getUserById(@PathVariable int id) {
        User user = userService.getUserById(id);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
        return ResponseEntity.ok(user);
    }

    @GetMapping("/username/{username}")
    public ResponseEntity<User> getUserByUsername(@PathVariable String username) {
        User user = userService.getUserByUsername(username);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
        return ResponseEntity.ok(user);
    }

    // 新增修改个人信息接口
    @PutMapping("/{id}/update")
    public ResponseEntity<Boolean> updateUserInfo(
            @PathVariable int id,
            @RequestParam(required = false) String sex,
            @RequestParam(required = false) Integer age,
            @RequestParam(required = false) String email) {

        boolean isUpdated = userService.updateUserInfo(id, sex, age, email);
        if (isUpdated) {
            return ResponseEntity.ok(true); // 更新成功
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(false); // 用户不存在或更新失败
        }
    }
}
