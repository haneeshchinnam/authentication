package com.example.auth.service;

import com.example.auth.exception.GroupNotFoundException;
import com.example.auth.exception.UserNotFoundException;
import com.example.auth.interfaces.GroupInterface;
import com.example.auth.model.Group;
import com.example.auth.model.User;
import com.example.auth.repository.GroupRepository;
import com.example.auth.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class GroupService implements GroupInterface {

    @Autowired
    private GroupRepository groupRepository;

    @Autowired
    private UserRepository userRepository;

    @Override
    public ResponseEntity<?> createGroup(String groupName, List<Long> userIds) {
        Group group = new Group();
        group.setName(groupName);
        for (Long userId : userIds) {
            Optional<User> user = userRepository.findById(userId);
            user.ifPresent(group.getUsers()::add);
        }
        groupRepository.save(group);
        return ResponseEntity.ok("Group created successfully");
    }

    @Override
    public ResponseEntity<?> addUserToGroup(String groupId, String userId) {
        Optional<Group> groupOpt = groupRepository.findById(UUID.fromString(groupId));
        Optional<User> userOpt = userRepository.findById(Long.getLong(userId));
        if (groupOpt.isPresent() && userOpt.isPresent()) {
            Group group = groupOpt.get();
            User user = userOpt.get();
            group.getUsers().add(user);
            groupRepository.save(group);
            return ResponseEntity.ok("User added to group successfully");
        }
        if (groupOpt.isEmpty()) {
            throw new GroupNotFoundException("Group not found");
        }
        throw new UserNotFoundException("User not found");
    }

    @Override
    public ResponseEntity<?> removeUserFromGroup(String groupId, String userId) {
        Optional<Group> groupOpt = groupRepository.findById(UUID.fromString(groupId));
        Optional<User> userOpt = userRepository.findById(Long.getLong(userId));
        if (groupOpt.isPresent() && userOpt.isPresent()) {
            Group group = groupOpt.get();
            User user = userOpt.get();
            group.getUsers().remove(user);
            groupRepository.save(group);
            return ResponseEntity.ok("User added to group successfully");
        }
        if (groupOpt.isEmpty()) {
            throw new GroupNotFoundException("Group not found");
        }
        throw new UserNotFoundException("User not found");
    }

    @Override
    public ResponseEntity<?> deleteGroup(String groupId) {
        groupRepository.deleteById(UUID.fromString(groupId));
        return ResponseEntity.ok("Group deleted successfully");
    }
}
