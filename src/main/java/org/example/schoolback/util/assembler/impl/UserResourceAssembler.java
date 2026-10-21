package org.example.schoolback.util.assembler.impl;

import org.example.schoolback.entity.User;
import org.example.schoolback.dto.UserDTO;
import org.example.schoolback.util.assembler.ConverterModelAssembler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class UserResourceAssembler extends ConverterModelAssembler<User, UserDTO> {

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public UserDTO toModel(User user) {
        UserDTO resource = new UserDTO();
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

    @Override
    public User createEntity(UserDTO resource) {
        User user = new User();
        updateEntity(user, resource);
        return user;
    }

    @Override
    public void updateEntity(User existEntity, UserDTO resource) {
        existEntity.setLogin(resource.getLogin());
        existEntity.setPassword(passwordEncoder.encode(resource.getPassword()));
        existEntity.setRole(resource.getRole());
        existEntity.setEmail(resource.getEmail());
        existEntity.setFirstName(resource.getFirstName());
        existEntity.setSecondName(resource.getSecondName());
        existEntity.setPatronymicName(resource.getPatronymicName());
        existEntity.setBirthDate(resource.getBirthDate());
        existEntity.setCoins(resource.getCoins());
    }
}