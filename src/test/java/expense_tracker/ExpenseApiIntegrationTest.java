package expense_tracker;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
class ExpenseApiIntegrationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:16");

    @Autowired
    MockMvc mockMvc;

    private long createCategory() throws Exception {
        String body = mockMvc.perform(post("/api/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Cat-" + UUID.randomUUID() + "\"}"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return ((Number) JsonPath.read(body, "$.id")).longValue();
    }

    @Test
    void createExpense_thenReadItBack() throws Exception {
        long categoryId = createCategory();

        String created = mockMvc.perform(post("/api/expenses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"description":"Lunch","amount":12.50,
                                 "date":"2026-11-04","categoryId":%d}
                                """.formatted(categoryId)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.description").value("Lunch"))
                .andReturn().getResponse().getContentAsString();

        long expenseId = ((Number) JsonPath.read(created, "$.id")).longValue();

        mockMvc.perform(get("/api/expenses/" + expenseId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.description").value("Lunch"))
                .andExpect(jsonPath("$.amount").value(12.50));
    }

    @Test
    void createExpense_withInvalidBody_returns400() throws Exception {
        mockMvc.perform(post("/api/expenses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"description":"","amount":-5,
                                 "date":"2026-11-04","categoryId":1}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.description").exists())
                .andExpect(jsonPath("$.amount").exists());
    }

    @Test
    void getExpense_whenMissing_returns404() throws Exception {
        mockMvc.perform(get("/api/expenses/999999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    void deleteCategory_withExpenses_returns409() throws Exception {
        long categoryId = createCategory();

        mockMvc.perform(post("/api/expenses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"description":"Taxi","amount":20,
                                 "date":"2026-11-04","categoryId":%d}
                                """.formatted(categoryId)))
                .andExpect(status().isCreated());

        mockMvc.perform(delete("/api/categories/" + categoryId))
                .andExpect(status().isConflict());
    }
}
