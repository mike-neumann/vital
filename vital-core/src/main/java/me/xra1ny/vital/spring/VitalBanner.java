package me.xra1ny.vital.spring;

import org.springframework.boot.Banner;
import org.springframework.core.env.Environment;

import java.io.PrintStream;

public class VitalBanner implements Banner {
    @Override
    public void printBanner(Environment environment, Class<?> sourceClass, PrintStream out) {
        out.print("""
                  .
                 /\\\\ __     ___ _        _  ______ \s
                ( ( )\\ \\   / ( ) |_ __ _| | \\ \\ \\ \\\s
                 \\\\/  \\ \\ / /| | __/ _` | |  \\ \\ \\ \\
                  ,    \\ V / | | || (_| | |  / / / /
                ========\\_/==|_|\\__\\__,_|_|=/_/_/_/\s
                                                   \s
                """);
    }
}
