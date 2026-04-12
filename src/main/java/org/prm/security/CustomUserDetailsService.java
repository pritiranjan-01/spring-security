package org.prm.security;

import java.util.List;

import org.prm.entity.User;
import org.prm.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User emp = userRepository.findByUsername(username);
        if (emp == null) throw new UsernameNotFoundException("User not found");
        return new org.springframework.security.core.userdetails.User(
                emp.getUsername(),
                emp.getPassword(), List.of(new SimpleGrantedAuthority("ROLE_USER"))
        );
    }
}
