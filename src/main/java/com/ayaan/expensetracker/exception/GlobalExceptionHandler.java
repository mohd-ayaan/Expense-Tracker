package com.ayaan.expensetracker.exception;

import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(ExpenseNotFoundException.class)
    public String handleExpenseNotFound(ExpenseNotFoundException ex, Model model) {
        model.addAttribute("errorMessage", ex.getMessage());
        return "error/expense-not-found";
    }

    @ExceptionHandler(Exception.class)
    public String handleGenericException(Exception ex, Model model) {
        model.addAttribute("errorMessage", "An unexpected error occurred.");
        return "error/generic-error";
    }
}
