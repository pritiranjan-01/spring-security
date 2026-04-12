package org.prm.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
public class HomeController {

    private List<Object> list = new ArrayList<>();

    @GetMapping("/home")
    public String home() {
        return "Hello Spring Security!";
    }

    @GetMapping("/id")
    public String id(HttpServletRequest request) {
        return request.getSession().getId();
    }

    @GetMapping("/csrf")
    public CsrfToken getToken(HttpServletRequest request) {
        return (CsrfToken) request.getAttribute("_csrf");
    }

    @GetMapping("/employees")
    public List<Object> getObject() {
        return list;
    }

    @PostMapping("/add")
    public String addObject(@RequestBody Object object) {
        list.add(object);
      return "Employee added successfully!";
    }
}
