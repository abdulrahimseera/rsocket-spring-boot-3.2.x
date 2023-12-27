package com.rs.sb.rsocketserver.config;

import io.rsocket.core.Resume;
import io.rsocket.frame.decoder.PayloadDecoder;
import org.springframework.boot.rsocket.server.RSocketServerCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.codec.json.Jackson2JsonDecoder;
import org.springframework.http.codec.json.Jackson2JsonEncoder;
import org.springframework.messaging.rsocket.RSocketStrategies;
import org.springframework.messaging.rsocket.annotation.support.RSocketMessageHandler;
import org.springframework.util.MimeType;

import java.time.Duration;

@Configuration
public class RSocketServerConfiguration {

    @Bean
    public RSocketMessageHandler rsocketMessageHandler() {
        RSocketMessageHandler handler = new RSocketMessageHandler();
        handler.setRSocketStrategies(rsocketStrategies());
        return handler;
    }

    @Bean
    public RSocketServerCustomizer customizer(){
        return c -> c.resume(resumeStrategy())
                .maxInboundPayloadSize(Integer.MAX_VALUE)
                .payloadDecoder(PayloadDecoder.ZERO_COPY)
                .fragment(1024 * 1024);
    }
    private Resume resumeStrategy(){
        return new Resume()
                .sessionDuration(Duration.ofMinutes(10));
    }

    @Bean
    public RSocketStrategies rsocketStrategies() {
        return RSocketStrategies.builder()
                .metadataExtractorRegistry(registry ->
                    registry.metadataToExtract(MimeType.valueOf("message/x.apiKey"), String.class, "apiKey")
                )
                .decoder(new Jackson2JsonDecoder())
                .encoder(new Jackson2JsonEncoder())
                .build();
    }
}