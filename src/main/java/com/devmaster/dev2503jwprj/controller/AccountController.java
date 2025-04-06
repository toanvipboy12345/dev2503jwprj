package com.devmaster.dev2503jwprj.controller;

import com.devmaster.dev2503jwprj.entity.Customer;
import com.devmaster.dev2503jwprj.service.CustomerService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/account")
public class AccountController {

    @Autowired
    private CustomerService customerService;

    @GetMapping("/login")
    public String login(Model model) {
        return "account/login";
    }

    @GetMapping("/register")
    public String showRegisterForm(Model model) {
        model.addAttribute("customer", new Customer());
        return "account/registration";
    }

    @PostMapping("/register")
    public String registerCustomer(@Valid @ModelAttribute("customer") Customer customer, 
                                   BindingResult result, Model model) {
        if (result.hasErrors()) {
            return "account/registration";
        }

        try {
            customerService.registerCustomer(customer);
            return "redirect:/account/login?success";
        } catch (RuntimeException e) {
            model.addAttribute("error", e.getMessage());
            return "account/registration";
        }
    }
}