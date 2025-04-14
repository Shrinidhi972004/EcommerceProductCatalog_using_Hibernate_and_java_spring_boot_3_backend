package com.shrinidhi.EcommerceProductCatalog.service;

import com.shrinidhi.EcommerceProductCatalog.dto.RegisterRequest;
import com.shrinidhi.EcommerceProductCatalog.dto.ForgotPasswordRequest;
import com.shrinidhi.EcommerceProductCatalog.model.User;
import com.shrinidhi.EcommerceProductCatalog.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserService(UserRepository userRepository, @Lazy PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public User registerUser(User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userRepository.save(user);
    }

    public User registerWithSecurityQuestions(RegisterRequest request) {
        User user = User.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(request.getRole())
                .securityAnswer1(request.getSecurityAnswer1())
                .securityAnswer2(request.getSecurityAnswer2())
                .build();

        return userRepository.save(user);
    }

    public boolean resetPasswordWithSecurityAnswers(ForgotPasswordRequest request) {
        return userRepository.findByUsername(request.getUsername())
                .map(user -> {
                    if (user.getSecurityAnswer1().equalsIgnoreCase(request.getAnswer1().trim())
                            && user.getSecurityAnswer2().equalsIgnoreCase(request.getAnswer2().trim())) {
                        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
                        userRepository.save(user);
                        return true;
                    } else {
                        return false;
                    }
                })
                .orElse(false);
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        GrantedAuthority authority = new SimpleGrantedAuthority(user.getRole());

        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPassword(),
                Collections.singleton(authority)
        );
    }
}
