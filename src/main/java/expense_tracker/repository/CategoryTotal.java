package expense_tracker.repository;

import java.math.BigDecimal;

public interface CategoryTotal {
    String getCategory();
    BigDecimal getTotal();
}
