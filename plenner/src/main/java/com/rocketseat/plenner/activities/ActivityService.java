package com.rocketseat.plenner.activities;

import com.rocketseat.plenner.trip.Trip;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ActivityService {

    @Autowired
    private ActivitiesRepository repository;

    public ActivityResponse registerActivity(ActivityRequestPayload payload, Trip trip){
        Activity newActivity = new Activity(payload.title(), payload.occurs_at(), trip);

        this.repository.save(newActivity);

        return new ActivityResponse(newActivity.getId());
    }
}
