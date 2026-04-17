package ru.kata.spring.boot_security.demo.dto;

import java.util.List;

public class UserDto {
    private Long id;
    private String name;
    private String lastname;
    private Integer age;
    private String username;
    private List<RoleDto> roles;

    public UserDto() {
    }

    public UserDto(Long id, String name, String lastname, Integer age, String username, List<RoleDto> roles) {
        this.id = id;
        this.name = name;
        this.lastname = lastname;
        this.age = age;
        this.username = username;
        this.roles = roles;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLastname() {
        return lastname;
    }

    public void setLastname(String lastname) {
        this.lastname = lastname;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public List<RoleDto> getRoles() {
        return roles;
    }

    public void setRoles(List<RoleDto> roles) {
        this.roles = roles;
    }
}
