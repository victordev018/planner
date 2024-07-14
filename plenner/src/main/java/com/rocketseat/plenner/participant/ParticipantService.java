package com.rocketseat.plenner.participant;

import com.rocketseat.plenner.trip.Trip;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class ParticipantService {

    @Autowired
    private ParticipantRepository repository;

    public void registerParticipantsToEvent(List<String> participantsToInvite, Trip trip){
        List<Participant> participants = participantsToInvite.stream().map(email -> new Participant(email, trip)).toList();

        repository.saveAll(participants);
        System.out.println(participants.get(0).getId());
    };

    public ParticipantCreateResponse registerParticipantToEvent(String email, Trip trip){
        Participant newParticipant = new Participant(email, trip);
        this.repository.save(newParticipant);

        return new ParticipantCreateResponse(newParticipant.getId());
    }

    public void triggerConfirmationEmailToParticipants(UUID tripId){};

    public void triggerConfirmationEmailToParticipant(String email){};
}
