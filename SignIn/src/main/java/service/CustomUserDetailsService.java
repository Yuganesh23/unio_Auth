package service;

import org.springframework.beans.factory.annotation.Autowired;
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

	public CustomUserDetailsService(SignUpRepository userRepository) {
		super();
		this.userRepository = userRepository;
	}

	@Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        SignUpEntity user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new UsernameNotFoundException("User not found with email: " + email));

        // Simple user details without roles
        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getEmail())
                .password(user.getPassword())
                .authorities("USER") // Simple authority
                .build();
    }
 
}
