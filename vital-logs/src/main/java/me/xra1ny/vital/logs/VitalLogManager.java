package me.xra1ny.vital.logs;

import ch.qos.logback.classic.Logger;
import me.xra1ny.essentia.inject.annotation.Component;
import me.xra1ny.vital.Vital;
import me.xra1ny.vital.VitalComponentListManager;
import org.slf4j.LoggerFactory;

import java.util.List;

@Component(dependsOn = VitalLogsSubModule.class)
public final class VitalLogManager extends VitalComponentListManager<VitalLogHandler> {
    private static final Logger log = (Logger) LoggerFactory.getLogger(VitalLogHandler.class);

    private final Vital<?> vital;

    public VitalLogManager(Vital<?> vital) {
        this.vital = vital;
    }

    @Override
    public void onRegistered() {
        log.info("mapping loggers to log handlers...");

        // get all registered loggers on current context
        final List<Logger> loggers = log.getLoggerContext().getLoggerList();

        for (Logger logger : loggers) {
            for (VitalLogHandler logHandler : getLogHandlers()) {
                log.info("{} <=> {}", logger.getClass().getSimpleName(), logHandler.getClass().getSimpleName());
                logger.addAppender(logHandler);
                // log handler may now handle all logs appending...
            }
        }

        log.info("loggers successfully mapped to log handlers");
    }

    /**
     * gets all log handlers managed by this manager instance
     *
     * @return a list of all managed log handlers
     */
    public List<VitalLogHandler> getLogHandlers() {
        return vital.getComponentsByType(VitalLogHandler.class);
    }
}
