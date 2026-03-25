package com.ayaan.expensetracker.repository;

import com.ayaan.expensetracker.entity.Category;
import com.ayaan.expensetracker.entity.Expense;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;
import java.util.List;

@Repository
public interface ExpenseRepository extends JpaRepository<Expense, Long> {

    // ✅ Eliminates N+1: loads all expenses + categories in one query
    @Query("SELECT e FROM Expense e LEFT JOIN FETCH e.category ORDER BY e.date DESC")
    List<Expense> findAllWithCategory();

    // ----------------------------
    // Per-user queries
    // ----------------------------

    @Query("SELECT e FROM Expense e LEFT JOIN FETCH e.category " +
            "WHERE e.user.id = :userId ORDER BY e.date DESC")
    List<Expense> findAllWithCategoryByUserId(@Param("userId") Long userId);

    // ✅ Paginated version — note: count query must be separate (no FETCH in count)
    @Query(value = "SELECT e FROM Expense e LEFT JOIN FETCH e.category ORDER BY e.date DESC",
            countQuery = "SELECT COUNT(e) FROM Expense e")
    Page<Expense> findAllWithCategory(Pageable pageable);

    @Query(value = "SELECT e FROM Expense e LEFT JOIN FETCH e.category " +
            "WHERE e.user.id = :userId ORDER BY e.date DESC",
            countQuery = "SELECT COUNT(e) FROM Expense e WHERE e.user.id = :userId")
    Page<Expense> findAllWithCategoryByUserId(@Param("userId") Long userId, Pageable pageable);

    // ✅ Date range with JOIN FETCH
    @Query("SELECT e FROM Expense e LEFT JOIN FETCH e.category " +
            "WHERE e.date BETWEEN :startDate AND :endDate ORDER BY e.date DESC")
    List<Expense> findByDateBetween(@Param("startDate") LocalDate startDate,
                                    @Param("endDate") LocalDate endDate);

    @Query("SELECT e FROM Expense e LEFT JOIN FETCH e.category " +
            "WHERE e.user.id = :userId AND e.date BETWEEN :startDate AND :endDate " +
            "ORDER BY e.date DESC")
    List<Expense> findByDateBetweenAndUserId(@Param("startDate") LocalDate startDate,
                                                @Param("endDate") LocalDate endDate,
                                                @Param("userId") Long userId);

    // ✅ Category filter with JOIN FETCH
    @Query("SELECT e FROM Expense e LEFT JOIN FETCH e.category " +
            "WHERE e.category = :category ORDER BY e.date DESC")
    List<Expense> searchByCategory(@Param("category") Category category);

    @Query("SELECT e FROM Expense e LEFT JOIN FETCH e.category " +
            "WHERE e.user.id = :userId AND e.category = :category ORDER BY e.date DESC")
    List<Expense> searchByCategoryAndUserId(@Param("category") Category category,
                                               @Param("userId") Long userId);

    // ✅ Category totals for sidebar
    @Query("SELECT c.name, SUM(e.amount) FROM Expense e JOIN e.category c GROUP BY c.name")
    List<Object[]> findTotalsByCategory();

    @Query("SELECT c.name, SUM(e.amount) FROM Expense e JOIN e.category c " +
            "WHERE e.user.id = :userId GROUP BY c.name")
    List<Object[]> findTotalsByCategoryForUser(@Param("userId") Long userId);

    // ✅ Monthly totals — single query replaces 12 date-range queries
    @Query("SELECT MONTH(e.date), SUM(e.amount) FROM Expense e " +
            "WHERE YEAR(e.date) = :year GROUP BY MONTH(e.date)")
    List<Object[]> findMonthlyTotals(@Param("year") int year);

    @Query("SELECT MONTH(e.date), SUM(e.amount) FROM Expense e " +
            "WHERE e.user.id = :userId AND YEAR(e.date) = :year GROUP BY MONTH(e.date)")
    List<Object[]> findMonthlyTotalsForUser(@Param("year") int year, @Param("userId") Long userId);

    @Query("SELECT e FROM Expense e LEFT JOIN FETCH e.category " +
            "WHERE e.id = :id AND e.user.id = :userId")
    Optional<Expense> findByIdAndUserId(@Param("id") Long id, @Param("userId") Long userId);
}