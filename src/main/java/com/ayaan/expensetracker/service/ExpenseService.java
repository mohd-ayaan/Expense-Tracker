package com.ayaan.expensetracker.service;

import com.ayaan.expensetracker.entity.Expense;
import com.ayaan.expensetracker.repository.ExpenseRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.Map;
import java.util.HashMap;

@Service
@Transactional
public class ExpenseService {

    private final ExpenseRepository expenseRepository;

    public ExpenseService(ExpenseRepository expenseRepository) {
        this.expenseRepository = expenseRepository;
    }

    public List<Expense> getAllExpenses() {
        return expenseRepository.findAll();
    }

    public Expense saveExpense(Expense expense) {
        return expenseRepository.save(expense);
    }

    public Expense createEmptyExpense() {
        return new Expense();
    }

    public Expense getExpenseById(Long id) {
        return expenseRepository.findById(id).orElseThrow();
    }

    public void deleteExpense(Long id) {
        expenseRepository.deleteById(id);
    }

    public List<Expense> getExpensesByCategory(Expense.Category category) {
        return expenseRepository.searchByCategory(category);
    }

    public Map<String, Double> getTotalsByCategory() {
        List<Object[]> rows = expenseRepository.findTotalsByCategory();
        Map<String, Double> map = new HashMap<>();
        for (Object[] row : rows) {
            String cat = row[0].toString();
            Double total = ((Number) row[1]).doubleValue();
            map.put(cat, total);
        }
        return map;
    }
}
