package me.vitalframework.minigames;

import lombok.Getter;

import java.util.UUID;

public class VitalMinigameState implements VitalBaseMinigameState {
    @Getter
    private final UUID uniqueId = UUID.randomUUID();
}
