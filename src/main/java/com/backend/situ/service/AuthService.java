package com.backend.situ.service;

import com.backend.situ.entity.LogInCredentials;
import com.backend.situ.model.SignUpForm;
import com.backend.situ.repository.AuthRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AuthService {
    @Autowired
    private AuthRepository authRepository;

    @Autowired
    public AuthService(AuthRepository authRepository) {
        this.authRepository = authRepository;
    }

    public Integer doLogin(LogInCredentials cred) {
        LogInCredentials user = this.authRepository.findByEmail(cred.email);

        if ((user == null) || (!user.getPassword().equals(cred.getPassword()))) {
            return 0;
        }

        return user.userId;
    }

    public boolean signup(SignUpForm form) {
        return true;
    }
}
