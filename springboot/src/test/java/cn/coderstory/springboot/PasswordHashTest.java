package cn.coderstory.springboot;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

class PasswordHashTest {

    @Test
    void generatePasswordHash() {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(10);
        String hash = encoder.encode("admin123");
        System.out.println("Password: admin123");
        System.out.println("Hash: " + hash);
        System.out.println("Matches: " + encoder.matches("admin123", hash));
    }
}
