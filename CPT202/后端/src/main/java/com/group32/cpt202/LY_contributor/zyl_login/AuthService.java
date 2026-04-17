package com.group32.cpt202.LY_contributor.zyl_login;

import com.group32.cpt202.LY_contributor.entity.User;
import com.group32.cpt202.LY_contributor.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AuthService {

    private final UserRepository userRepository;

    public AuthService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // 注册
    public void register(String username, String password) {
        // 不用 findByUsername！用 findAll 遍历判断（绝对不报错）
        List<User> all = userRepository.findAll();
        for (User u : all) {
            if (u.getUsername().equals(username)) {
                throw new RuntimeException("username already exists");
            }
        }

        User user = new User();
        user.setUsername(username);
        user.setPassword(password);
        user.setRole(User.Role.USER);

        userRepository.save(user);
    }

    // 登录
    public User login(String username, String password) {
        // 不用 findByUsername！用 findAll 遍历判断（绝对不报错）
        List<User> all = userRepository.findAll();
        for (User u : all) {
            if (u.getUsername().equals(username)) {
                if (u.getPassword().equals(password)) {
                    return u;
                } else {
                    throw new RuntimeException("invalid password");
                }
            }
        }
        throw new RuntimeException("user not found");
    }
}
