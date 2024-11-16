package me.vitalframework.tasks;

import lombok.NonNull;
import me.vitalframework.Vital;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;

@Service
public class VitalTaskService {
    @NonNull
    public Collection<? extends VitalRepeatableTask<?, ?, ?>> getRepeatableTasks(Class<? extends VitalRepeatableTask<?, ?, ?>> vitalRepeatableTaskClass) {
        try {
            return Vital.getContext().getBeansOfType(vitalRepeatableTaskClass).values();
        } catch (Exception e) {
            return List.of();
        }
    }

    // must be suppressed for compile safety cast
    @SuppressWarnings({"unchecked", "RedundantCast"})
    @NonNull
    public Collection<? extends VitalRepeatableTask<?, ?, ?>> getRepeatableTasks() {
        return getRepeatableTasks((Class<? extends VitalRepeatableTask<?, ?, ?>>) ((Class<?>) VitalRepeatableTask.class));
    }


    public <T extends VitalRepeatableTask<?, ?, ?>> T getRepeatableTask(@NonNull Class<T> vitalRepeatableTaskClass) {
        try {
            return Vital.getContext().getBean(vitalRepeatableTaskClass);
        } catch (Exception e) {
            return null;
        }
    }

    @NonNull
    public Collection<? extends VitalCountdownTask<?, ?>> getCountdownTasks(@NonNull Class<? extends VitalCountdownTask<?, ?>> vitalCountdownTaskClass) {
        try {
            return Vital.getContext().getBeansOfType(vitalCountdownTaskClass).values();
        } catch (Exception e) {
            return List.of();
        }
    }

    // must be suppressed for compile safety cast
    @SuppressWarnings({"unchecked", "RedundantCast"})
    @NonNull
    public Collection<? extends VitalCountdownTask<?, ?>> getCountdownTasks() {
        return getCountdownTasks((Class<? extends VitalCountdownTask<?, ?>>) ((Class<?>) VitalCountdownTask.class));
    }


    public <T extends VitalCountdownTask<?, ?>> T getCountdownTask(@NonNull Class<T> vitalCountdownTaskClass) {
        try {
            return Vital.getContext().getBean(vitalCountdownTaskClass);
        } catch (Exception e) {
            return null;
        }
    }
}
