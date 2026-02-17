package com.backend.situ.service;

import com.backend.situ.entity.Complaint;
import com.backend.situ.entity.User;
import com.backend.situ.entity.UserCredentials;
import com.backend.situ.enums.ComplaintState;
import com.backend.situ.exception.BadRequestException;
import com.backend.situ.model.ComplaintCreateDTO;
import com.backend.situ.model.ComplaintResponseDTO;
import com.backend.situ.model.ComplaintStateUpdateDTO;
import com.backend.situ.repository.AuthRepository;
import com.backend.situ.repository.ComplaintRepository;
import com.backend.situ.repository.LineRepository;
import com.backend.situ.repository.RouteRepository;
import com.backend.situ.repository.StopRepository;
import com.backend.situ.repository.UserRepository;
import com.backend.situ.repository.image.ReportImageRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.sql.Timestamp;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ComplaintServiceTest {

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @Mock
    private ComplaintRepository complaintRepository;

    @Mock
    private AuthRepository authRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private LineRepository lineRepository;

    @Mock
    private RouteRepository routeRepository;

    @Mock
    private StopRepository stopRepository;

    @Mock
    private ReportImageRepository reportImageRepository;

    @Mock
    private SensitiveDataService sensitiveDataService;

    @Mock
    private ComplaintNotificationService complaintNotificationService;

    private ComplaintService complaintService;

    @BeforeEach
    void setUp() {
        complaintService = new ComplaintService(
                eventPublisher,
                complaintRepository,
                authRepository,
                userRepository,
                lineRepository,
                routeRepository,
                stopRepository,
                reportImageRepository,
                sensitiveDataService,
                complaintNotificationService
        );
    }

    @Test
    void shouldCreateComplaintWithDefaultsAndMaskedContact() {
        String subject = "passenger@example.com";
        User reporter = createUser(10L, "Pablo", "Passenger");
        UserCredentials credentials = new UserCredentials(reporter, subject, "hash");

        when(authRepository.findByEmail(subject)).thenReturn(Optional.of(credentials));
        when(sensitiveDataService.encrypt("john@example.com")).thenReturn("enc-email");
        when(sensitiveDataService.encrypt("1122334455")).thenReturn("enc-phone");
        when(sensitiveDataService.decrypt("enc-email")).thenReturn("john@example.com");
        when(sensitiveDataService.decrypt("enc-phone")).thenReturn("1122334455");
        when(complaintRepository.save(any(Complaint.class))).thenAnswer(invocation -> {
            Complaint saved = invocation.getArgument(0);
            saved.setId(1L);
            return saved;
        });

        ComplaintCreateDTO request = new ComplaintCreateDTO(
                "The bus did not stop at the station",
                "Service quality",
                null,
                true,
                "john@example.com",
                "1122334455",
                null,
                null,
                null,
                null
        );

        ComplaintResponseDTO response = complaintService.createComplaint(request, subject);

        assertNotNull(response);
        assertEquals(ComplaintState.PENDING_REVIEW, response.state());
        assertTrue(response.anonymous());
        assertNotNull(response.responseDueAt());
        assertNotNull(response.resolutionDueAt());
        assertEquals("j***@example.com", response.maskedContactEmail());
        assertEquals("***4455", response.maskedContactPhone());
        assertNotNull(response.trackingToken());
        verify(eventPublisher, times(1)).publishEvent(any());
    }

    @Test
    void shouldRejectInvalidStateTransition() {
        Complaint complaint = new Complaint();
        complaint.setId(25L);
        complaint.setState(ComplaintState.PENDING_REVIEW);
        complaint.setCreatedAt(Timestamp.from(java.time.Instant.now()));
        complaint.setUpdatedAt(Timestamp.from(java.time.Instant.now()));
        complaint.setResponseDueAt(Timestamp.from(java.time.Instant.now()));
        complaint.setResolutionDueAt(Timestamp.from(java.time.Instant.now()));

        when(complaintRepository.findById(25L)).thenReturn(Optional.of(complaint));

        ComplaintStateUpdateDTO request = new ComplaintStateUpdateDTO(ComplaintState.REOPENED);

        assertThrows(BadRequestException.class, () -> complaintService.updateComplaintState(25L, request, "staff@example.com"));
        verify(complaintRepository, never()).save(any(Complaint.class));
    }

    @Test
    void shouldMoveComplaintToInReviewAndAssignCurrentUser() {
        String subject = "employee@example.com";
        User assignee = createUser(99L, "Eva", "Employee");
        UserCredentials credentials = new UserCredentials(assignee, subject, "hash");

        Complaint complaint = new Complaint();
        complaint.setId(33L);
        complaint.setState(ComplaintState.PENDING_REVIEW);
        complaint.setCreatedAt(Timestamp.from(java.time.Instant.now()));
        complaint.setUpdatedAt(Timestamp.from(java.time.Instant.now()));
        complaint.setResponseDueAt(Timestamp.from(java.time.Instant.now()));
        complaint.setResolutionDueAt(Timestamp.from(java.time.Instant.now()));

        when(authRepository.findByEmail(subject)).thenReturn(Optional.of(credentials));
        when(complaintRepository.findById(33L)).thenReturn(Optional.of(complaint));
        when(complaintRepository.save(any(Complaint.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ComplaintStateUpdateDTO request = new ComplaintStateUpdateDTO(ComplaintState.IN_REVIEW);
        ComplaintResponseDTO response = complaintService.updateComplaintState(33L, request, subject);

        assertEquals(ComplaintState.IN_REVIEW, response.state());
        assertNotNull(response.firstResponseAt());
        assertNotNull(response.assignee());
        assertEquals(99L, response.assignee().id());
        verify(complaintNotificationService, times(1)).notifyStatusChanged(any(Complaint.class));
    }

    @Test
    void shouldReturnOnlyAuthenticatedUserComplaints() {
        String subject = "passenger@example.com";
        User reporter = createUser(50L, "Pia", "Passenger");
        UserCredentials credentials = new UserCredentials(reporter, subject, "hash");
        Pageable pageable = PageRequest.of(0, 10);

        Complaint complaint = new Complaint();
        complaint.setId(1L);
        complaint.setReporterUser(reporter);
        complaint.setState(ComplaintState.PENDING_REVIEW);
        complaint.setCreatedAt(Timestamp.from(java.time.Instant.now()));
        complaint.setUpdatedAt(Timestamp.from(java.time.Instant.now()));
        complaint.setResponseDueAt(Timestamp.from(java.time.Instant.now()));
        complaint.setResolutionDueAt(Timestamp.from(java.time.Instant.now()));

        when(authRepository.findByEmail(subject)).thenReturn(Optional.of(credentials));
        when(complaintRepository.findByReporterUserIdOrderByCreatedAtDesc(50L, pageable))
                .thenReturn(new PageImpl<>(java.util.List.of(complaint), pageable, 1));

        var page = complaintService.listMyComplaints(subject, 0, 10);

        assertEquals(1, page.getTotalElements());
        assertEquals(1L, page.getContent().getFirst().id());
        verify(complaintRepository, times(1)).findByReporterUserIdOrderByCreatedAtDesc(50L, pageable);
    }

    private User createUser(Long id, String firstName, String lastName) {
        User user = new User();
        user.setId(id);
        user.setFirstName(firstName);
        user.setLastName(lastName);
        return user;
    }
}
