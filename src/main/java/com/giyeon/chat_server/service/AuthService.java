package com.giyeon.chat_server.service;

import com.giyeon.chat_server.component.IdGenerator;
import com.giyeon.chat_server.dto.AuthTokenDto;
import com.giyeon.chat_server.dto.LoginRequestDto;
import com.giyeon.chat_server.dto.SignupDto;
import com.giyeon.chat_server.entity.main.Role;
import com.giyeon.chat_server.entity.main.User;
import com.giyeon.chat_server.properties.S3Property;
import com.giyeon.chat_server.repository.main.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.List;

import static com.giyeon.chat_server.util.JwtUtil.createAccessToken;
import static com.giyeon.chat_server.util.JwtUtil.createRefreshToken;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManagerBuilder authenticationManagerBuilder;
    private final UserRepository userRepository;
    private final IdGenerator idGenerator;
    private final PasswordEncoder passwordEncoder;
    private final S3Property s3Property;

    private static final Long ACCESS_TOKEN_EXPIRATION_TIME = 1000 * 60 * 30L;
    private static final Long REFRESH_TOKEN_EXPIRATION_TIME = 1000 * 60 * 60 * 24 * 30L;
    private static String JWT_SECRET_KEY;


    @Transactional
    public AuthTokenDto authenticateUser(LoginRequestDto loginRequestDto) {
        String email = loginRequestDto.getEmail();
        String password = loginRequestDto.getPassword();
        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(email, password);

        Authentication authenticate = authenticationManagerBuilder.getObject().authenticate(authenticationToken);
        User user = (User) authenticate.getPrincipal();

        Collection<? extends GrantedAuthority> authorities = authenticate.getAuthorities();
        List<String> roles = authorities.stream()
                .map(GrantedAuthority::getAuthority)
                .toList();

        byte[] bytes = JWT_SECRET_KEY.getBytes(StandardCharsets.UTF_8);
        AuthTokenDto authToken = createAuthDto(user.getId(), roles, bytes);

        user.updateRefreshToken(authToken.getRefreshToken());

        return authToken;
    }

    @Transactional
    public AuthTokenDto reset(String refreshToken) {

        User user = userRepository.areTokensEqual(refreshToken)
                .orElseThrow(()->new RuntimeException("유효하지 않은 토큰입니다."));

        AuthTokenDto authDto = createAuthDto(user.getId(),
                List.of(String.valueOf(user.getUserRole())),
                JWT_SECRET_KEY.getBytes(StandardCharsets.UTF_8));

        user.updateRefreshToken(authDto.getRefreshToken());

        System.out.println(authDto);
        return authDto;

    }

    private AuthTokenDto createAuthDto(Long userId, List<String> roles, byte[] bytes) {
        return AuthTokenDto.builder()
                .grantType("Bearer")
                .accessToken(createAccessToken(userId, roles, bytes,ACCESS_TOKEN_EXPIRATION_TIME))
                .refreshToken(createRefreshToken(userId, bytes, REFRESH_TOKEN_EXPIRATION_TIME))
                .userId(userId)
                .build();
    }

    @Value("${jwt.secret}")
    public void setKey(String key){
        JWT_SECRET_KEY = key;
    }

    @Transactional
    public AuthTokenDto signup(SignupDto signupDto) {

        User user = User.builder()
                .id(idGenerator.nextId())
                .name(signupDto.getName())
                .email(signupDto.getEmail())
                .password(passwordEncoder.encode(signupDto.getPassword()))
                .userRole(Role.ROLE_USER)
                .userImageUrl(s3Property.getS3().getDefaultImage())
                .build();

        userRepository.save(user);

        return AuthTokenDto.builder()
                .userId(user.getId())
                .build();
    }
}
