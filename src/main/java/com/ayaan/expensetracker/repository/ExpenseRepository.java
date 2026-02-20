package com.ayaan.expensetracker.repository;

import com.ayaan.expensetracker.entity.Expense;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ExpenseRepository extends JpaRepository<Expense, Long> {

    // find all expenses for a given category
    List<Expense> findByCategory(Expense.Category category);

    @Query("SELECT e.category, SUM(e.amount) FROM Expense e GROUP BY e.category")
    List<Object[]> findTotalsByCategory();

    @Query("SELECT e FROM Expense e WHERE (:category IS NULL OR e.category = :category)")
    List<Expense> searchByCategory(@Param("category") Expense.Category category);
}
