package com.ayaan.expensetracker.controller;

import com.ayaan.expensetracker.entity.Expense;
import com.ayaan.expensetracker.service.ExpenseService;
import com.ayaan.expensetracker.service.CategoryService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@Controller
@RequestMapping("/expenses")
public class ExpenseController {

    private final ExpenseService expenseService;
    private final CategoryService categoryService;

    public ExpenseController(ExpenseService expenseService, CategoryService categoryService) {
        this.expenseService = expenseService;
        this.categoryService = categoryService;
    }

    @GetMapping
    public String listExpenses(
            @RequestParam(name = "category", required = false) String category,
            @RequestParam(name = "page", required = false, defaultValue = "0") int page,
            @RequestParam(name = "startDate", required = false) String startDateParam,
            @RequestParam(name = "endDate", required = false) String endDateParam,
            Model model) {

        int size = 10;
        LocalDate startDate = null;
        LocalDate endDate = null;

        if (startDateParam != null && endDateParam != null) {
            try {
                startDate = LocalDate.parse(startDateParam);
                endDate = LocalDate.parse(endDateParam);
            } catch (Exception e) {
                // leave null
            }
        }

        Page<Expense> expensePage;

        if (category != null && !category.isBlank()) {
            // ✅ Filter by category name
            com.ayaan.expensetracker.entity.Category cat = categoryService.getCategoryByName(category);
            if (cat != null) {
                List<Expense> filtered = expenseService.getExpensesByCategory(cat);
                // Apply date filter on top if needed
                if (startDate != null && endDate != null) {
                    LocalDate finalStartDate = startDate;
                    LocalDate finalEndDate = endDate;
                    filtered = filtered.stream()
                            .filter(e -> e.getDate() != null
                                    && !e.getDate().isBefore(finalStartDate)
                                    && !e.getDate().isAfter(finalEndDate))
                            .toList();
                }
                int total = filtered.size();
                int startIndex = page * size;
                int endIndex = Math.min(startIndex + size, total);
                List<Expense> pageContent = (startIndex >= total) ? List.of() : filtered.subList(startIndex, endIndex);
                expensePage = new org.springframework.data.domain.PageImpl<>(pageContent, PageRequest.of(page, size), total);
            } else {
                expensePage = expenseService.getExpensesPage(page, size);
            }
        } else if (startDate != null && endDate != null) {
            // Date range filter only
            List<Expense> filtered = expenseService.getExpensesByDateRange(startDate, endDate);
            int total = filtered.size();
            int startIndex = page * size;
            int endIndex = Math.min(startIndex + size, total);
            List<Expense> pageContent = (startIndex >= total) ? List.of() : filtered.subList(startIndex, endIndex);
            expensePage = new org.springframework.data.domain.PageImpl<>(pageContent, PageRequest.of(page, size), total);
        } else {
            // No filters — paginated
            expensePage = expenseService.getExpensesPage(page, size);
        }

        model.addAttribute("expensePage", expensePage);
        model.addAttribute("expense", new Expense());
        model.addAttribute("selectedCategory", category);
        model.addAttribute("categories", categoryService.getAllCategories());
        model.addAttribute("totals", expenseService.getTotalsByCategory());
        model.addAttribute("startDate", startDateParam);
        model.addAttribute("endDate", endDateParam);

        return "expenses/list";
    }

    @PostMapping
    public String addExpense(
            @Valid @ModelAttribute("expense") Expense expense,
            BindingResult bindingResult,
            Model model) {

        if (bindingResult.hasErrors()) {
            // ✅ All attributes the template needs
            model.addAttribute("expensePage", expenseService.getExpensesPage(0, 10));
            model.addAttribute("categories", categoryService.getAllCategories());
            model.addAttribute("totals", expenseService.getTotalsByCategory());
            return "expenses/list";
        }

        expenseService.saveExpense(expense);
        return "redirect:/expenses";
    }

    @GetMapping("/{id}/edit")
    public String editExpense(@PathVariable Long id, Model model) {
        Expense existing = expenseService.getExpenseById(id);
        model.addAttribute("expense", existing);
        model.addAttribute("expensePage", expenseService.getExpensesPage(0, 10));
        model.addAttribute("categories", categoryService.getAllCategories());
        model.addAttribute("totals", expenseService.getTotalsByCategory());
        model.addAttribute("editMode", true);
        return "expenses/list";
    }

    @PostMapping("/{id}/delete")
    public String deleteExpense(@PathVariable Long id) {
        expenseService.deleteExpense(id);
        return "redirect:/expenses";
    }
}