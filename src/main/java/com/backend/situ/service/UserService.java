package com.backend.situ.service;

import com.backend.situ.entity.Company;
import com.backend.situ.entity.User;
import com.backend.situ.entity.UserCredentials;
import com.backend.situ.entity.image.ProfileImage;
import com.backend.situ.enums.UserRole;
import com.backend.situ.exception.BadRequestException;
import com.backend.situ.exception.NotFoundException;
import com.backend.situ.model.CompanySummaryDTO;
import com.backend.situ.model.ImageSummaryDTO;
import com.backend.situ.model.UserCreateDTO;
import com.backend.situ.model.UserResponseDTO;
import com.backend.situ.model.UserUpdateDTO;
import com.backend.situ.repository.AuthRepository;
import com.backend.situ.repository.UserRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final AuthRepository authRepository;

    public UserService(UserRepository userRepository, AuthRepository authRepository) {
        this.userRepository = userRepository;
        this.authRepository = authRepository;
    }

    @Transactional(readOnly = true)
    public Page<UserResponseDTO> listUsers(Integer pageIndex, Integer pageSize, String subjectEmail) {
        Long companyId = resolveUserFromSubject(subjectEmail).getCompany().getId();
        Pageable pageable = PageRequest.of(pageIndex, pageSize);
        return this.userRepository.findByCompanyIdOrderByLastNameAscFirstNameAsc(companyId, pageable).map(this::toResponseDTO);
    }

    @Transactional(readOnly = true)
    public UserResponseDTO getUser(Long id, String subjectEmail) {
        Long companyId = resolveUserFromSubject(subjectEmail).getCompany().getId();
        User user = this.userRepository.findByIdAndCompanyId(id, companyId)
                .orElseThrow(() -> new NotFoundException("ERRORS.USER.NOT_FOUND"));
        return toResponseDTO(user);
    }

    @Transactional
    public UserResponseDTO createUser(UserCreateDTO request, String subjectEmail) {
        if (request == null || request.dni() == null) {
            throw new BadRequestException("ERRORS.USER.DNI_REQUIRED");
        }

        User currentUser = resolveUserFromSubject(subjectEmail);
        Company company = currentUser.getCompany();
        if (company == null) {
            throw new BadRequestException("ERRORS.AUTH.USER_NOT_FOUND");
        }

        if (userRepository.existsByDniAndCompanyId(request.dni(), company.getId())) {
            throw new BadRequestException("ERRORS.USER.DNI_EXISTS");
        }

        User user = new User();
        user.setCompany(company);
        user.setDni(request.dni());
        user.setFirstName(normalizeName(request.firstName()));
        user.setLastName(normalizeName(request.lastName()));
        user.setRole(request.role() == null ? UserRole.EMPLOYEE : request.role());

        return toResponseDTO(this.userRepository.save(user));
    }

    @Transactional
    public UserResponseDTO updateUser(Long id, UserUpdateDTO changes, String subjectEmail) {
        Long companyId = resolveUserFromSubject(subjectEmail).getCompany().getId();
        User storedUser = this.userRepository.findByIdAndCompanyId(id, companyId)
                .orElseThrow(() -> new NotFoundException("ERRORS.USER.NOT_FOUND"));

        if (changes == null) {
            throw new BadRequestException("ERRORS.USER.INVALID_REQUEST");
        }

        if (changes.dni() != null && !changes.dni().equals(storedUser.getDni())) {
            if (this.userRepository.existsByDniAndCompanyId(changes.dni(), companyId)) {
                throw new BadRequestException("ERRORS.USER.DNI_EXISTS");
            }
            storedUser.setDni(changes.dni());
        }

        if (changes.firstName() != null) {
            storedUser.setFirstName(normalizeName(changes.firstName()));
        }

        if (changes.lastName() != null) {
            storedUser.setLastName(normalizeName(changes.lastName()));
        }

        if (changes.role() != null) {
            storedUser.setRole(changes.role());
        }

        return toResponseDTO(this.userRepository.save(storedUser));
    }

    @Transactional
    public void deleteUser(Long userId, String subjectEmail) {
        Long companyId = resolveUserFromSubject(subjectEmail).getCompany().getId();
        User storedUser = this.userRepository.findByIdAndCompanyId(userId, companyId)
                .orElseThrow(() -> new NotFoundException("ERRORS.USER.NOT_FOUND"));

        try {
            this.authRepository.deleteByUserId(userId);
            this.userRepository.delete(storedUser);
        } catch (DataIntegrityViolationException ex) {
            throw new BadRequestException("ERRORS.USER.CANNOT_DELETE_REFERENCED");
        }
    }

    @Transactional
    public User createUser(User user) {
        if (user.getDni() != null && userRepository.existsByDni(user.getDni())) {
            throw new BadRequestException("ERRORS.USER.DNI_EXISTS");
        }
        return this.userRepository.save(user);
    }

    public Boolean existDNI(Integer dni) {
        return this.userRepository.existsByDni(dni);
    }

    private User resolveUserFromSubject(String subjectEmail) {
        UserCredentials credentials = authRepository.findByEmail(subjectEmail)
                .orElseThrow(() -> new BadRequestException("ERRORS.AUTH.USER_NOT_FOUND"));
        if (credentials.getUser() == null) {
            throw new BadRequestException("ERRORS.AUTH.USER_NOT_FOUND");
        }
        return credentials.getUser();
    }

    private UserResponseDTO toResponseDTO(User user) {
        CompanySummaryDTO company = null;
        if (user.getCompany() != null) {
            company = new CompanySummaryDTO(
                    user.getCompany().getId(),
                    user.getCompany().getName(),
                    user.getCompany().getLogo_filename()
            );
        }

        ImageSummaryDTO profileImage = null;
        ProfileImage entityImage = user.getProfileImage();
        if (entityImage != null) {
            profileImage = new ImageSummaryDTO(entityImage.getId(), entityImage.getFilename());
        }

        return new UserResponseDTO(
                user.getId(),
                company,
                profileImage,
                user.getDni(),
                user.getFirstName(),
                user.getLastName(),
                user.getRole()
        );
    }

    private String normalizeName(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
