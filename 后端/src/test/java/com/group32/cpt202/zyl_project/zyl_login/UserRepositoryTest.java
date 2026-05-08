package com.group32.cpt202.zyl_project.zyl_login;

import com.group32.cpt202.LY_contributor.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserRepositoryTest {

    @Mock
    private com.group32.cpt202.LY_contributor.repository.UserRepository delegate;

    private UserRepository repository;

    @BeforeEach
    void setUp() {
        repository = new UserRepository(delegate);
    }

    @Test
    void delegatesLookupMethods() {
        User user = user(1L, "alice");
        when(delegate.findById(1L)).thenReturn(Optional.of(user));
        when(delegate.findByUsername("alice")).thenReturn(Optional.of(user));
        when(delegate.findByEmail("alice@example.com")).thenReturn(Optional.of(user));
        when(delegate.existsByUsername("alice")).thenReturn(true);
        when(delegate.existsByEmail("alice@example.com")).thenReturn(true);

        assertThat(repository.findById(1L)).contains(user);
        assertThat(repository.findByUsername("alice")).contains(user);
        assertThat(repository.findByEmail("alice@example.com")).contains(user);
        assertThat(repository.existsByUsername("alice")).isTrue();
        assertThat(repository.existsByEmail("alice@example.com")).isTrue();
    }

    @Test
    void delegatesListMethods() {
        User user = user(1L, "alice");
        when(delegate.findAllByOrderByIdAsc()).thenReturn(List.of(user));
        when(delegate.findByRoleOrderByIdAsc(User.Role.USER)).thenReturn(List.of(user));

        assertThat(repository.findAllByOrderByIdAsc()).containsExactly(user);
        assertThat(repository.findByRoleOrderByIdAsc(User.Role.USER)).containsExactly(user);
    }

    @Test
    void saveDelegatesToSharedRepository() {
        User user = user(1L, "alice");
        when(delegate.save(user)).thenReturn(user);

        assertThat(repository.save(user)).isSameAs(user);
        verify(delegate).save(user);
    }

    private User user(Long id, String username) {
        User user = new User();
        user.setId(id);
        user.setUsername(username);
        user.setRole(User.Role.USER);
        return user;
    }
}
