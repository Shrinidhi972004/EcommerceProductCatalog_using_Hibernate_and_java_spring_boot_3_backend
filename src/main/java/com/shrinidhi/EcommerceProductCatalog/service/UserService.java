package com.shrinidhi.EcommerceProductCatalog.service;

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
        System.out.println("🔐 Attempting to register user: " + user.getUsername());
        System.out.println("🔐 Original password: " + user.getPassword());

        user.setPassword(passwordEncoder.encode(user.getPassword()));

        System.out.println("🔐 Encrypted password: " + user.getPassword());
        System.out.println("🔐 Role: " + user.getRole());

        User savedUser = userRepository.save(user);

        System.out.println("✅ User saved with ID: " + savedUser.getId());

        return savedUser;
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
