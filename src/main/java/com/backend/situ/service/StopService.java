package com.backend.situ.service;

import com.backend.situ.entity.Stop;
import com.backend.situ.model.StopDTO;
import com.backend.situ.repository.StopRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class StopService {

    @Autowired
    private StopRepository stopRepository;

    public List<Stop> getAllStops() {
        return stopRepository.findAll();
    }

    public List<StopDTO> getStopsByRoute(Long routeId) {
        List<Object[]> results_stops_ids = stopRepository.findStopsIdByRouteId(routeId);

        List<Long> stops_ids = new ArrayList<>();
        for (Object[] result : results_stops_ids) {
            Long id = ((Number) result[0]).longValue();
            stops_ids.add(id);
        }

        List<StopDTO> stops = new ArrayList<>();
        for (Long stop_id : stops_ids)
        {
            List<Object[]> results_stops = stopRepository.findStopsById(stop_id);
            for (Object[] result : results_stops) {
                Long id = ((Number) result[0]).longValue();
                String name = (String) result[1];
                String location = (String) result[2];

                stops.add(new StopDTO(id, name, location));
            }
        }

        return stops;
    }

    public Stop createStop(Stop stop) {
        return stopRepository.save(stop);
    }

    public Stop updateStop(Long id, Stop stopDetails) {
        Optional<Stop> stopOptional = stopRepository.findById(id);
        if (stopOptional.isPresent()) {
            Stop stop = stopOptional.get();
            stop.setName(stopDetails.getName());
            stop.setLocation(stopDetails.getLocation());
            return stopRepository.save(stop);
        }
        return null;
    }

    public void deleteStop(Long id) {
        stopRepository.deleteById(id);
    }
}
