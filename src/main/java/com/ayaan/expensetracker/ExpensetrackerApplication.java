package com.ayaan.expensetracker;

import com.ayaan.expensetracker.entity.Expense;
import com.ayaan.expensetracker.entity.Expense.Category;
import com.ayaan.expensetracker.service.ExpenseService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.time.LocalDate;

@SpringBootApplication
public class ExpensetrackerApplication {

	public static void main(String[] args) {
		SpringApplication.run(ExpensetrackerApplication.class, args);
	}

	@Bean
	public CommandLineRunner dataLoader(ExpenseService expenseService) {
		return args -> {
			if (expenseService.getAllExpenses().isEmpty()) {
				Expense e = new Expense();
				e.setDescription("Test Lunch");
				e.setAmount(150.0);
				e.setDate(LocalDate.now());
				e.setCategory(Category.FOOD);
				expenseService.saveExpense(e);
			}
		};
	}
}
