package com.ayaan.expensetracker.controller;

import com.ayaan.expensetracker.service.ExpenseService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.time.LocalDate;

@Controller
public class AnalyticsController {

    private final ExpenseService expenseService;

    public AnalyticsController(ExpenseService expenseService) {
        this.expenseService = expenseService;
    }

    @GetMapping("/analytics")
    public String analytics(Model model) {
        int currentYear = LocalDate.now().getYear();

        model.addAttribute("monthlyData", expenseService.getMonthlyTotals(currentYear));
        model.addAttribute("categoryData", expenseService.getTotalsByCategory());
        model.addAttribute("currentYear", currentYear);

        return "analytics";
    }
}