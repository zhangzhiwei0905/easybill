package com.easybill.service;

import com.easybill.dto.LoginRequest;
import com.easybill.dto.LoginResponse;
import com.easybill.dto.UserDTO;
import com.easybill.entity.User;
import com.easybill.repository.UserRepository;
import com.easybill.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;

    public LoginResponse login(LoginRequest request) {
        try {
            // 验证用户名和密码
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getUsername(),
                            request.getPassword()
                    )
            );

            // 获取用户信息
            User user = userRepository.findByUsername(request.getUsername())
                    .orElseThrow(() -> new RuntimeException("User not found"));

            // 生成 JWT Token
            String token = jwtUtil.generateToken(user.getUsername(), user.getId());

            // 构建响应
            UserDTO userDTO = UserDTO.builder()
                    .id(user.getId())
                    .username(user.getUsername())
                    .email(user.getEmail())
                    .phone(user.getPhone())
                    .apiKey(user.getApiKey())
                    .build();

            return LoginResponse.builder()
                    .success(true)
                    .token(token)
                    .user(userDTO)
                    .message("登录成功")
                    .build();

        } catch (AuthenticationException e) {
            return LoginResponse.builder()
                    .success(false)
                    .message("用户名或密码错误")
                    .build();
        }
    }

    public UserDTO getCurrentUser(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return UserDTO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .phone(user.getPhone())
                .apiKey(user.getApiKey())
                .build();
    }
}
