package com.backend.situ.service;

import com.backend.situ.entity.Line;
import com.backend.situ.repository.LineRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class LineService {

    @Autowired
    private LineRepository lineRepository;

    public List<Line> getAllLines() {
        return lineRepository.findAll();
    }

    public Line getLineById(Long id) {
        return lineRepository.findById(id).orElse(null);
    }

    // TODO: Add more methods
}

