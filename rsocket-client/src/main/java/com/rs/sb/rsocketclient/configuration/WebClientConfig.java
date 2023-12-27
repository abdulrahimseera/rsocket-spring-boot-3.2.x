package com.rs.sb.rsocketclient.configuration;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.http.codec.ClientCodecConfigurer;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;
import reactor.netty.resources.ConnectionProvider;

import java.time.Duration;

@Configuration
@Slf4j
public class WebClientConfig {

  @Bean
  public ConnectionProvider poolingHttpClientConnectionManager() {
    return ConnectionProvider.builder("http-client-service")
        .maxConnections(100)
        .pendingAcquireMaxCount(1000)
        .pendingAcquireTimeout(Duration.ofMillis(30000))
        .maxIdleTime(Duration.ofSeconds(3))
        .build();
  }

  @Bean
  public HttpClient httpClient(ConnectionProvider connectionProvider) {
    return HttpClient.create(connectionProvider).keepAlive(true).compress(true);
  }

  @Bean
  public ReactorClientHttpConnector clientHttpConnector(HttpClient httpClient) {
    return new ReactorClientHttpConnector(httpClient);
  }

  @Bean
  public WebClient webClient(WebClient.Builder webClientBuilder) {
    return webClientBuilder
        .defaultHeader("accept-encoding", "gzip, deflate")
        .filter(logRequest())
        .exchangeStrategies(
            ExchangeStrategies.builder()
                .codecs(
                    codecConfigurer -> {
                      ClientCodecConfigurer.ClientDefaultCodecs clientDefaultCodecs =
                          codecConfigurer.defaultCodecs();
                      clientDefaultCodecs.maxInMemorySize(1024 * 1024 * 10);
                      clientDefaultCodecs.enableLoggingRequestDetails(true);
                    })
                .build())
        .clientConnector(clientHttpConnector(httpClient(poolingHttpClientConnectionManager())))
        .build();
  }

  private ExchangeFilterFunction logRequest() {
    return ExchangeFilterFunction.ofRequestProcessor(
        clientRequest -> {
          log.info(
              "Request( url ==> {},  method ==> {}, headers ==> {}",
              clientRequest.url(),
              clientRequest.method(),
              clientRequest.headers());
          return Mono.just(clientRequest);
        });
    }
}
