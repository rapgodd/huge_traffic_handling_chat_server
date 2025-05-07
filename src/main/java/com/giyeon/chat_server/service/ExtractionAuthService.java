package com.giyeon.chat_server.service;

import com.giyeon.chat_server.repository.main.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class ExtractionAuthService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
         return userRepository.findByEmail(email)
                 .orElseThrow(()-> new UsernameNotFoundException("User not found with email: " + email));

    }


}
