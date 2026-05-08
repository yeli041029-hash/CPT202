package com.group32.cpt202.zyl_project.zyl_login;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    private UserService service;

    @BeforeEach
    void setUp() {
        service = new UserService(userRepository);
    }

    @Test
    void registerSavesTrimmedUserAndDefaultsToUserRole() {
        User request = requestUser("  alice  ", "  secret  ", null, "  alice@example.com  ", " 123 ", " bio ", " avatar ");

        when(userRepository.existsByUsername("alice")).thenReturn(false);
        when(userRepository.existsByEmail("alice@example.com")).thenReturn(false);
        when(userRepository.save(org.mockito.ArgumentMatchers.any(com.group32.cpt202.LY_contributor.entity.User.class)))
                .thenAnswer(invocation -> {
                    com.group32.cpt202.LY_contributor.entity.User entity = invocation.getArgument(0);
                    entity.setId(1L);
                    entity.setCreatedAt(LocalDateTime.of(2026, 5, 8, 16, 0));
                    return entity;
                });

        User result = service.register(request);

        ArgumentCaptor<com.group32.cpt202.LY_contributor.entity.User> captor =
                ArgumentCaptor.forClass(com.group32.cpt202.LY_contributor.entity.User.class);
        verify(userRepository).save(captor.capture());

        com.group32.cpt202.LY_contributor.entity.User saved = captor.getValue();
        assertThat(saved.getUsername()).isEqualTo("alice");
        assertThat(saved.getPassword()).isEqualTo("secret");
        assertThat(saved.getRole()).isEqualTo(com.group32.cpt202.LY_contributor.entity.User.Role.USER);
        assertThat(saved.getEmail()).isEqualTo("alice@example.com");
        assertThat(saved.getPhone()).isEqualTo("123");
        assertThat(saved.getBio()).isEqualTo("bio");
        assertThat(saved.getAvatarUrl()).isEqualTo("avatar");

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getUsername()).isEqualTo("alice");
        assertThat(result.getRole()).isEqualTo(UserRole.USER);
        assertThat(result.getCreatedAt()).isEqualTo(LocalDateTime.of(2026, 5, 8, 16, 0));
    }

    @Test
    void registerAllowsAdminRoleWhenRequested() {
        User request = requestUser("admin", "secret", UserRole.ADMIN, null, null, null, null);

        when(userRepository.existsByUsername("admin")).thenReturn(false);
        when(userRepository.save(org.mockito.ArgumentMatchers.any(com.group32.cpt202.LY_contributor.entity.User.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        service.register(request);

        ArgumentCaptor<com.group32.cpt202.LY_contributor.entity.User> captor =
                ArgumentCaptor.forClass(com.group32.cpt202.LY_contributor.entity.User.class);
        verify(userRepository).save(captor.capture());
        assertThat(captor.getValue().getRole()).isEqualTo(com.group32.cpt202.LY_contributor.entity.User.Role.ADMIN);
    }

    @Test
    void registerThrowsWhenRequestIsNull() {
        assertThatThrownBy(() -> service.register(null))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("request is required");
    }

    @Test
    void registerThrowsWhenUsernameIsMissing() {
        User request = requestUser("   ", "secret", null, null, null, null, null);

        assertThatThrownBy(() -> service.register(request))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("username is required");
    }

    @Test
    void registerThrowsWhenPasswordIsMissing() {
        User request = requestUser("alice", "   ", null, null, null, null, null);

        assertThatThrownBy(() -> service.register(request))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("password is required");
    }

    @Test
    void registerThrowsWhenUsernameAlreadyExists() {
        User request = requestUser("alice", "secret", null, null, null, null, null);
        when(userRepository.existsByUsername("alice")).thenReturn(true);

        assertThatThrownBy(() -> service.register(request))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("username already exists");
    }

    @Test
    void registerThrowsWhenEmailAlreadyExists() {
        User request = requestUser("alice", "secret", null, "alice@example.com", null, null, null);
        when(userRepository.existsByUsername("alice")).thenReturn(false);
        when(userRepository.existsByEmail("alice@example.com")).thenReturn(true);

        assertThatThrownBy(() -> service.register(request))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("email already exists");
    }

    @Test
    void loginFindsUserByUsername() {
        com.group32.cpt202.LY_contributor.entity.User entity = sharedUser(1L, "alice", "secret",
                com.group32.cpt202.LY_contributor.entity.User.Role.USER, "alice@example.com");
        when(userRepository.findByUsername("alice")).thenReturn(Optional.of(entity));

        User result = service.login(" alice ", "secret");

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getUsername()).isEqualTo("alice");
        assertThat(result.getRole()).isEqualTo(UserRole.USER);
    }

    @Test
    void loginFallsBackToEmailLookup() {
        com.group32.cpt202.LY_contributor.entity.User entity = sharedUser(1L, "alice", "secret",
                com.group32.cpt202.LY_contributor.entity.User.Role.CONTRIBUTOR, "alice@example.com");
        when(userRepository.findByUsername("alice@example.com")).thenReturn(Optional.empty());
        when(userRepository.findByEmail("alice@example.com")).thenReturn(Optional.of(entity));

        User result = service.login("alice@example.com", "secret");

        assertThat(result.getRole()).isEqualTo(UserRole.CONTRIBUTOR);
    }

    @Test
    void loginThrowsWhenIdentifierIsBlank() {
        assertThatThrownBy(() -> service.login("   ", "secret"))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("username or email is required");
    }

    @Test
    void loginThrowsWhenPasswordIsBlank() {
        assertThatThrownBy(() -> service.login("alice", "   "))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("password is required");
    }

    @Test
    void loginThrowsWhenUserDoesNotExist() {
        when(userRepository.findByUsername("alice")).thenReturn(Optional.empty());
        when(userRepository.findByEmail("alice")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.login("alice", "secret"))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("user not found");
    }

    @Test
    void loginThrowsWhenPasswordIsInvalid() {
        com.group32.cpt202.LY_contributor.entity.User entity = sharedUser(1L, "alice", "secret",
                com.group32.cpt202.LY_contributor.entity.User.Role.USER, "alice@example.com");
        when(userRepository.findByUsername("alice")).thenReturn(Optional.of(entity));

        assertThatThrownBy(() -> service.login("alice", "wrong"))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("invalid password");
    }

    @Test
    void getProfileReturnsMappedUser() {
        com.group32.cpt202.LY_contributor.entity.User entity = sharedUser(1L, "alice", "secret",
                com.group32.cpt202.LY_contributor.entity.User.Role.ADMIN, "alice@example.com");
        entity.setCreatedAt(LocalDateTime.of(2026, 5, 8, 16, 0));
        when(userRepository.findById(1L)).thenReturn(Optional.of(entity));

        User result = service.getProfile(1L);

        assertThat(result.getUsername()).isEqualTo("alice");
        assertThat(result.getRole()).isEqualTo(UserRole.ADMIN);
        assertThat(result.getCreatedAt()).isEqualTo(LocalDateTime.of(2026, 5, 8, 16, 0));
    }

    @Test
    void listUsersWithoutRoleReturnsAllUsers() {
        when(userRepository.findAllByOrderByIdAsc()).thenReturn(List.of(
                sharedUser(1L, "alice", "secret", com.group32.cpt202.LY_contributor.entity.User.Role.USER, "alice@example.com"),
                sharedUser(2L, "bob", "secret", com.group32.cpt202.LY_contributor.entity.User.Role.CONTRIBUTOR, "bob@example.com")
        ));

        List<User> result = service.listUsers(null);

        assertThat(result).extracting(User::getUsername).containsExactly("alice", "bob");
        assertThat(result).extracting(User::getRole).containsExactly(UserRole.USER, UserRole.CONTRIBUTOR);
    }

    @Test
    void listUsersWithRoleUsesMappedSharedRole() {
        when(userRepository.findByRoleOrderByIdAsc(com.group32.cpt202.LY_contributor.entity.User.Role.CONTRIBUTOR))
                .thenReturn(List.of(
                        sharedUser(2L, "bob", "secret", com.group32.cpt202.LY_contributor.entity.User.Role.CONTRIBUTOR, "bob@example.com")
                ));

        List<User> result = service.listUsers(UserRole.CONTRIBUTOR);

        assertThat(result).singleElement().extracting(User::getRole).isEqualTo(UserRole.CONTRIBUTOR);
    }

    @Test
    void updateProfileUpdatesFieldsWhenValuesAreValid() {
        com.group32.cpt202.LY_contributor.entity.User existing = sharedUser(1L, "alice", "old",
                com.group32.cpt202.LY_contributor.entity.User.Role.USER, "old@example.com");
        when(userRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(userRepository.findByUsername("new-alice")).thenReturn(Optional.empty());
        when(userRepository.findByEmail("new@example.com")).thenReturn(Optional.empty());
        when(userRepository.save(existing)).thenReturn(existing);

        User request = requestUser(" new-alice ", "new-secret", null, " new@example.com ", " 123 ", " bio ", " avatar ");

        User result = service.updateProfile(1L, request);

        assertThat(existing.getUsername()).isEqualTo("new-alice");
        assertThat(existing.getPassword()).isEqualTo("new-secret");
        assertThat(existing.getEmail()).isEqualTo("new@example.com");
        assertThat(existing.getPhone()).isEqualTo("123");
        assertThat(existing.getBio()).isEqualTo("bio");
        assertThat(existing.getAvatarUrl()).isEqualTo("avatar");
        assertThat(result.getUsername()).isEqualTo("new-alice");
    }

    @Test
    void updateProfileThrowsWhenUserDoesNotExist() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.updateProfile(1L, new User()))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("user not found");
    }

    @Test
    void updateProfileThrowsWhenUsernameIsDuplicated() {
        com.group32.cpt202.LY_contributor.entity.User existing = sharedUser(1L, "alice", "old",
                com.group32.cpt202.LY_contributor.entity.User.Role.USER, "old@example.com");
        com.group32.cpt202.LY_contributor.entity.User duplicate = sharedUser(2L, "new-alice", "old",
                com.group32.cpt202.LY_contributor.entity.User.Role.USER, "other@example.com");

        when(userRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(userRepository.findByUsername("new-alice")).thenReturn(Optional.of(duplicate));

        User request = requestUser("new-alice", null, null, null, null, null, null);

        assertThatThrownBy(() -> service.updateProfile(1L, request))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("username already exists");
    }

    @Test
    void updateProfileThrowsWhenEmailIsDuplicated() {
        com.group32.cpt202.LY_contributor.entity.User existing = sharedUser(1L, "alice", "old",
                com.group32.cpt202.LY_contributor.entity.User.Role.USER, "old@example.com");
        com.group32.cpt202.LY_contributor.entity.User duplicate = sharedUser(2L, "other", "old",
                com.group32.cpt202.LY_contributor.entity.User.Role.USER, "new@example.com");

        when(userRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(userRepository.findByEmail("new@example.com")).thenReturn(Optional.of(duplicate));

        User request = requestUser(null, null, null, "new@example.com", null, null, null);

        assertThatThrownBy(() -> service.updateProfile(1L, request))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("email already exists");
    }

    private User requestUser(String username,
                             String password,
                             UserRole role,
                             String email,
                             String phone,
                             String bio,
                             String avatarUrl) {
        User user = new User();
        user.setUsername(username);
        user.setPassword(password);
        user.setRole(role);
        user.setEmail(email);
        user.setPhone(phone);
        user.setBio(bio);
        user.setAvatarUrl(avatarUrl);
        return user;
    }

    private com.group32.cpt202.LY_contributor.entity.User sharedUser(Long id,
                                                                     String username,
                                                                     String password,
                                                                     com.group32.cpt202.LY_contributor.entity.User.Role role,
                                                                     String email) {
        com.group32.cpt202.LY_contributor.entity.User user = new com.group32.cpt202.LY_contributor.entity.User();
        user.setId(id);
        user.setUsername(username);
        user.setPassword(password);
        user.setRole(role);
        user.setEmail(email);
        return user;
    }
}
