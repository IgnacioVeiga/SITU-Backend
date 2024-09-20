package com.backend.situ.controller;

import com.backend.situ.entity.Line;
import com.backend.situ.service.LineService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/situ/lines")
public class LineController {

    @Autowired
    private LineService lineService;

    @GetMapping
    public List<Line> getAllLines() {
        return lineService.getAllLines();
    }

    @GetMapping("/{id}")
    public Line getLineById(@PathVariable Long id) {
        return lineService.getLineById(id);
    }

    // TODO: Add more endpoints
}
