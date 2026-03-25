package com.ayaan.expensetracker.service;

import com.ayaan.expensetracker.entity.Category;
import com.ayaan.expensetracker.entity.Expense;
import com.ayaan.expensetracker.entity.User;
import com.ayaan.expensetracker.repository.ExpenseRepository;
import com.ayaan.expensetracker.repository.UserRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

@Service
@Transactional
public class ExpenseService {

    private final ExpenseRepository expenseRepository;
    private final UserRepository userRepository;

    public ExpenseService(ExpenseRepository expenseRepository, UserRepository userRepository) {
        this.expenseRepository = expenseRepository;
        this.userRepository = userRepository;
    }

    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new IllegalStateException("No authenticated user in security context.");
        }
        String username = auth.getName();
        User user = userRepository.findByUsername(username);
        if (user == null) {
            throw new IllegalStateException("Authenticated user not found in database: " + username);
        }
        return user;
    }

    public List<Expense> getAllExpenses() {
        return expenseRepository.findAllWithCategoryByUserId(getCurrentUser().getId());      // ✅ JOIN FETCH
    }

    public Page<Expense> getExpensesPage(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return expenseRepository.findAllWithCategoryByUserId(getCurrentUser().getId(), pageable); // ✅ JOIN FETCH
    }

    public List<Expense> getExpensesByDateRange(LocalDate startDate, LocalDate endDate) {
        return expenseRepository.findByDateBetweenAndUserId(startDate, endDate, getCurrentUser().getId()); // already has JOIN FETCH
    }

    public List<Expense> getExpensesByCategory(Category category) {
        return expenseRepository.searchByCategoryAndUserId(category, getCurrentUser().getId());
    }

    @CacheEvict(
            value = "categoryTotals",
            key = "T(org.springframework.security.core.context.SecurityContextHolder).getContext().getAuthentication()?.getName()"
    )
    public Expense saveExpense(Expense expense) {
        User currentUser = getCurrentUser();

        // Update flow: ensure the expense belongs to the current user.
        if (expense.getId() != null) {
            Expense existing = expenseRepository.findByIdAndUserId(expense.getId(), currentUser.getId())
                    .orElseThrow(() -> new com.ayaan.expensetracker.exception.ExpenseNotFoundException(expense.getId()));

            existing.setDescription(expense.getDescription());
            existing.setAmount(expense.getAmount());
            if (expense.getDate() != null) {
                existing.setDate(expense.getDate());
            }
            if (expense.getCategory() != null) {
                existing.setCategory(expense.getCategory());
            }
            existing.setUser(currentUser);
            return expenseRepository.save(existing);
        }

        // Create flow
        expense.setUser(currentUser);
        return expenseRepository.save(expense);
    }

    @CacheEvict(
            value = "categoryTotals",
            key = "T(org.springframework.security.core.context.SecurityContextHolder).getContext().getAuthentication()?.getName()"
    )
    public void deleteExpense(Long id) {
        User currentUser = getCurrentUser();
        Expense existing = expenseRepository.findByIdAndUserId(id, currentUser.getId())
                .orElseThrow(() -> new com.ayaan.expensetracker.exception.ExpenseNotFoundException(id));
        expenseRepository.delete(existing);
    }

    public Expense getExpenseById(Long id) {
        User currentUser = getCurrentUser();
        return expenseRepository.findByIdAndUserId(id, currentUser.getId())
                .orElseThrow(() -> new com.ayaan.expensetracker.exception.ExpenseNotFoundException(id));
    }

    public Expense createEmptyExpense() {
        return new Expense();
    }

    @Cacheable(
            value = "categoryTotals",
            key = "T(org.springframework.security.core.context.SecurityContextHolder).getContext().getAuthentication()?.getName()"
    )
    public Map<String, BigDecimal> getTotalsByCategory() {
        List<Object[]> rows = expenseRepository.findTotalsByCategoryForUser(getCurrentUser().getId());
        Map<String, BigDecimal> map = new LinkedHashMap<>();
        for (Object[] row : rows) {
            String cat = row[0].toString();
            BigDecimal total = (row[1] instanceof BigDecimal)
                    ? (BigDecimal) row[1]
                    : new BigDecimal(row[1].toString());
            map.put(cat, total);
        }
        return map;
    }

    public List<BigDecimal> getMonthlyTotals(int year) {
        List<Object[]> rows = expenseRepository.findMonthlyTotalsForUser(year, getCurrentUser().getId());
        Map<Integer, BigDecimal> map = new HashMap<>();
        for (Object[] row : rows) {
            int month = ((Number) row[0]).intValue();
            BigDecimal total = (BigDecimal) row[1];
            map.put(month, total);
        }
        List<BigDecimal> result = new ArrayList<>();
        for (int m = 1; m <= 12; m++) {
            result.add(map.getOrDefault(m, BigDecimal.ZERO));
        }
        return result;
    }
}