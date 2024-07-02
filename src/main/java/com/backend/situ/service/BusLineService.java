package com.backend.situ.service;

import com.backend.situ.model.BusLine;
import org.springframework.stereotype.Service;

@Service
public class BusLineService {

    public BusLine[] listBuses() {
        return new BusLine[]{
                new BusLine(
                        1,
                        276,
                        new String[]{
                                "Luján - Cazador",
                                "Cazador - Luján",
                                "Pilar - Paraná",
                                "Paraná - Pilar",
                                "Zelaya - Escobar",
                                "Escobar - Zelaya"
                        }
                ),
                new BusLine(
                        2,
                        194,
                        new String[]{
                                "Zarate - Once [Común]",
                                "Once - Zarate [Común]",
                                "Zarate - Once [Expreso]",
                                "Once - Zarate [Expreso]",
                                "Zarate - Once [Directo]",
                                "Once - Zarate [Directo]"
                        }
                )
        };
    }
}
