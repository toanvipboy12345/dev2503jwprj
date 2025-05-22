package com.devmaster.dev2503jwprj.controller;

import com.devmaster.dev2503jwprj.entity.TransportMethod;
import com.devmaster.dev2503jwprj.service.TransportMethodService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/admin/transport")
public class TransportMethodController {

    private static final Logger logger = LoggerFactory.getLogger(TransportMethodController.class);

    @Autowired
    private TransportMethodService transportMethodService;

    @GetMapping("/list")
    public String listTransportMethods(Model model, @RequestParam(value = "keyword", required = false) String keyword) {
        logger.info("Truy cập endpoint /admin/transport/list với keyword: {}", keyword);
        List<TransportMethod> transportMethods;
        if (keyword != null && !keyword.trim().isEmpty()) {
            transportMethods = transportMethodService.searchTransportMethods(keyword);
            model.addAttribute("keyword", keyword);
        } else {
            transportMethods = transportMethodService.getAllTransportMethods();
        }
        model.addAttribute("transportMethods", transportMethods);
        model.addAttribute("breadcrumb", "Phương Thức Vận Chuyển");
        logger.info("Trả về template admin/transport/list với số lượng phương thức: {}", transportMethods.size());
        return "admin/transport/list";
    }

    @GetMapping("/add")
    public String showAddForm(Model model) {
        model.addAttribute("transportMethod", new TransportMethod());
        model.addAttribute("breadcrumb", "Thêm Phương Thức Vận Chuyển");
        return "admin/transport/add";
    }

    @PostMapping("/add")
    public String addTransportMethod(@ModelAttribute("transportMethod") TransportMethod transportMethod) {
        transportMethodService.createTransportMethod(transportMethod);
        return "redirect:/admin/transport/list";
    }

    @PostMapping("/edit/{id}")
    public String updateTransportMethod(@PathVariable("id") Long id, @ModelAttribute("transportMethod") TransportMethod transportMethod) {
        transportMethod.setId(id);
        transportMethodService.updateTransportMethod(transportMethod);
        return "redirect:/admin/transport/list";
    }

    @PostMapping("/delete/{id}")
    public String deleteTransportMethod(@PathVariable("id") Long id) {
        transportMethodService.deleteTransportMethod(id);
        return "redirect:/admin/transport/list";
    }
}