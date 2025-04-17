package com.devmaster.dev2503jwprj.service;

import com.devmaster.dev2503jwprj.entity.Customer;
import com.devmaster.dev2503jwprj.repository.CustomerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class CustomerService {

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public Customer registerCustomer(Customer customer) {
        Optional<Customer> existingByUsername = customerRepository.findByUsername(customer.getUsername());
        if (existingByUsername.isPresent()) {
            throw new RuntimeException("Tên người dùng đã tồn tại");
        }

        Optional<Customer> existingByEmail = customerRepository.findByEmail(customer.getEmail());
        if (existingByEmail.isPresent()) {
            throw new RuntimeException("Email đã tồn tại");
        }

        customer.setPassword(passwordEncoder.encode(customer.getPassword()));
        customer.setCreatedDate(LocalDateTime.now());
        customer.setIsActive((byte) 1);
        customer.setIsDelete((byte) 0);
        // Không cần set address nếu không bắt buộc, để trống hoặc lấy từ form nếu có

        return customerRepository.save(customer);
    }

    public Optional<Customer> findByUsername(String username) {
        return customerRepository.findByUsername(username)
                .filter(customer -> customer.getIsDelete() == null || customer.getIsDelete() == 0);
    }

    public Optional<Customer> findByEmail(String email) {
        return customerRepository.findByEmail(email)
                .filter(customer -> customer.getIsDelete() == null || customer.getIsDelete() == 0);
    }

    public Optional<Customer> findById(Long id) {
        return customerRepository.findById(id)
                .filter(customer -> customer.getIsDelete() == null || customer.getIsDelete() == 0);
    }
}