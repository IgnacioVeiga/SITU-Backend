package com.backend.situ.service;

import com.backend.situ.entity.Company;
import com.backend.situ.entity.Line;
import com.backend.situ.entity.User;
import com.backend.situ.entity.UserCredentials;
import com.backend.situ.exception.BadRequestException;
import com.backend.situ.exception.NotFoundException;
import com.backend.situ.model.CompanySummaryDTO;
import com.backend.situ.model.LineResponseDTO;
import com.backend.situ.model.LineUpsertDTO;
import com.backend.situ.repository.AuthRepository;
import com.backend.situ.repository.LineRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class LineService {

    private final LineRepository lineRepository;
    private final AuthRepository authRepository;

    public LineService(LineRepository lineRepository, AuthRepository authRepository) {
        this.lineRepository = lineRepository;
        this.authRepository = authRepository;
    }

    @Transactional(readOnly = true)
    public List<LineResponseDTO> getAllLines(String subjectEmail) {
        Long companyId = resolveCompanyFromSubject(subjectEmail).getId();
        return lineRepository.findByCompanyIdOrderByNumberAsc(companyId).stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public LineResponseDTO getLineById(Long id, String subjectEmail) {
        Long companyId = resolveCompanyFromSubject(subjectEmail).getId();
        Line line = lineRepository.findByIdAndCompanyId(id, companyId)
                .orElseThrow(() -> new NotFoundException("ERRORS.LINE.NOT_FOUND"));
        return toResponseDTO(line);
    }

    @Transactional
    public LineResponseDTO createLine(LineUpsertDTO request, String subjectEmail) {
        if (request == null || request.number() == null || request.number().isBlank()) {
            throw new BadRequestException("ERRORS.LINE.NUMBER_REQUIRED");
        }

        User user = resolveUserFromSubject(subjectEmail);
        Long companyId = user.getCompany().getId();
        String normalizedNumber = request.number().trim();
        if (lineRepository.existsByCompanyIdAndNumberIgnoreCase(companyId, normalizedNumber)) {
            throw new BadRequestException("ERRORS.LINE.NUMBER_EXISTS");
        }

        Line line = new Line();
        line.setCompany(user.getCompany());
        line.setNumber(normalizedNumber);
        line.setName(request.name() == null ? null : request.name().trim());

        return toResponseDTO(lineRepository.save(line));
    }

    @Transactional
    public LineResponseDTO updateLine(Long id, LineUpsertDTO request, String subjectEmail) {
        if (request == null) {
            throw new BadRequestException("ERRORS.LINE.INVALID_REQUEST");
        }

        Long companyId = resolveCompanyFromSubject(subjectEmail).getId();
        Line line = lineRepository.findByIdAndCompanyId(id, companyId)
                .orElseThrow(() -> new NotFoundException("ERRORS.LINE.NOT_FOUND"));

        if (request.number() != null && !request.number().isBlank()) {
            String normalizedNumber = request.number().trim();
            boolean numberTaken = lineRepository.existsByCompanyIdAndNumberIgnoreCase(companyId, normalizedNumber)
                    && !normalizedNumber.equalsIgnoreCase(line.getNumber());
            if (numberTaken) {
                throw new BadRequestException("ERRORS.LINE.NUMBER_EXISTS");
            }
            line.setNumber(normalizedNumber);
        }

        if (request.name() != null) {
            line.setName(request.name().trim());
        }

        return toResponseDTO(lineRepository.save(line));
    }

    @Transactional
    public void deleteLine(Long id, String subjectEmail) {
        Long companyId = resolveCompanyFromSubject(subjectEmail).getId();
        Line line = lineRepository.findByIdAndCompanyId(id, companyId)
                .orElseThrow(() -> new NotFoundException("ERRORS.LINE.NOT_FOUND"));
        lineRepository.delete(line);
    }

    private User resolveUserFromSubject(String subjectEmail) {
        UserCredentials credentials = authRepository.findByEmail(subjectEmail)
                .orElseThrow(() -> new BadRequestException("ERRORS.AUTH.USER_NOT_FOUND"));
        if (credentials.getUser() == null) {
            throw new BadRequestException("ERRORS.AUTH.USER_NOT_FOUND");
        }
        return credentials.getUser();
    }

    private Company resolveCompanyFromSubject(String subjectEmail) {
        User user = resolveUserFromSubject(subjectEmail);
        if (user.getCompany() == null) {
            throw new BadRequestException("ERRORS.AUTH.USER_NOT_FOUND");
        }
        return user.getCompany();
    }

    private LineResponseDTO toResponseDTO(Line line) {
        CompanySummaryDTO company = null;
        if (line.getCompany() != null) {
            company = new CompanySummaryDTO(
                    line.getCompany().getId(),
                    line.getCompany().getName(),
                    line.getCompany().getLogo_filename()
            );
        }

        return new LineResponseDTO(
                line.getId(),
                line.getNumber(),
                line.getName(),
                company
        );
    }
}
