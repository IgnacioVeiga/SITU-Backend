package com.backend.situ.service;

import com.backend.situ.entity.Alert;
import com.backend.situ.entity.User;
import com.backend.situ.entity.UserCredentials;
import com.backend.situ.enums.AlertPriority;
import com.backend.situ.exception.BadRequestException;
import com.backend.situ.model.AlertCreateDTO;
import com.backend.situ.model.AlertResponseDTO;
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
        Pageable pageable = PageRequest.of(0, 20);
        Alert alert = buildAlert(1L, "Service delay");
        when(alertRepository.findActiveAt(any(Timestamp.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(alert), pageable, 1));

        Page<AlertResponseDTO> result = alertService.listAlerts(0, 20, true);

        assertEquals(1, result.getTotalElements());
        assertEquals("Service delay", result.getContent().getFirst().title());
        verify(alertRepository).findActiveAt(any(Timestamp.class), any(Pageable.class));
        verify(alertRepository, never()).findAllByOrderByAlertDateDesc(any(Pageable.class));
    }

    @Test
    void shouldReturnAllAlertsWhenActiveOnlyFalse() {
        Pageable pageable = PageRequest.of(0, 20);
        Alert alert = buildAlert(2L, "Road closure");
        when(alertRepository.findAllByOrderByAlertDateDesc(any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(alert), pageable, 1));

        Page<AlertResponseDTO> result = alertService.listAlerts(0, 20, false);

        assertEquals(1, result.getTotalElements());
        assertEquals("Road closure", result.getContent().getFirst().title());
        verify(alertRepository).findAllByOrderByAlertDateDesc(any(Pageable.class));
        verify(alertRepository, never()).findActiveAt(any(Timestamp.class), any(Pageable.class));
    }

    @Test
    void shouldRejectAlertWhenEndsBeforeStarts() {
        String subject = "admin@situ.com";
        User creator = new User();
        creator.setId(50L);
        creator.setFirstName("Admin");
        creator.setLastName("User");

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

    private Alert buildAlert(Long id, String title) {
        Alert alert = new Alert();
        alert.setId(id);
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
        alert.setUser(user);
        return alert;
    }
}
