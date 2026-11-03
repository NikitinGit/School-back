package org.example.schoolback.util.assembler.impl;

import org.example.schoolback.dto.GroupDTO;
import org.example.schoolback.entity.Group;
import org.example.schoolback.entity.User;
import org.example.schoolback.service.UserService;
import org.example.schoolback.util.assembler.ConverterModelAssembler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class GroupAssembler  extends ConverterModelAssembler<Group, GroupDTO> {
    @Autowired
    private UserResourceAssembler userResourceAssembler;
    @Autowired
    private UserService userService;


    @Override
    public GroupDTO toModel(Group group) {
        GroupDTO resource = new GroupDTO();
        resource.setId(group.getId());
        resource.setGroupName(group.getName());
        resource.setTeacherId(group.getTeacherId());
        resource.setTeacher(userResourceAssembler.toModel(group.getTeacher()));
        resource.setStudents(userResourceAssembler.toModels(group.getParticipants()));
        return resource;
    }

    @Override
    public Group createEntity(GroupDTO groupDTO) {
        final Optional<User> teacher = userService.getUserById(groupDTO.getTeacherId());

        Group group = new Group();
        group.setName(groupDTO.getGroupName());
        teacher.ifPresent(group::setTeacher);
        return group;
    }

    @Override
    public void updateEntity(Group existingGroup, GroupDTO groupDTO) {
        final Optional<User> teacher = userService.getUserById(groupDTO.getTeacherId());

        existingGroup.setName(groupDTO.getGroupName());
        if (teacher.isPresent()) {
            existingGroup.setTeacher(teacher.get());
        }
    }
}
