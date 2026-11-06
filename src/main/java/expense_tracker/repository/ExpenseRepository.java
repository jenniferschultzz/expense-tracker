package expense_tracker.repository;

import expense_tracker.model.Expense;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;

public interface ExpenseRepository extends JpaRepository<Expense, Long> {

    List<Expense> findByCategoryId(Long categoryId);

    List<Expense> findByDateBetween(LocalDate from, LocalDate to);

    @Query("""
        select e.category.name as category, sum(e.amount) as total
        from Expense e
        group by e.category.name
        """)
    List<CategoryTotal> totalsByCategory();
}
