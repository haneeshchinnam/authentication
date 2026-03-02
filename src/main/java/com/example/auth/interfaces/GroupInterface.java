package com.example.auth.interfaces;

import org.springframework.http.ResponseEntity;

import java.util.List;

public interface GroupInterface {
    ResponseEntity<?> createGroup(String groupName, List<Long> userIds);
    ResponseEntity<?> addUserToGroup(String groupId, String userId);
    ResponseEntity<?> removeUserFromGroup(String groupId, String userId);
    ResponseEntity<?> deleteGroup(String groupId);
}
