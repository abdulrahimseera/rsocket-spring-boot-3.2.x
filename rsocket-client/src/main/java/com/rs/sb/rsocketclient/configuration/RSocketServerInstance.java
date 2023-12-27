package com.rs.sb.rsocketclient.configuration;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RSocketServerInstance {
    private String host;
    private int port;
}