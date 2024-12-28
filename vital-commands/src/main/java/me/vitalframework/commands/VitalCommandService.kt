package me.vitalframework.commands;

import lombok.NonNull;
import me.vitalframework.Vital;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;

@Service
public class VitalCommandService {
    public Collection<? extends VitalCommand<?, ?>> getCommands(@NonNull Class<? extends VitalCommand<?, ?>> vitalInventoryClass) {
        try {
            return Vital.getContext().getBeansOfType(vitalInventoryClass).values();
        } catch (Exception e) {
            return List.of();
        }
    }

    // must be suppressed otherwise compiler cant convert types safely
    @SuppressWarnings({"RedundantCast", "unchecked"})
    public Collection<? extends VitalCommand<?, ?>> getCommands() {
        return getCommands((Class<? extends VitalCommand<?, ?>>) ((Class<?>) (VitalCommand.class)));
    }

    public <T extends VitalCommand<?, ?>> T getCommand(@NonNull Class<T> vitalInventoryClass) {
        try {
            return Vital.getContext().getBean(vitalInventoryClass);
        } catch (Exception e) {
            return null;
        }
    }
}