package com.backend.situ.service;

import com.backend.situ.entity.User;
import com.backend.situ.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public Page<User> listUsers(int pageIndex, int pageSize, int companyId) {
        Pageable pageable = PageRequest.of(pageIndex, pageSize);
        return this.userRepository.findByCompanyId(companyId, pageable);
    }

    public User getUser(Long id) {
        return this.userRepository.findById(id).orElse(null);
    }

    public User createUser(User user) {
        return this.userRepository.save(user);
    }
}
