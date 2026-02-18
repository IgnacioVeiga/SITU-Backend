package com.backend.situ.service;

import com.backend.situ.entity.Company;
import com.backend.situ.entity.Complaint;
import com.backend.situ.entity.Line;
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
    private TrackingTokenService trackingTokenService;

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
                trackingTokenService,
                complaintNotificationService
        );
    }

    @Test
    void shouldCreateComplaintWithDefaultsAndMaskedContact() {
        String subject = "passenger@example.com";
        Company company = createCompany(1L, "Company One");
        User reporter = createUser(10L, "Pablo", "Passenger", company);
        UserCredentials credentials = new UserCredentials(reporter, subject, "hash");

        when(authRepository.findByEmail(subject)).thenReturn(Optional.of(credentials));
        when(trackingTokenService.generateToken()).thenReturn("public-tracking-token");
        when(trackingTokenService.hashToken("public-tracking-token")).thenReturn("hashed-tracking-token");
        when(sensitiveDataService.encrypt("john@example.com")).thenReturn("enc-email");
        when(sensitiveDataService.encrypt("1122334455")).thenReturn("enc-phone");
        when(sensitiveDataService.encrypt("public-tracking-token")).thenReturn("enc-tracking-token");
        when(sensitiveDataService.decrypt("enc-email")).thenReturn("john@example.com");
        when(sensitiveDataService.decrypt("enc-phone")).thenReturn("1122334455");
        when(sensitiveDataService.decrypt("enc-tracking-token")).thenReturn("public-tracking-token");
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
        assertEquals("public-tracking-token", response.trackingToken());
        verify(eventPublisher, times(1)).publishEvent(any());
    }

    @Test
    void shouldRejectInvalidStateTransition() {
        String subject = "staff@example.com";
        Company company = createCompany(1L, "Company One");
        User staff = createUser(20L, "Staff", "User", company);
        UserCredentials credentials = new UserCredentials(staff, subject, "hash");

        Complaint complaint = new Complaint();
        complaint.setId(25L);
        complaint.setCompany(company);
        complaint.setState(ComplaintState.PENDING_REVIEW);
        complaint.setCreatedAt(Timestamp.from(java.time.Instant.now()));
        complaint.setUpdatedAt(Timestamp.from(java.time.Instant.now()));
        complaint.setResponseDueAt(Timestamp.from(java.time.Instant.now()));
        complaint.setResolutionDueAt(Timestamp.from(java.time.Instant.now()));

        when(authRepository.findByEmail(subject)).thenReturn(Optional.of(credentials));
        when(complaintRepository.findByIdAndCompanyId(25L, 1L)).thenReturn(Optional.of(complaint));

        ComplaintStateUpdateDTO request = new ComplaintStateUpdateDTO(ComplaintState.REOPENED);

        assertThrows(BadRequestException.class, () -> complaintService.updateComplaintState(25L, request, subject));
        verify(complaintRepository, never()).save(any(Complaint.class));
    }

    @Test
    void shouldMoveComplaintToInReviewAndAssignCurrentUser() {
        String subject = "employee@example.com";
        Company company = createCompany(1L, "Company One");
        User assignee = createUser(99L, "Eva", "Employee", company);
        UserCredentials credentials = new UserCredentials(assignee, subject, "hash");

        Complaint complaint = new Complaint();
        complaint.setId(33L);
        complaint.setCompany(company);
        complaint.setState(ComplaintState.PENDING_REVIEW);
        complaint.setCreatedAt(Timestamp.from(java.time.Instant.now()));
        complaint.setUpdatedAt(Timestamp.from(java.time.Instant.now()));
        complaint.setResponseDueAt(Timestamp.from(java.time.Instant.now()));
        complaint.setResolutionDueAt(Timestamp.from(java.time.Instant.now()));

        when(authRepository.findByEmail(subject)).thenReturn(Optional.of(credentials));
        when(complaintRepository.findByIdAndCompanyId(33L, 1L)).thenReturn(Optional.of(complaint));
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
        Company company = createCompany(1L, "Company One");
        User reporter = createUser(50L, "Pia", "Passenger", company);
        UserCredentials credentials = new UserCredentials(reporter, subject, "hash");
        Pageable pageable = PageRequest.of(0, 10);

        Complaint complaint = new Complaint();
        complaint.setId(1L);
        complaint.setCompany(company);
        complaint.setReporterUser(reporter);
        complaint.setState(ComplaintState.PENDING_REVIEW);
        complaint.setCreatedAt(Timestamp.from(java.time.Instant.now()));
        complaint.setUpdatedAt(Timestamp.from(java.time.Instant.now()));
        complaint.setResponseDueAt(Timestamp.from(java.time.Instant.now()));
        complaint.setResolutionDueAt(Timestamp.from(java.time.Instant.now()));

        when(authRepository.findByEmail(subject)).thenReturn(Optional.of(credentials));
        when(complaintRepository.findByCompanyIdAndReporterUserIdOrderByCreatedAtDesc(1L, 50L, pageable))
                .thenReturn(new PageImpl<>(java.util.List.of(complaint), pageable, 1));

        var page = complaintService.listMyComplaints(subject, 0, 10);

        assertEquals(1, page.getTotalElements());
        assertEquals(1L, page.getContent().getFirst().id());
        verify(complaintRepository, times(1)).findByCompanyIdAndReporterUserIdOrderByCreatedAtDesc(1L, 50L, pageable);
    }

    @Test
    void shouldRejectComplaintCreateWhenLineBelongsToAnotherCompany() {
        String subject = "passenger@example.com";
        Company reporterCompany = createCompany(1L, "Company One");
        Company foreignCompany = createCompany(2L, "Company Two");
        User reporter = createUser(10L, "Pablo", "Passenger", reporterCompany);
        UserCredentials credentials = new UserCredentials(reporter, subject, "hash");

        Line foreignLine = new Line();
        foreignLine.setId(77L);
        foreignLine.setNumber("77");
        foreignLine.setCompany(foreignCompany);

        when(authRepository.findByEmail(subject)).thenReturn(Optional.of(credentials));
        when(lineRepository.findAllById(java.util.Set.of(77L))).thenReturn(java.util.List.of(foreignLine));

        ComplaintCreateDTO request = new ComplaintCreateDTO(
                "Cross-tenant line should fail",
                "Service quality",
                null,
                false,
                "john@example.com",
                "1122334455",
                null,
                java.util.List.of(77L),
                null,
                null
        );

        BadRequestException ex = assertThrows(BadRequestException.class, () -> complaintService.createComplaint(request, subject));
        assertEquals("ERRORS.COMPLAINT.LINE_NOT_FOUND", ex.getMessage());
        verify(complaintRepository, never()).save(any(Complaint.class));
    }

    @Test
    void shouldRejectAssignWhenAssigneeIsOutsideCurrentCompany() {
        String subject = "employee@example.com";
        Company company = createCompany(1L, "Company One");
        User staff = createUser(99L, "Eva", "Employee", company);
        UserCredentials credentials = new UserCredentials(staff, subject, "hash");

        Complaint complaint = new Complaint();
        complaint.setId(33L);
        complaint.setCompany(company);
        complaint.setState(ComplaintState.PENDING_REVIEW);
        complaint.setCreatedAt(Timestamp.from(java.time.Instant.now()));
        complaint.setUpdatedAt(Timestamp.from(java.time.Instant.now()));
        complaint.setResponseDueAt(Timestamp.from(java.time.Instant.now()));
        complaint.setResolutionDueAt(Timestamp.from(java.time.Instant.now()));

        when(authRepository.findByEmail(subject)).thenReturn(Optional.of(credentials));
        when(complaintRepository.findByIdAndCompanyId(33L, 1L)).thenReturn(Optional.of(complaint));
        when(userRepository.findByIdAndCompanyId(888L, 1L)).thenReturn(Optional.empty());

        var request = new com.backend.situ.model.ComplaintAssignDTO(888L);
        BadRequestException ex = assertThrows(BadRequestException.class, () -> complaintService.assignComplaint(33L, request, subject));

        assertEquals("ERRORS.COMPLAINT.ASSIGNEE_NOT_FOUND", ex.getMessage());
        verify(complaintRepository, never()).save(any(Complaint.class));
    }

    private User createUser(Long id, String firstName, String lastName, Company company) {
        User user = new User();
        user.setId(id);
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setCompany(company);
        return user;
    }

    private Company createCompany(Long id, String name) {
        Company company = new Company();
        company.setId(id);
        company.setName(name);
        return company;
    }
}
