package com.giyeon.chat_server.entity.main;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

@Table(name = "users")
@Entity
@Getter
@NoArgsConstructor
public class User implements UserDetails {

    @Id
    private Long id;
    private String name;
    private String email;
    private String password;
    private String userImageUrl;
    private String refreshToken;
    @Enumerated(EnumType.STRING)
    private Role userRole;

    public void updateRefreshToken(String refreshToken){
        this.refreshToken = refreshToken;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of();
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Builder
    public User(Long id, String name, String email, String password, Role userRole) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.password = password;
        this.userRole = userRole;
    }
}
