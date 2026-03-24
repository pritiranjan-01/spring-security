package org.prm.security;

import java.util.List;

import org.prm.entity.Employee;
import org.prm.repository.EmployeeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private EmployeeRepository employeeRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Employee emp = employeeRepository.findByUsername(username);
        if (emp == null) throw new UsernameNotFoundException("User not found");
        return new User(
                emp.getUsername(),
                emp.getPassword(), List.of(new SimpleGrantedAuthority("ROLE_USER"))
        );
    }
}
