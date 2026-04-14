package ru.kata.spring.boot_security.demo.service;

import ru.kata.spring.boot_security.demo.model.User;

import java.util.List;

public interface UserService {
    List<User> getAllUsers();

    User getUserById(Long id);

    void saveUser(User user);

    void updateUser(User user);

    void deleteUser(Long id);

    void createUser(String username, String password, String name, String lastname, Integer age, List<String> roles);

    void updateUser(Long id, String username, String password, String name, String lastname, Integer age, List<String> roles);

}
