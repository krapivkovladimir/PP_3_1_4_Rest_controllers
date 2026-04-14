package ru.kata.spring.boot_security.demo.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.kata.spring.boot_security.demo.dao.RoleDao;
import ru.kata.spring.boot_security.demo.dao.UserDao;
import ru.kata.spring.boot_security.demo.model.Role;
import ru.kata.spring.boot_security.demo.model.User;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional
public class UserServiceImpl implements UserService {

    private final UserDao userDao;
    private final PasswordEncoder passwordEncoder;
    private final RoleDao roleDao;

    public UserServiceImpl(UserDao userDao, PasswordEncoder passwordEncoder, RoleDao roleDao) {
        this.userDao = userDao;
        this.passwordEncoder = passwordEncoder;
        this.roleDao = roleDao;
    }

    @Override
    @Transactional(readOnly = true)
    public List<User> getAllUsers() {
        return userDao.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public User getUserById(Long id) {
        return userDao.findById(id).orElse(null);
    }

    @Override
    public void saveUser(User user) {
        userDao.save(user);
    }

    @Override
    public void updateUser(User user) {
        userDao.save(user);
    }

    @Override
    public void deleteUser(Long id) {
        if (userDao.existsById(id)) {
            userDao.deleteById(id);
        }
    }

    @Override
    public void createUser(String username, String password, String name, String lastname, Integer age, List<String> roles) {
        Set<Role> roleSet = roles.stream()
                .map(roleName -> roleDao.findByName(roleName)
                        .orElseThrow(() -> new RuntimeException("Role not found: " + roleName)))
                .collect(Collectors.toSet());

        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password));
        user.setName(name);
        user.setLastname(lastname);
        user.setAge(age);
        user.setRoles(roleSet);

        userDao.save(user);
    }

    @Override
    public void updateUser(Long id, String username, String password, String name, String lastname, Integer age, List<String> roles) {
        User user = userDao.findById(id).orElse(null);

        if (user != null) {
            Set<Role> roleSet = roles.stream()
                    .map(roleName -> roleDao.findByName(roleName)
                            .orElseThrow(() -> new RuntimeException("Role not found: " + roleName)))
                    .collect(Collectors.toSet());

            user.setUsername(username);
            user.setName(name);
            user.setLastname(lastname);
            user.setAge(age);
            user.setRoles(roleSet);

            if (password != null && !password.isBlank()) {
                user.setPassword(passwordEncoder.encode(password));
            }

            userDao.save(user);
        }
    }


}
