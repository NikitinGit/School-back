package org.example.schoolback.util.assembler.impl;

import org.example.schoolback.entity.User;
import org.example.schoolback.dto.UserDTO;
import org.example.schoolback.dto.response.UserResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;

@Component
public class UserResourceAssembler {

    @Autowired
    private PasswordEncoder passwordEncoder;

    public UserResponse toModel(User user) {
        UserResponse resource = new UserResponse();
        resource.setId(user.getId());
        resource.setLogin(user.getLogin());
        resource.setRole(user.getRole());
        resource.setFirstName(user.getFirstName());
        resource.setSecondName(user.getSecondName());
        resource.setPatronymicName(user.getPatronymicName());
        resource.setFullName(user.getFullName());
        resource.setEmail(user.getEmail());
        resource.setBirthDate(user.getBirthDate());
        resource.setCoins(user.getCoins());
        resource.setExperience(user.getExperience());
        resource.setRankLevel(user.getRankLevel());
        return resource;
    }

    public User createEntity(UserDTO resource) {
        User user = new User();
        updateEntity(user, resource);
        return user;
    }

    public void updateEntity(User existEntity, UserDTO resource) {
        existEntity.setLogin(resource.getLogin());
        if (resource.getPassword() != null) {
            existEntity.setPassword(passwordEncoder.encode(resource.getPassword()));
        }
        existEntity.setRole(resource.getRole());
        existEntity.setEmail(resource.getEmail());
        existEntity.setFirstName(resource.getFirstName());
        existEntity.setSecondName(resource.getSecondName());
        existEntity.setPatronymicName(resource.getPatronymicName());
        existEntity.setBirthDate(resource.getBirthDate());
    }

    public List<UserResponse> toModels(Collection<User> users) {
        return users.stream().map(this::toModel).toList();
    }
}
