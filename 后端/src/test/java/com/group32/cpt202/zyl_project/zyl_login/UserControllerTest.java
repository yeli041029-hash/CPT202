package com.group32.cpt202.zyl_project.zyl_login;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    @Mock
    private UserService userService;

    private UserController controller;

    @BeforeEach
    void setUp() {
        controller = new UserController(userService);
    }

    @Test
    void registerReturnsOkOnSuccess() {
        User request = new User();
        User saved = new User();
        when(userService.register(request)).thenReturn(saved);

        ResponseEntity<?> response = controller.register(request);

        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).isSameAs(saved);
    }

    @Test
    void registerReturnsBadRequestOnFailure() {
        User request = new User();
        when(userService.register(request)).thenThrow(new RuntimeException("bad request"));

        ResponseEntity<?> response = controller.register(request);

        assertThat(response.getStatusCode().value()).isEqualTo(400);
        assertThat(response.getBody()).isEqualTo("bad request");
    }

    @Test
    void loginReturnsUnauthorizedOnFailure() {
        User request = new User();
        request.setUsername("alice");
        request.setPassword("secret");
        when(userService.login("alice", "secret")).thenThrow(new RuntimeException("invalid password"));

        ResponseEntity<?> response = controller.login(request);

        assertThat(response.getStatusCode().value()).isEqualTo(401);
        assertThat(response.getBody()).isEqualTo("invalid password");
    }

    @Test
    void getProfileReturnsBadRequestOnFailure() {
        when(userService.getProfile(1L)).thenThrow(new RuntimeException("user not found"));

        ResponseEntity<?> response = controller.getProfile(1L);

        assertThat(response.getStatusCode().value()).isEqualTo(400);
        assertThat(response.getBody()).isEqualTo("user not found");
    }

    @Test
    void updateProfileReturnsBadRequestOnFailure() {
        User request = new User();
        when(userService.updateProfile(1L, request)).thenThrow(new RuntimeException("username already exists"));

        ResponseEntity<?> response = controller.updateProfile(1L, request);

        assertThat(response.getStatusCode().value()).isEqualTo(400);
        assertThat(response.getBody()).isEqualTo("username already exists");
    }

    @Test
    void listUsersReturnsServiceResult() {
        List<User> users = List.of(new User());
        when(userService.listUsers(UserRole.USER)).thenReturn(users);

        ResponseEntity<List<User>> response = controller.listUsers(UserRole.USER);

        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).isSameAs(users);
    }
}
