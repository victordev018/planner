package com.rocketseat.plenner.trip;

import com.rocketseat.plenner.activity.*;
import com.rocketseat.plenner.link.LinkData;
import com.rocketseat.plenner.link.LinkRequestPayload;
import com.rocketseat.plenner.link.LinkResponse;
import com.rocketseat.plenner.link.LinkService;
import com.rocketseat.plenner.participant.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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
    private LinkService linkService;
    @Autowired
    private TripRepository repository;

    // TRIPS

    @PostMapping
    public ResponseEntity<TripCreateResponse> createTrip(@RequestBody TripRequestPayload payload){
        Trip newTrip = new Trip(payload);

        this.repository.save(newTrip);

        this.participantService.registerParticipantsToEvent(payload.emails_to_invite(), newTrip);

        return ResponseEntity.ok(new TripCreateResponse(newTrip.getId()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Trip> getTripDetails(@PathVariable UUID id){
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

    // ACTIVITIES

    @GetMapping("/{id}/activities")
    public ResponseEntity<List<ActivityData>> getAllActivities(@PathVariable UUID id){
        List<ActivityData> activityDataList = this.activityService.getAllActivitiesFromEvent(id);
        return ResponseEntity.ok(activityDataList);
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

    // PARTICIPANTS

    @GetMapping("/{id}/participants")
    public ResponseEntity<List<ParticipantData>> getAllParticipants(@PathVariable UUID id){
        List<ParticipantData> participantList = this.participantService.getAllParticipantsFromEvent(id);
        return ResponseEntity.ok(participantList);
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

    // LINKS

    @PostMapping({"/{id}/links"})
    public ResponseEntity<LinkResponse> registerLink(@PathVariable UUID id, @RequestBody LinkRequestPayload payload){
        Optional<Trip> trip = repository.findById(id);

        // verificando se a trip existe
        if (trip.isPresent()){
            Trip rawTrip = trip.get();

            LinkResponse linkResponse = this.linkService.registerLink(payload, rawTrip);

            return ResponseEntity.ok(linkResponse);
        }

        return ResponseEntity.notFound().build();
    }

    @GetMapping("/{id}/links")
    public ResponseEntity<List<LinkData>> getAllALinks(@PathVariable UUID id){
        List<LinkData> linkDataList = this.linkService.getAllLinksFromEvent(id);
        return ResponseEntity.ok(linkDataList);
    }
}
