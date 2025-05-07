package com.giyeon.chat_server.entity.main;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.ToString;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

@Table(name = "users")
@Entity
@Getter
public class User implements UserDetails {

    @Id
    private Long id;

    private String email;
    private String password;
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
}
