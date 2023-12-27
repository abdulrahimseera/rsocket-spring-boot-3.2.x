package com.rs.sb.rsocketserver.rest;

import io.micrometer.context.ContextSnapshot;
import io.micrometer.context.ContextSnapshotFactory;
import io.micrometer.observation.contextpropagation.ObservationThreadLocalAccessor;
import io.micrometer.tracing.Tracer;
import io.swagger.annotations.Api;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.Objects;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@Api(value = "Greetings")
@RestController
@AllArgsConstructor
@Slf4j
public class GreetingsController {

    private final Tracer tracer;
    private final ContextSnapshotFactory contextSnapshotFactory = ContextSnapshotFactory.builder().build();


    @MessageMapping(value = "/hello/rsocket")
    public Mono<String> helloRsocket() {
        return Mono.deferContextual(contextView -> {
            log.info(contextView.toString());
            try (ContextSnapshot.Scope ignored = this.contextSnapshotFactory.setThreadLocalsFrom(contextView,
                    ObservationThreadLocalAccessor.KEY)) {
                String traceId = Objects.requireNonNull(this.tracer.currentSpan()).context().traceId();
                log.info("<ACCEPTANCE_TEST> <TRACE:{}> Hello from RSocket consumer", traceId);
                return Mono.just("Hello from RSocket consumer");
            }
        });
    }

    @GetMapping(value = "/hello/http", produces = APPLICATION_JSON_VALUE)
    public Mono<String> helloHttp() {
        log.info("Hello from Http consumer");
        return Mono.just("Hello from Http consumer");
    }
}
