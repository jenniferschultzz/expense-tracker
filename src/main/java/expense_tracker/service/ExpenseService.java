package expense_tracker.service;

import expense_tracker.dto.ExpenseRequest;
import expense_tracker.dto.ExpenseResponse;
import expense_tracker.exception.NotFoundException;
import expense_tracker.model.Category;
import expense_tracker.model.Expense;
import expense_tracker.repository.CategoryRepository;
import expense_tracker.repository.CategoryTotal;
import expense_tracker.repository.ExpenseRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class ExpenseService {

    private final ExpenseRepository expenses;
    private final CategoryRepository categories;

    public ExpenseService(ExpenseRepository expenses, CategoryRepository categories) {
        this.expenses = expenses;
        this.categories = categories;
    }

    @Transactional
    public ExpenseResponse create(ExpenseRequest req) {
        Category category = findCategory(req.categoryId());
        Expense expense = new Expense(req.description(), req.amount(), req.date(), category);
        return toResponse(expenses.save(expense));
    }

    @Transactional(readOnly = true)
    public ExpenseResponse get(Long id) {
        return toResponse(findExpense(id));
    }

    @Transactional(readOnly = true)
    public List<ExpenseResponse> list(Long categoryId, LocalDate from, LocalDate to) {
        List<Expense> result;
        if (categoryId != null) {
            result = expenses.findByCategoryId(categoryId);
        } else if (from != null && to != null) {
            result = expenses.findByDateBetween(from, to);
        } else {
            result = expenses.findAll();
        }
        return result.stream().map(this::toResponse).toList();
    }

    @Transactional
    public ExpenseResponse update(Long id, ExpenseRequest req) {
        Expense expense = findExpense(id);
        Category category = findCategory(req.categoryId());
        expense.setDescription(req.description());
        expense.setAmount(req.amount());
        expense.setDate(req.date());
        expense.setCategory(category);
        return toResponse(expense);   // changes are saved automatically when the transaction commits
    }

    @Transactional
    public void delete(Long id) {
        Expense expense = findExpense(id);
        expenses.delete(expense);
    }

    @Transactional(readOnly = true)
    public List<CategoryTotal> totalsByCategory() {
        return expenses.totalsByCategory();
    }

    // ---- helpers ----

    private Expense findExpense(Long id) {
        return expenses.findById(id)
                .orElseThrow(() -> new NotFoundException("Expense " + id + " not found"));
    }

    private Category findCategory(Long id) {
        return categories.findById(id)
                .orElseThrow(() -> new NotFoundException("Category " + id + " not found"));
    }

    private ExpenseResponse toResponse(Expense e) {
        return new ExpenseResponse(
                e.getId(), e.getDescription(), e.getAmount(), e.getDate(), e.getCategory().getName());
    }
}
