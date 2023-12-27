package com.rs.sb.rsocketclient.rest;

import io.micrometer.context.ContextRegistry;
import io.micrometer.context.ContextSnapshot;
import io.micrometer.context.ContextSnapshotFactory;
import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationRegistry;
import io.micrometer.observation.contextpropagation.ObservationThreadLocalAccessor;
import io.micrometer.tracing.Tracer;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageExceptionHandler;
import org.springframework.messaging.rsocket.RSocketRequester;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Objects;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@Slf4j
@AllArgsConstructor
public class GreetingsRsocketController {
    private final RSocketRequester rSocketRequester;
    private final WebClient webClient;
    private final Tracer tracer;
    private final ObservationRegistry observationRegistry;
    private final ContextSnapshotFactory contextSnapshotFactory = ContextSnapshotFactory.builder().build();

    @GetMapping(value = "/hello/http", produces = APPLICATION_JSON_VALUE)
    public Mono<String> helloHttp() {
        return webClient
                .get()
                .uri("http://localhost:2222/hello/http")
                .exchangeToMono(clientResponse -> clientResponse
                        .bodyToMono(String.class));
    }

    @GetMapping(value = "/hello/rsocket", produces = APPLICATION_JSON_VALUE)
    public Mono<String> helloRsocket() {
        ObservationThreadLocalAccessor.getInstance().setObservationRegistry(observationRegistry);
        ContextRegistry contextRegistry = new ContextRegistry();
        contextRegistry.registerThreadLocalAccessor(new ObservationThreadLocalAccessor());
         ContextSnapshotFactory.builder().contextRegistry(contextRegistry).build().captureAll();

        Observation child = Observation.start("child", observationRegistry);
        return Mono.deferContextual(contextView -> {
                    try (ContextSnapshot.Scope ignored = this.contextSnapshotFactory.setThreadLocalsFrom(contextView,
                            ObservationThreadLocalAccessor.KEY)) {

                            log.info("<ACCEPTANCE_TEST> <TRACE:{}> Hello from Rsocket producer",
                                Objects.requireNonNull(this.tracer.currentSpan()).context().traceId());
                        return rSocketRequester
                                .route("/hello/rsocket")
                                .retrieveMono(String.class);
                    }
                })
                .contextWrite(context -> context.put(ObservationThreadLocalAccessor.KEY, child))
                .doFinally(signalType -> child.stop());

    }

    @MessageExceptionHandler
    public Mono<String> handleException(Exception e) {
        rSocketRequester.dispose();
        return Mono.error(e);
    }
}
