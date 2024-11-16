package me.vitalframework.configs;

import lombok.NonNull;
import me.vitalframework.Vital;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;

@Service
public class VitalConfigService {
    @NonNull
    public Collection<? extends VitalConfig> getConfigs(Class<? extends VitalConfig> vitalConfigClass) {
        try {
            return Vital.getContext().getBeansOfType(vitalConfigClass).values();
        } catch (Exception e) {
            return List.of();
        }
    }

    @NonNull
    public Collection<? extends VitalConfig> getConfigs() {
        return getConfigs(VitalConfig.class);
    }


    public <T extends VitalConfig> T getConfig(@NonNull Class<T> vitalConfigClass) {
        try {
            return Vital.getContext().getBean(vitalConfigClass);
        } catch (Exception e) {
            return null;
        }
    }
}
