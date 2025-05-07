package com.giyeon.chat_server.repository.main;

import com.giyeon.chat_server.entity.main.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    @Query("SELECT u FROM User u WHERE u.refreshToken = :refreshToken")
    Optional<User> areTokensEqual(String refreshToken);

    Optional<UserDetails> findByEmail(String email);
}
