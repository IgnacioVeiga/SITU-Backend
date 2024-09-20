package com.backend.situ.service;

import com.backend.situ.entity.Line;
import com.backend.situ.repository.LineRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

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

    public Line createLine(Line line) {
        return lineRepository.save(line);
    }

    public Line updateLine(Long id, Line lineDetails) {
        Optional<Line> lineOptional = lineRepository.findById(id);
        if (lineOptional.isPresent()) {
            Line line = lineOptional.get();
            line.setName(lineDetails.getName());
            line.setNumber(lineDetails.getNumber());
            line.setCompany(lineDetails.getCompany());
            return lineRepository.save(line);
        }
        return null;
    }

    public void deleteLine(Long id) {
        lineRepository.deleteById(id);
    }
}
