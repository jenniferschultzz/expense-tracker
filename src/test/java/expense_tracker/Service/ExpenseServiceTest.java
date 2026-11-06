package expense_tracker.service;

import expense_tracker.dto.ExpenseRequest;
import expense_tracker.dto.ExpenseResponse;
import expense_tracker.exception.NotFoundException;
import expense_tracker.model.Category;
import expense_tracker.model.Expense;
import expense_tracker.repository.CategoryRepository;
import expense_tracker.repository.ExpenseRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ExpenseServiceTest {

    @Mock ExpenseRepository expenses;
    @Mock CategoryRepository categories;
    @InjectMocks ExpenseService service;

    private final Category food = new Category("Food");
    private final LocalDate day = LocalDate.of(2026, 11, 4);

    private ExpenseRequest request(Long categoryId) {
        return new ExpenseRequest("Lunch", new BigDecimal("12.50"), day, categoryId);
    }

    // ---------- create ----------

    @Test
    void create_savesExpenseAndReturnsResponse() {
        when(categories.findById(1L)).thenReturn(Optional.of(food));
        when(expenses.save(any(Expense.class))).thenAnswer(inv -> inv.getArgument(0));

        ExpenseResponse result = service.create(request(1L));

        assertEquals("Lunch", result.description());
        assertEquals(new BigDecimal("12.50"), result.amount());
        assertEquals("Food", result.category());
        verify(expenses).save(any(Expense.class));
    }

    @Test
    void create_throwsWhenCategoryMissing() {
        when(categories.findById(99L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> service.create(request(99L)));
        verify(expenses, never()).save(any());
    }

    // ---------- get ----------

    @Test
    void get_returnsExpense() {
        when(expenses.findById(1L))
                .thenReturn(Optional.of(new Expense("Lunch", new BigDecimal("12.50"), day, food)));

        ExpenseResponse result = service.get(1L);

        assertEquals("Lunch", result.description());
        assertEquals("Food", result.category());
    }

    @Test
    void get_throwsWhenMissing() {
        when(expenses.findById(5L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> service.get(5L));
    }

    // ---------- list ----------

    @Test
    void list_withNoFilters_returnsEverything() {
        when(expenses.findAll())
                .thenReturn(List.of(new Expense("Lunch", new BigDecimal("12.50"), day, food)));

        List<ExpenseResponse> result = service.list(null, null, null);

        assertEquals(1, result.size());
        verify(expenses).findAll();
    }

    @Test
    void list_withCategory_usesCategoryQuery() {
        when(expenses.findByCategoryId(1L)).thenReturn(List.of());

        service.list(1L, null, null);

        verify(expenses).findByCategoryId(1L);
        verify(expenses, never()).findAll();
    }

    @Test
    void list_withDateRange_usesDateQuery() {
        LocalDate from = day.minusDays(7);
        when(expenses.findByDateBetween(from, day)).thenReturn(List.of());

        service.list(null, from, day);

        verify(expenses).findByDateBetween(from, day);
    }

    // ---------- update ----------

    @Test
    void update_changesAllFields() {
        Expense existing = new Expense("Old", new BigDecimal("1.00"), day.minusDays(1), food);
        Category travel = new Category("Travel");
        when(expenses.findById(5L)).thenReturn(Optional.of(existing));
        when(categories.findById(2L)).thenReturn(Optional.of(travel));

        ExpenseResponse result = service.update(5L, request(2L));

        assertEquals("Lunch", existing.getDescription());
        assertEquals(new BigDecimal("12.50"), existing.getAmount());
        assertEquals("Travel", result.category());
    }

    @Test
    void update_throwsWhenExpenseMissing() {
        when(expenses.findById(5L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> service.update(5L, request(1L)));
    }

    // ---------- delete ----------

    @Test
    void delete_removesExpense() {
        Expense existing = new Expense("Lunch", new BigDecimal("12.50"), day, food);
        when(expenses.findById(1L)).thenReturn(Optional.of(existing));

        service.delete(1L);

        verify(expenses).delete(existing);
    }

    @Test
    void delete_throwsWhenMissing() {
        when(expenses.findById(1L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> service.delete(1L));
        verify(expenses, never()).delete(any());
    }
}
