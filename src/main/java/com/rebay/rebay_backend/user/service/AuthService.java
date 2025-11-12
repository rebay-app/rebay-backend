package com.rebay.rebay_backend.user.service;

import com.rebay.rebay_backend.user.dto.AuthResponse;
import com.rebay.rebay_backend.user.dto.LoginRequest;
import com.rebay.rebay_backend.user.dto.RegisterRequest;
import com.rebay.rebay_backend.user.dto.UserDto;
import com.rebay.rebay_backend.user.entity.AuthProvider;
import com.rebay.rebay_backend.user.entity.User;
import com.rebay.rebay_backend.user.exception.AuthenticationException;
import com.rebay.rebay_backend.user.exception.BadRequestException;
import com.rebay.rebay_backend.user.exception.UserAlreadyExistsException;
import com.rebay.rebay_backend.user.repository.UserRepository;
import com.rebay.rebay_backend.user.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final AuthenticationManager authenticationManager;

    public AuthResponse register(RegisterRequest request) {
        if(userRepository.existsByUsername(request.getUsername())) {
            throw new UserAlreadyExistsException("동일한 username이 존재합니다.");
            //throw new UserAlreadyExistsException("Username already exists");
        }

        if(userRepository.existsByEmail(request.getEmail())) {
            throw new UserAlreadyExistsException("동일한 email이 존재합니다.");
            //throw new UserAlreadyExistsException("Email already exists");
        }

        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .fullName(request.getFullName())
                .provider(AuthProvider.LOCAL)
                .build();

        user = userRepository.save(user);

        String jwtToken = jwtService.generateToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);

        return AuthResponse.builder()
                .accessToken(jwtToken)
                .refreshToken(refreshToken)
                .user(UserDto.fromEntity(user))
                .build();
    }

    public AuthResponse authenticate(LoginRequest request) {
        try {
            String loginId = request.getEmail() != null ? request.getEmail() : request.getUsername();

            User user = userRepository.findByEmail(loginId)
                    .or(() -> userRepository.findByUsername(loginId))
                    .orElseThrow(() -> new AuthenticationException("로그인 ID가 유효하지 않습니다."));
                                        //new AuthenticationException("Authentication failed"));

            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginId,
                            request.getPassword()
                    )
            );

            String jwtToken = jwtService.generateToken(user);
            String refreshToken = jwtService.generateRefreshToken(user);

            return AuthResponse.builder()
                    .accessToken(jwtToken)
                    .refreshToken(refreshToken)
                    .user(UserDto.fromEntity(user))
                    .build();
        } catch (BadRequestException e) {
            throw new AuthenticationException("로그인 ID와 비밀번호가 유효하지 않습니다.");
            //throw new AuthenticationException("Invalid email or password");
        }
    }
}
