package ru.kata.spring.boot_security.demo.configs;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import ru.kata.spring.boot_security.demo.dao.RoleDao;
import ru.kata.spring.boot_security.demo.dao.UserDao;
import ru.kata.spring.boot_security.demo.model.Role;
import ru.kata.spring.boot_security.demo.model.User;

import java.util.Set;

@Configuration
public class DataInitializer {

    private final RoleDao roleDao;
    private final UserDao userDao;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(RoleDao roleDao, UserDao userDao, PasswordEncoder passwordEncoder) {
        this.roleDao = roleDao;
        this.userDao = userDao;
        this.passwordEncoder = passwordEncoder;
    }

    @Bean
    public CommandLineRunner init() {
        return args -> {
            Role roleUser = roleDao.findByName("ROLE_USER")
                    .orElseGet(() -> roleDao.save(new Role("ROLE_USER")));

            Role roleAdmin = roleDao.findByName("ROLE_ADMIN")
                    .orElseGet(() -> roleDao.save(new Role("ROLE_ADMIN")));

            if (userDao.findByUsername("user").isEmpty()) {
                User user = new User();
                user.setUsername("user");
                user.setPassword(passwordEncoder.encode("user"));
                user.setName("User");
                user.setLastname("Userov");
                user.setAge(30);
                user.setRoles(Set.of(roleUser));

                userDao.save(user);
            }

            if (userDao.findByUsername("admin").isEmpty()) {
                User admin = new User();
                admin.setUsername("admin");
                admin.setPassword(passwordEncoder.encode("admin"));
                admin.setName("Admin");
                admin.setLastname("Adminov");
                admin.setAge(35);
                admin.setRoles(Set.of(roleAdmin, roleUser));

                userDao.save(admin);
            }
        };
    }
}
