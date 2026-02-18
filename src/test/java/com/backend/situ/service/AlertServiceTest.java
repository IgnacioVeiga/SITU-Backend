package com.backend.situ.service;

import com.backend.situ.entity.Alert;
import com.backend.situ.entity.Company;
import com.backend.situ.entity.User;
import com.backend.situ.entity.UserCredentials;
import com.backend.situ.enums.AlertPriority;
import com.backend.situ.exception.BadRequestException;
import com.backend.situ.exception.NotFoundException;
import com.backend.situ.model.AlertCreateDTO;
import com.backend.situ.model.AlertResponseDTO;
import com.backend.situ.model.AlertUpdateDTO;
import com.backend.situ.repository.AlertRepository;
import com.backend.situ.repository.AuthRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AlertServiceTest {

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @Mock
    private AlertRepository alertRepository;

    @Mock
    private AuthRepository authRepository;

    private AlertService alertService;

    @BeforeEach
    void setUp() {
        alertService = new AlertService(eventPublisher, alertRepository, authRepository);
    }

    @Test
    void shouldReturnOnlyActiveAlertsWhenActiveOnlyTrue() {
        String subject = "admin@situ.com";
        Company company = buildCompany(1L);
        User user = buildUser(10L, "Admin", "User", company);
        UserCredentials credentials = new UserCredentials(user, subject, "hash");
        Pageable pageable = PageRequest.of(0, 20);
        Alert alert = buildAlert(1L, "Service delay", company);

        when(authRepository.findByEmail(subject)).thenReturn(Optional.of(credentials));
        when(alertRepository.findActiveAt(any(Long.class), any(Timestamp.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(alert), pageable, 1));

        Page<AlertResponseDTO> result = alertService.listAlerts(0, 20, true, subject);

        assertEquals(1, result.getTotalElements());
        assertEquals("Service delay", result.getContent().getFirst().title());
        verify(alertRepository).findActiveAt(any(Long.class), any(Timestamp.class), any(Pageable.class));
        verify(alertRepository, never()).findByCompanyIdOrderByAlertDateDesc(any(Long.class), any(Pageable.class));
    }

    @Test
    void shouldReturnAllAlertsWhenActiveOnlyFalse() {
        String subject = "admin@situ.com";
        Company company = buildCompany(1L);
        User user = buildUser(10L, "Admin", "User", company);
        UserCredentials credentials = new UserCredentials(user, subject, "hash");
        Pageable pageable = PageRequest.of(0, 20);
        Alert alert = buildAlert(2L, "Road closure", company);

        when(authRepository.findByEmail(subject)).thenReturn(Optional.of(credentials));
        when(alertRepository.findByCompanyIdOrderByAlertDateDesc(any(Long.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(alert), pageable, 1));

        Page<AlertResponseDTO> result = alertService.listAlerts(0, 20, false, subject);

        assertEquals(1, result.getTotalElements());
        assertEquals("Road closure", result.getContent().getFirst().title());
        verify(alertRepository).findByCompanyIdOrderByAlertDateDesc(any(Long.class), any(Pageable.class));
        verify(alertRepository, never()).findActiveAt(any(Long.class), any(Timestamp.class), any(Pageable.class));
    }

    @Test
    void shouldRejectAlertWhenEndsBeforeStarts() {
        String subject = "admin@situ.com";
        User creator = new User();
        creator.setId(50L);
        creator.setFirstName("Admin");
        creator.setLastName("User");
        creator.setCompany(buildCompany(1L));

        UserCredentials credentials = new UserCredentials(creator, subject, "hash");
        when(authRepository.findByEmail(subject)).thenReturn(Optional.of(credentials));

        Timestamp startsAt = Timestamp.from(Instant.now());
        Timestamp endsAt = Timestamp.from(startsAt.toInstant().minusSeconds(60));

        AlertCreateDTO request = new AlertCreateDTO(
                "Transport issue",
                "A short description",
                "Downtown",
                AlertPriority.HIGH,
                startsAt,
                endsAt
        );

        BadRequestException ex = assertThrows(BadRequestException.class, () -> alertService.createAlert(request, subject));
        assertEquals("ERRORS.ALERT.INVALID_WINDOW", ex.getMessage());
        verify(alertRepository, never()).save(any(Alert.class));
    }

    @Test
    void shouldMapCreatorSummaryOnCreate() {
        String subject = "employee@situ.com";
        User creator = new User();
        creator.setId(11L);
        creator.setFirstName("Eva");
        creator.setLastName("Lopez");
        creator.setCompany(buildCompany(1L));
        UserCredentials credentials = new UserCredentials(creator, subject, "hash");

        when(authRepository.findByEmail(subject)).thenReturn(Optional.of(credentials));
        when(alertRepository.save(any(Alert.class))).thenAnswer(invocation -> {
            Alert stored = invocation.getArgument(0);
            stored.setId(90L);
            return stored;
        });

        AlertCreateDTO request = new AlertCreateDTO(
                "Signal problem",
                "Traffic lights are down",
                "Av. Central",
                AlertPriority.MEDIUM,
                null,
                null
        );

        AlertResponseDTO response = alertService.createAlert(request, subject);

        assertEquals(90L, response.id());
        assertTrue(response.user() != null);
        assertEquals(11L, response.user().id());
        assertEquals("Eva", response.user().firstName());
        assertEquals("Lopez", response.user().lastName());
        verify(eventPublisher).publishEvent(any());
    }

    @Test
    void shouldRejectAlertUpdateOutsideCurrentCompany() {
        String subject = "staff@situ.com";
        Company company = buildCompany(1L);
        User staff = buildUser(15L, "Staff", "User", company);
        UserCredentials credentials = new UserCredentials(staff, subject, "hash");

        when(authRepository.findByEmail(subject)).thenReturn(Optional.of(credentials));
        when(alertRepository.findByIdAndCompanyId(999L, 1L)).thenReturn(Optional.empty());

        AlertUpdateDTO request = new AlertUpdateDTO(
                "Updated",
                "Updated description",
                "Updated location",
                AlertPriority.LOW,
                null,
                null,
                true
        );

        assertThrows(NotFoundException.class, () -> alertService.updateAlert(999L, request, subject));
        verify(alertRepository, never()).save(any(Alert.class));
    }

    private Alert buildAlert(Long id, String title, Company company) {
        Alert alert = new Alert();
        alert.setId(id);
        alert.setCompany(company);
        alert.setTitle(title);
        alert.setDescription("Description");
        alert.setAlertDate(Timestamp.from(Instant.now()));
        alert.setStartsAt(Timestamp.from(Instant.now()));
        alert.setEndsAt(null);
        alert.setActive(true);
        alert.setPriority(AlertPriority.MEDIUM);
        alert.setLocation("Location");

        User user = new User();
        user.setId(1L);
        user.setFirstName("Juan");
        user.setLastName("Perez");
        user.setCompany(company);
        alert.setUser(user);
        return alert;
    }

    private Company buildCompany(Long id) {
        Company company = new Company();
        company.setId(id);
        company.setName("Company " + id);
        return company;
    }

    private User buildUser(Long id, String firstName, String lastName, Company company) {
        User user = new User();
        user.setId(id);
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setCompany(company);
        return user;
    }
}
