package com.backend.situ.controller;

import com.backend.situ.model.ApiResponse;
import com.backend.situ.model.ComplaintAssignDTO;
import com.backend.situ.model.ComplaintCreateDTO;
import com.backend.situ.model.ComplaintResponseDTO;
import com.backend.situ.model.ComplaintStateUpdateDTO;
import com.backend.situ.service.ComplaintService;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/complaints")
public class ComplaintController {

    private final ComplaintService complaintService;

    public ComplaintController(ComplaintService complaintService) {
        this.complaintService = complaintService;
    }

    @GetMapping("/{pageIndex}/{pageSize}")
    public ResponseEntity<ApiResponse<Page<ComplaintResponseDTO>>> list(
            @PathVariable("pageIndex") int pageIndex,
            @PathVariable("pageSize") int pageSize
    ) {
        Page<ComplaintResponseDTO> complaints = complaintService.listComplaints(pageIndex, pageSize);
        return ResponseEntity.ok(ApiResponse.success(complaints, null));
    }

    @GetMapping("/mine/{pageIndex}/{pageSize}")
    public ResponseEntity<ApiResponse<Page<ComplaintResponseDTO>>> listMine(
            @PathVariable("pageIndex") int pageIndex,
            @PathVariable("pageSize") int pageSize,
            @RequestAttribute("auth.subject") String subjectEmail
    ) {
        Page<ComplaintResponseDTO> complaints = complaintService.listMyComplaints(subjectEmail, pageIndex, pageSize);
        return ResponseEntity.ok(ApiResponse.success(complaints, null));
    }

    @GetMapping("/{complaintId}")
    public ResponseEntity<ApiResponse<ComplaintResponseDTO>> get(@PathVariable("complaintId") Long complaintId) {
        ComplaintResponseDTO complaint = complaintService.getComplaint(complaintId);
        return ResponseEntity.ok(ApiResponse.success(complaint, null));
    }

    @GetMapping("/tracking/{trackingToken}")
    public ResponseEntity<ApiResponse<ComplaintResponseDTO>> getByTrackingToken(
            @PathVariable("trackingToken") String trackingToken
    ) {
        ComplaintResponseDTO complaint = complaintService.getComplaintByTrackingToken(trackingToken);
        return ResponseEntity.ok(ApiResponse.success(complaint, null));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ComplaintResponseDTO>> create(
            @RequestBody ComplaintCreateDTO request,
            @RequestAttribute("auth.subject") String subjectEmail
    ) {
        ComplaintResponseDTO complaint = complaintService.createComplaint(request, subjectEmail);
        return ResponseEntity.ok(ApiResponse.success(complaint, "Denuncia creada."));
    }

    @PatchMapping("/{complaintId}/state")
    public ResponseEntity<ApiResponse<ComplaintResponseDTO>> updateState(
            @PathVariable("complaintId") Long complaintId,
            @RequestBody ComplaintStateUpdateDTO request,
            @RequestAttribute("auth.subject") String subjectEmail
    ) {
        ComplaintResponseDTO complaint = complaintService.updateComplaintState(complaintId, request, subjectEmail);
        return ResponseEntity.ok(ApiResponse.success(complaint, "Estado actualizado."));
    }

    @PatchMapping("/{complaintId}/assign")
    public ResponseEntity<ApiResponse<ComplaintResponseDTO>> assign(
            @PathVariable("complaintId") Long complaintId,
            @RequestBody ComplaintAssignDTO request,
            @RequestAttribute("auth.subject") String subjectEmail
    ) {
        ComplaintResponseDTO complaint = complaintService.assignComplaint(complaintId, request, subjectEmail);
        return ResponseEntity.ok(ApiResponse.success(complaint, "Denuncia asignada."));
    }
}
