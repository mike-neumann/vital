package {packageName};

import me.vitalframework.Vital;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;

// we want spring boot to scan every component but our main class, since that is instatiated by our minecraft server runtime
@ComponentScan(basePackages = {"me.vitalframework", {scans}}, excludeFilters = @ComponentScan.Filter(type = FilterType.ANNOTATION, classes = Vital.Info.class))
@SpringBootApplication(scanBasePackages = {"me.vitalframework", {scans}})
public class PluginConfiguration {}