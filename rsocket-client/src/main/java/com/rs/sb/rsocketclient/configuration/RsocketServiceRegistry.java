package com.rs.sb.rsocketclient.configuration;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
@Slf4j
public class RsocketServiceRegistry {

    public List<RSocketServerInstance> getServers() {
        List<RSocketServerInstance> servers = new ArrayList<>();

        try {
            InetAddress[] hostAddresses = InetAddress.getAllByName("localhost");

            Arrays.stream(hostAddresses).forEach(host -> {
                        log.info("Rsocket IPs {} ", host.getHostAddress());
                        servers.add(RSocketServerInstance.builder()
                                .host(host.getHostAddress())
                                .port(7003)
                                .build());
                    }
            );
        } catch (UnknownHostException exp) {
            throw new RuntimeException(exp);
        }
        return servers;
    }
}