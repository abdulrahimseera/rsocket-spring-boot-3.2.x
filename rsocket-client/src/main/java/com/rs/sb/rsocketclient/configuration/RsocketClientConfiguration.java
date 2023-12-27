package com.rs.sb.rsocketclient.configuration;

import io.rsocket.core.Resume;
import io.rsocket.frame.decoder.PayloadDecoder;
import io.rsocket.loadbalance.LoadbalanceTarget;
import io.rsocket.loadbalance.RoundRobinLoadbalanceStrategy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.codec.json.Jackson2JsonDecoder;
import org.springframework.http.codec.json.Jackson2JsonEncoder;
import org.springframework.messaging.rsocket.RSocketRequester;
import org.springframework.messaging.rsocket.RSocketStrategies;
import org.springframework.util.MimeTypeUtils;
import reactor.core.publisher.Flux;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.util.List;

@Slf4j
@Configuration
public class RsocketClientConfiguration {

    @Bean
    public RSocketRequester getRSocketRequester(Flux<List<LoadbalanceTarget>> targetFlux) {
        RSocketRequester.Builder builder = RSocketRequester.builder();

        return builder
                .rsocketConnector(rSocketConnector ->
                        rSocketConnector
                                .resume(resumeStrategy())
                                .reconnect(retryStrategy())
                                .fragment(1024 * 1024)
                                .payloadDecoder(PayloadDecoder.ZERO_COPY)
                                .maxInboundPayloadSize(Integer.MAX_VALUE)
                )
                .rsocketStrategies(rsocketStrategies())
                .dataMimeType(MimeTypeUtils.APPLICATION_JSON)
                .transports(targetFlux, new RoundRobinLoadbalanceStrategy());
    }

    @Bean
    public RSocketStrategies rsocketStrategies() {

        return RSocketStrategies.builder()
                .decoder(new Jackson2JsonDecoder())
                .encoder(new Jackson2JsonEncoder())
                .build();
    }

    private Resume resumeStrategy(){
        return new Resume()
                .retry(Retry.fixedDelay(2000, Duration.ofSeconds(2))
                        .doBeforeRetry(s -> log.info("resume - retry :" + s.totalRetriesInARow())));
    }

    private Retry retryStrategy(){
        return Retry.fixedDelay(100, Duration.ofSeconds(1))
                .doBeforeRetry(s -> log.info("Retrying connection : " + s.totalRetriesInARow()));
    }
}