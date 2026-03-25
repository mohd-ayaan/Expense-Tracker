package com.ayaan.expensetracker;

import com.ayaan.expensetracker.entity.Category;
import com.ayaan.expensetracker.service.CategoryService;
import org.springframework.cache.annotation.EnableCaching;
import com.ayaan.expensetracker.entity.Expense;
import com.ayaan.expensetracker.entity.User;
import com.ayaan.expensetracker.repository.ExpenseRepository;
import com.ayaan.expensetracker.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
@SpringBootApplication
@EnableCaching
public class ExpensetrackerApplication {

	public static void main(String[] args) {
		SpringApplication.run(ExpensetrackerApplication.class, args);
	}

	@Bean
	public CommandLineRunner dataLoader(CategoryService categoryService,
	                                   ExpenseRepository expenseRepository,
	                                   UserRepository userRepository) {
		return args -> {
			// Seed all categories if they don't exist
			String[] categoryNames = {
					"Housing",
					"Bills",
					"Food & Groceries",
					"Transport",
					"Healthcare",
					"Education",
					"Entertainment",
					"Savings & Investments",
					"Others"
			};
			for (String name : categoryNames) {
				if (categoryService.getCategoryByName(name) == null) {
					Category cat = new Category();
					cat.setName(name);
					categoryService.saveCategory(cat);
				}
			}

			// Seed a test expense only when at least one user exists.
			// (Startup isn't authenticated; per-user scoping lives in ExpenseService.)
			if (expenseRepository.count() == 0 && userRepository.count() > 0) {
				Category foodCategory = categoryService.getCategoryByName("Food & Groceries");
				User anyUser = userRepository.findAll().stream().findFirst().orElse(null);

				if (foodCategory != null && anyUser != null) {
					Expense e = new Expense();
					e.setDescription("Test Lunch");
					e.setAmount(java.math.BigDecimal.valueOf(150.00));
					e.setDate(java.time.LocalDate.now());
					e.setCategory(foodCategory);
					e.setUser(anyUser);
					expenseRepository.save(e);
				}
			}
		};
	}
}