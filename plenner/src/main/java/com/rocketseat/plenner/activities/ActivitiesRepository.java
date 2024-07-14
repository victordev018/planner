package com.rocketseat.plenner.activities;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ActivitiesRepository extends JpaRepository<Activity, UUID> {
}
