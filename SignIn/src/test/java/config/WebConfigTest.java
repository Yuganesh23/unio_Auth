package config;



import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import main.SignInApplication;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;

@SpringBootTest(classes = SignInApplication.class)
@AutoConfigureMockMvc
public class WebConfigTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void testCORSConfiguration() throws Exception {
        mockMvc.perform(get("/api/users/1")
                .header("Origin", "http://localhost:8081"))
            .andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:8081"));
    }

}