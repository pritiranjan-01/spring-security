package org.prm.controller;

import org.prm.entity.Employee;
import org.prm.repository.EmployeeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class EmployeeController {

    @Autowired
    private EmployeeRepository employeeRepo;
    private BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(12);


    @PostMapping("/signup")
    public String registration(@RequestBody Employee employee) {
        employee.setPassword(encoder.encode(employee.getPassword()));
        employeeRepo.save(employee);
        return "registration success";
    }
}
