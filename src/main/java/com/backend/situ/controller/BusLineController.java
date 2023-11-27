package com.backend.situ.controller;

import com.backend.situ.service.BusLineService;
import com.backend.situ.model.BusCompany;
import com.backend.situ.model.BusLine;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/situ/bus")
public class BusLineController {
    private final BusLineService busLineService;

    @Autowired
    public BusLineController(BusLineService busLineService) {
        this.busLineService = busLineService;
    }

    @GetMapping("/list")
    public BusLine[] list() {
        return this.busLineService.listBuses();
    }

    @GetMapping("/companyLogo/{companyId}")
    public BusCompany getLogo(@PathVariable("companyId") int companyId) {
        return this.busLineService.getLogo(companyId);
    }
}
