package ${packageName};

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@ComponentScan({"dev.vitalframework", ${scans}})
@SpringBootApplication
public class PluginConfiguration {}