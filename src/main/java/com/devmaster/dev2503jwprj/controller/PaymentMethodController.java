package com.devmaster.dev2503jwprj.controller;

import com.devmaster.dev2503jwprj.entity.PaymentMethod;
import com.devmaster.dev2503jwprj.service.PaymentMethodService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/admin/payment")
public class PaymentMethodController {

    private static final Logger logger = LoggerFactory.getLogger(PaymentMethodController.class);

    @Autowired
    private PaymentMethodService paymentMethodService;

    @GetMapping("/list")
    public String listPaymentMethods(Model model, @RequestParam(value = "keyword", required = false) String keyword) {
        logger.info("Truy cập endpoint /admin/payment/list với keyword: {}", keyword);
        List<PaymentMethod> paymentMethods;
        if (keyword != null && !keyword.trim().isEmpty()) {
            paymentMethods = paymentMethodService.searchPaymentMethods(keyword);
            model.addAttribute("keyword", keyword);
        } else {
            paymentMethods = paymentMethodService.getAllPaymentMethods();
        }
        model.addAttribute("paymentMethods", paymentMethods);
        model.addAttribute("breadcrumb", "Phương Thức Thanh Toán");
        logger.info("Trả về template admin/payment/list với số lượng phương thức: {}", paymentMethods.size());
        return "admin/payment/list";
    }

    @GetMapping("/add")
    public String showAddForm(Model model) {
        model.addAttribute("paymentMethod", new PaymentMethod());
        model.addAttribute("breadcrumb", "Thêm Phương Thức Thanh Toán");
        return "admin/payment/add";
    }

    @PostMapping("/add")
    public String addPaymentMethod(@ModelAttribute("paymentMethod") PaymentMethod paymentMethod) {
        paymentMethodService.createPaymentMethod(paymentMethod);
        return "redirect:/admin/payment/list";
    }

    @PostMapping("/edit/{id}")
    public String updatePaymentMethod(@PathVariable("id") Long id, @ModelAttribute("paymentMethod") PaymentMethod paymentMethod) {
        paymentMethod.setId(id);
        paymentMethodService.updatePaymentMethod(paymentMethod);
        return "redirect:/admin/payment/list";
    }

    @PostMapping("/delete/{id}")
    public String deletePaymentMethod(@PathVariable("id") Long id) {
        paymentMethodService.deletePaymentMethod(id);
        return "redirect:/admin/payment/list";
    }
}