package com.backend.situ.service;

import com.backend.situ.entity.Company;
import com.backend.situ.entity.User;
import com.backend.situ.entity.UserCredentials;
import com.backend.situ.enums.UserRole;
import com.backend.situ.exception.BadRequestException;
import com.backend.situ.exception.NotFoundException;
import com.backend.situ.model.UserCreateDTO;
import com.backend.situ.model.UserResponseDTO;
import com.backend.situ.model.UserUpdateDTO;
import com.backend.situ.repository.AuthRepository;
import com.backend.situ.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private AuthRepository authRepository;

    private UserService userService;

    @BeforeEach
    void setUp() {
        userService = new UserService(userRepository, authRepository);
    }

    @Test
    void shouldListUsersForCurrentCompany() {
        String subject = "admin@situ.com";
        Company company = buildCompany(7L);
        User currentUser = buildUser(1L, company, 12345678, "Admin", "User", UserRole.ADMIN);
        UserCredentials credentials = new UserCredentials(currentUser, subject, "hash");

        User employee = buildUser(2L, company, 11222333, "Ana", "Diaz", UserRole.EMPLOYEE);
        Pageable pageable = PageRequest.of(0, 20);

        when(authRepository.findByEmail(subject)).thenReturn(Optional.of(credentials));
        when(userRepository.findByCompanyIdOrderByLastNameAscFirstNameAsc(7L, pageable))
                .thenReturn(new PageImpl<>(List.of(employee), pageable, 1));

        Page<UserResponseDTO> page = userService.listUsers(0, 20, subject);

        assertEquals(1, page.getTotalElements());
        assertEquals(2L, page.getContent().getFirst().id());
        assertEquals("Ana", page.getContent().getFirst().firstName());
        verify(userRepository).findByCompanyIdOrderByLastNameAscFirstNameAsc(7L, pageable);
    }

    @Test
    void shouldCreateUserWithDefaultRoleEmployee() {
        String subject = "admin@situ.com";
        Company company = buildCompany(9L);
        User admin = buildUser(1L, company, 10101010, "Admin", "Owner", UserRole.ADMIN);
        UserCredentials credentials = new UserCredentials(admin, subject, "hash");

        when(authRepository.findByEmail(subject)).thenReturn(Optional.of(credentials));
        when(userRepository.existsByDniAndCompanyId(44556677, 9L)).thenReturn(false);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(55L);
            return user;
        });

        UserCreateDTO request = new UserCreateDTO(44556677, "Lucia", "Rivera", null);
        UserResponseDTO created = userService.createUser(request, subject);

        assertEquals(55L, created.id());
        assertEquals(UserRole.EMPLOYEE, created.role());
        assertEquals(9L, created.company().id());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void shouldRejectCreateUserWhenDniAlreadyExistsInCompany() {
        String subject = "admin@situ.com";
        Company company = buildCompany(11L);
        User admin = buildUser(1L, company, 99999999, "Admin", "Owner", UserRole.ADMIN);
        UserCredentials credentials = new UserCredentials(admin, subject, "hash");

        when(authRepository.findByEmail(subject)).thenReturn(Optional.of(credentials));
        when(userRepository.existsByDniAndCompanyId(12312312, 11L)).thenReturn(true);

        UserCreateDTO request = new UserCreateDTO(12312312, "Mario", "Sosa", UserRole.SUPERVISOR);
        BadRequestException ex = assertThrows(BadRequestException.class, () -> userService.createUser(request, subject));

        assertEquals("ERRORS.USER.DNI_EXISTS", ex.getMessage());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void shouldUpdateUserWithinCurrentCompany() {
        String subject = "supervisor@situ.com";
        Company company = buildCompany(13L);
        User supervisor = buildUser(1L, company, 55554444, "Sup", "User", UserRole.SUPERVISOR);
        UserCredentials credentials = new UserCredentials(supervisor, subject, "hash");

        User stored = buildUser(3L, company, 10000000, "Old", "Name", UserRole.EMPLOYEE);

        when(authRepository.findByEmail(subject)).thenReturn(Optional.of(credentials));
        when(userRepository.findByIdAndCompanyId(3L, 13L)).thenReturn(Optional.of(stored));
        when(userRepository.existsByDniAndCompanyId(20000000, 13L)).thenReturn(false);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UserUpdateDTO request = new UserUpdateDTO(20000000, "New", "Lastname", UserRole.DRIVER);
        UserResponseDTO updated = userService.updateUser(3L, request, subject);

        assertEquals(20000000, updated.dni());
        assertEquals("New", updated.firstName());
        assertEquals("Lastname", updated.lastName());
        assertEquals(UserRole.DRIVER, updated.role());
    }

    @Test
    void shouldThrowNotFoundWhenUserIsOutsideCompanyScope() {
        String subject = "admin@situ.com";
        Company company = buildCompany(21L);
        User admin = buildUser(1L, company, 11111111, "Admin", "Root", UserRole.ADMIN);
        UserCredentials credentials = new UserCredentials(admin, subject, "hash");

        when(authRepository.findByEmail(subject)).thenReturn(Optional.of(credentials));
        when(userRepository.findByIdAndCompanyId(999L, 21L)).thenReturn(Optional.empty());

        NotFoundException ex = assertThrows(NotFoundException.class, () -> userService.getUser(999L, subject));
        assertEquals("ERRORS.USER.NOT_FOUND", ex.getMessage());
    }

    private Company buildCompany(Long id) {
        Company company = new Company();
        company.setId(id);
        company.setName("Company " + id);
        company.setLogo_filename("logo.png");
        return company;
    }

    private User buildUser(Long id, Company company, Integer dni, String firstName, String lastName, UserRole role) {
        User user = new User();
        user.setId(id);
        user.setCompany(company);
        user.setDni(dni);
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setRole(role);
        return user;
    }
}
