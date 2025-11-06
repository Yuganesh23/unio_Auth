package service;

import java.util.Collections;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import entity.SignUpEntity;
import repository.SignUpRepository;

@Service("serviceCustomUserDetailsService")
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private SignUpRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        SignUpEntity user = userRepository.findByEmail(email)
            .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));

        // You can add roles/authorities here if you implement roles later
        return new User(user.getEmail(), user.getPassword(), Collections.emptyList());
    }
}
