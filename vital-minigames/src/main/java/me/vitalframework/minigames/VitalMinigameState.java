package me.vitalframework.minigames;

import lombok.Getter;
import lombok.NonNull;

import java.util.UUID;

@Getter
public class VitalMinigameState implements VitalBaseMinigameState {
    @NonNull
    private final UUID uniqueId = UUID.randomUUID();
}