package com.backend.situ.controller;

import com.backend.situ.service.StopService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/situ/stops")
public class StopController {

    @Autowired
    private StopService stopService;

    // TODO: Add endpoints
}
