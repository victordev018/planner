package com.rocketseat.plenner.trip;

import com.rocketseat.plenner.activities.ActivityRequestPayload;
import com.rocketseat.plenner.activities.ActivityResponse;
import com.rocketseat.plenner.activities.ActivityService;
import com.rocketseat.plenner.participant.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/trips")
public class TripControler {

    @Autowired
    private ParticipantService participantService;

    @Autowired
    private ActivityService activityService;

    @Autowired
    private TripRepository repository;

    @PostMapping
    public ResponseEntity<TripCreateResponse> createTrip(@RequestBody TripRequestPayload payload){
        Trip newTrip = new Trip(payload);

        this.repository.save(newTrip);

        this.participantService.registerParticipantsToEvent(payload.emails_to_invite(), newTrip);

        return ResponseEntity.ok(new TripCreateResponse(newTrip.getId()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Trip> getTripDatails(@PathVariable UUID id){
        Optional<Trip> trip = repository.findById(id);

        return trip.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<Trip> updatedTrip(@PathVariable UUID id, @RequestBody TripRequestPayload payload){
        Optional<Trip> trip = repository.findById(id);

        // verificando se a trip existe
        if (trip.isPresent()){
            Trip rawTrip = trip.get();
            // atualizando iformações da viagem
            rawTrip.setStart_at(LocalDateTime.parse(payload.starts_at(), DateTimeFormatter.ISO_DATE_TIME));
            rawTrip.setEnds_at(LocalDateTime.parse(payload.ends_at(), DateTimeFormatter.ISO_DATE_TIME));
            rawTrip.setDestination(payload.destination());

            this.repository.save(rawTrip);

            return ResponseEntity.ok(rawTrip);
        }

        return ResponseEntity.notFound().build();
    }

    @GetMapping("/{id}/confirm")
    public ResponseEntity<Trip> confirmTrip(@PathVariable UUID id){
        Optional<Trip> trip = repository.findById(id);

        // verificando se a trip existe
        if (trip.isPresent()){
            Trip rawTrip = trip.get();
            // atualizando status de viagem
            rawTrip.setIsConfirmed(true);

            this.repository.save(rawTrip);
            // chamada do serviço que dispara os emails para os participantes
            this.participantService.triggerConfirmationEmailToParticipants(id);
            return ResponseEntity.ok(rawTrip);
        }

        return ResponseEntity.notFound().build();
    }

    @PostMapping({"/{id}/invite"})
    public ResponseEntity<ParticipantCreateResponse> inviteParticipant(@PathVariable UUID id, @RequestBody ParticipantRequestPayload payload){
        Optional<Trip> trip = repository.findById(id);

        // verificando se a trip existe
        if (trip.isPresent()){
            Trip rawTrip = trip.get();


            ParticipantCreateResponse participantCreateResponse = participantService.registerParticipantToEvent(payload.email(), rawTrip);

            // verificando se a viagem ja foi foi confirmada
            if (rawTrip.getIsConfirmed()) participantService.triggerConfirmationEmailToParticipant(payload.email());

            return ResponseEntity.ok(participantCreateResponse);
        }

        return ResponseEntity.notFound().build();
    }

    @PostMapping({"/{id}/activities"})
    public ResponseEntity<ActivityResponse> registerActivity(@PathVariable UUID id, @RequestBody ActivityRequestPayload payload){
        Optional<Trip> trip = repository.findById(id);

        // verificando se a trip existe
        if (trip.isPresent()){
            Trip rawTrip = trip.get();

            ActivityResponse activityResponse = this.activityService.registerActivity(payload, rawTrip);

            return ResponseEntity.ok(activityResponse);
        }

        return ResponseEntity.notFound().build();
    }

    @GetMapping("/{id}/participants")
    public ResponseEntity<List<ParticipantData>> getAllParticipats(@PathVariable UUID id){
        List<ParticipantData> participantList = this.participantService.getAllParticipantsFromEvent(id);
        return ResponseEntity.ok(participantList);
    }
}
