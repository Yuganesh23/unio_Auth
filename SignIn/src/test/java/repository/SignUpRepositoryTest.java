package repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.SpringBootTest;

import entity.SignUpEntity;
import main.SignInApplication;

@DataJpaTest
@SpringBootTest(classes = SignInApplication.class)

class SignUpRepositoryTest {

    @Autowired
    private SignUpRepository signUpRepository;

    @Test
    void testFindByEmail() {
        // Create a new SignUpEntity object
        SignUpEntity signUpEntity = new SignUpEntity();
        signUpEntity.setName("John Doe");
        signUpEntity.setEmail("john.doe@example.com");
        signUpEntity.setPassword("password123");

        // Save the entity in the repository
        signUpRepository.save(signUpEntity);

        // Fetch the entity by email
        SignUpEntity found = signUpRepository.findByEmail("john.doe@example.com").orElse(null);

        // Assert that the entity is not null
        assertNotNull(found);

        // Assert that the retrieved entity has the correct email and name
        assertEquals("john.doe@example.com", found.getEmail());
        assertEquals("John Doe", found.getName());
        assertEquals("password123", found.getPassword());
    }

    @Test
    void testFindByEmailNotFound() {
        // Try to find a non-existing email
        SignUpEntity found = signUpRepository.findByEmail("non.existing@example.com").orElse(null);

        // Assert that the result is null because the email doesn't exist
        assertNull(found);
    }
}
