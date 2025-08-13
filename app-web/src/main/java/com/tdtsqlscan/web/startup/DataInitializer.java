package com.tdtsqlscan.web.startup;

import com.tdtsqlscan.web.domain.User;
import com.tdtsqlscan.web.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class DataInitializer {

    @Bean
    public CommandLineRunner initialUserData(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            if (userRepository.count() == 0) {
                String rawPassword = UUID.randomUUID().toString().substring(0, 12);

                User adminUser = new User();
                adminUser.setUsername("Azriel");
                adminUser.setEmail("el.sirviente.de.los.huesos@gmail.com");
                adminUser.setPassword(passwordEncoder.encode(rawPassword));
                adminUser.setRoles("ADMIN");
                adminUser.setEnabled(true);
                adminUser.setMustChangePassword(true);

                userRepository.save(adminUser);

                System.out.println("\n\n************************************************************");
                System.out.println("*                                                          *");
                System.out.println("*      Default admin user created successfully.            *");
                System.out.println("*      Username: Azriel                                    *");
                System.out.printf("*      Password: %-12s                            *\n", rawPassword);
                System.out.println("*      Please change this password on first login.         *");
                System.out.println("*                                                          *");
                System.out.println("************************************************************\n\n");
            }
        };
    }
}
