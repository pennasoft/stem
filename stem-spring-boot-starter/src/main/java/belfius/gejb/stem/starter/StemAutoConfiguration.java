package belfius.gejb.stem.starter;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.ComponentScan;

/**
 * Spring Boot auto-configuration for the Stem library.
 * <p>
 * Automatically configures the Stem Standalone Task Engine Module when it is
 * present on the classpath. Consumers can override individual beans by defining
 * their own beans of the same type.
 */
@AutoConfiguration
@ComponentScan(basePackages = {
        "belfius.gejb.stem.core",
        "belfius.gejb.stem.api"
})
public class StemAutoConfiguration {
}
