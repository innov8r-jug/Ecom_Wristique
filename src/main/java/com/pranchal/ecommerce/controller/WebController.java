package com.pranchal.ecommerce.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class WebController
{
    @GetMapping("/login")
    public String login()
    {
        return "login";
    }

    @GetMapping("/register")
    public String register()
    {
        return "register";
    }

    @GetMapping("/profile")
    public String profile()
    {
        return "profile";
    }

    @GetMapping("/dashboard")
    public String dashboard(HttpSession session)
    {
        if(session.isNew())
        {
            return "redirect:/login";
        }
        return "dashboard";
    }
}