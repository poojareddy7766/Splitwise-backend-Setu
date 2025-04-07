package com.splitwise.controller;

import com.splitwise.dto.UserDTO;
import com.splitwise.model.User;
import com.splitwise.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.List;

@RestController
@RequestMapping("/users")
@Tag(name = "Users", description = "APIs for managing users")
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping
    public List<UserDTO> getUsers() {
        return userService.getAllUsers();
    }


    @PostMapping("/add")
    @Tag(name = "Users", description = "APIs for managing users")
    @Operation(summary = "Create a new user", description = "Adds a new user to the system")
    public ResponseEntity<UserDTO> createUser(@RequestBody User user) {
        System.out.println("Received user: " + user);  // Debugging log
        if (user == null) {
            System.out.println("User object is null. Request body might be missing or malformed.");
        }
        UserDTO createdUser = userService.createUser(user);
        return ResponseEntity.ok(createdUser);
    }


}
