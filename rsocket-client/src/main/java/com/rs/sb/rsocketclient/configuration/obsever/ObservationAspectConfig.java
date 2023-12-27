package com.rs.sb.rsocketclient.configuration.obsever;

import io.micrometer.observation.ObservationRegistry;
import io.micrometer.observation.aop.ObservedAspect;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
public class ObservationAspectConfig {

    @Bean
    public ObservedAspect observedAspect(ObservationRegistry observationRegistry) {
        observationRegistry.observationConfig()
                .observationHandler(new PerformanceTrackerHandler());
        return new ObservedAspect(observationRegistry);
    }
}
