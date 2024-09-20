package com.backend.situ.controller;

import com.backend.situ.entity.Line;
import com.backend.situ.service.LineService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

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

    @PostMapping
    public Line createLine(@RequestBody Line line) {
        return lineService.createLine(line);
    }

    @PutMapping("/{id}")
    public Line updateLine(@PathVariable Long id, @RequestBody Line lineDetails) {
        return lineService.updateLine(id, lineDetails);
    }

    @DeleteMapping("/{id}")
    public void deleteLine(@PathVariable Long id) {
        lineService.deleteLine(id);
    }
}
