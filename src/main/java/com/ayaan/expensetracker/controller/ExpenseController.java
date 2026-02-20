package com.ayaan.expensetracker.controller;

import com.ayaan.expensetracker.service.ExpenseService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import com.ayaan.expensetracker.entity.Expense;
import jakarta.validation.Valid;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequestMapping("/expenses")
public class ExpenseController {

    private final ExpenseService expenseService;

    public ExpenseController(ExpenseService expenseService) {
        this.expenseService = expenseService;
    }

    @GetMapping
    public String listExpenses(
            @RequestParam(name = "category", required = false) String category,
            Model model) {

        Expense.Category catEnum = null;
        if (category != null && !category.isBlank()) {
            catEnum = Expense.Category.valueOf(category);
        }

        if (catEnum != null) {
            model.addAttribute("expenses", expenseService.getExpensesByCategory(catEnum));
        } else {
            model.addAttribute("expenses", expenseService.getAllExpenses());
        }

        model.addAttribute("expense", new Expense());
        model.addAttribute("selectedCategory", category);
        model.addAttribute("totals", expenseService.getTotalsByCategory());

        return "expenses/list";
    }


    @PostMapping
    public String addExpense(
            @Valid @ModelAttribute("expense") Expense expense,
            BindingResult bindingResult,
            Model model) {

        if (bindingResult.hasErrors()) {
            model.addAttribute("expenses", expenseService.getAllExpenses());
            return "expenses/list";
        }

        expenseService.saveExpense(expense);
        return "redirect:/expenses";
    }

    @GetMapping("/{id}/edit")
    public String editExpense(@PathVariable Long id, Model model) {
        Expense existing = expenseService.getExpenseById(id);
        model.addAttribute("expense", existing);
        model.addAttribute("expenses", expenseService.getAllExpenses());
        model.addAttribute("editMode", true);
        return "expenses/list";
    }

    @PostMapping("/{id}/delete")
    public String deleteExpense(@PathVariable Long id) {
        expenseService.deleteExpense(id);
        return "redirect:/expenses";
    }
}
