package com.backend.situ.service;

import com.backend.situ.entity.User;
import com.backend.situ.exception.BadRequestException;
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

    public Page<User> listUsers(Integer pageIndex, Integer pageSize, Long companyId) {
        Pageable pageable = PageRequest.of(pageIndex, pageSize);
        return this.userRepository.findByCompanyId(companyId, pageable);
    }

    public User getUser(Long id) {
        return this.userRepository.findById(id).orElse(null);
    }

    @Transactional
    public User createUser(User user) {
        if (user.getDni() != null && userRepository.existsByDni(user.getDni())) {
            throw new BadRequestException("ERRORS.USER.DNI_EXISTS");
        }
        return this.userRepository.save(user);
    }

    @Transactional
    public User updateUser(Long id, User changes) {
        User storedUser = this.userRepository.findById(id)
                .orElseThrow(() -> new BadRequestException("ERRORS.USER.NOT_FOUND"));

        if (changes.getDni() != null && !changes.getDni().equals(storedUser.getDni())) {
            if (this.userRepository.existsByDni(changes.getDni())) {
                throw new BadRequestException("ERRORS.USER.DNI_EXISTS");
            }
            storedUser.setDni(changes.getDni());
        }

        if (changes.getFirstName() != null) {
            storedUser.setFirstName(changes.getFirstName());
        }

        if (changes.getLastName() != null) {
            storedUser.setLastName(changes.getLastName());
        }

        if (changes.getRole() != null) {
            storedUser.setRole(changes.getRole());
        }

        if (changes.getCompany() != null) {
            storedUser.setCompany(changes.getCompany());
        }

        if (changes.getProfileImage() != null) {
            storedUser.setProfileImage(changes.getProfileImage());
        }

        return this.userRepository.save(storedUser);
    }

    @Transactional
    public void deleteUser(Long userId) {
        User storedUser = this.userRepository.findById(userId)
                .orElseThrow(() -> new BadRequestException("ERRORS.USER.NOT_FOUND"));

        try {
            this.authRepository.deleteByUserId(userId);
            this.userRepository.delete(storedUser);
        } catch (DataIntegrityViolationException ex) {
            throw new BadRequestException("ERRORS.USER.CANNOT_DELETE_REFERENCED");
        }
    }

    public Boolean existDNI(Integer dni) {
        return this.userRepository.existsByDni(dni);
    }
}
