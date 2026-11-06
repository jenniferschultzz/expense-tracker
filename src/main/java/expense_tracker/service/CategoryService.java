package expense_tracker.service;

import expense_tracker.dto.CategoryRequest;
import expense_tracker.exception.NotFoundException;
import expense_tracker.model.Category;
import expense_tracker.repository.CategoryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CategoryService {

    private final CategoryRepository categories;

    public CategoryService(CategoryRepository categories) {
        this.categories = categories;
    }

    @Transactional
    public Category create(CategoryRequest req) {
        return categories.save(new Category(req.name()));
    }

    @Transactional(readOnly = true)
    public List<Category> list() {
        return categories.findAll();
    }

    @Transactional
    public void delete(Long id) {
        if (!categories.existsById(id)) {
            throw new NotFoundException("Category " + id + " not found");
        }
        categories.deleteById(id);
    }
}
