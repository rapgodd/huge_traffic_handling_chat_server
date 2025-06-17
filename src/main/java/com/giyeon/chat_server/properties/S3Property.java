package com.giyeon.chat_server.properties;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix="spring.cloud.aws")
@Getter
@ToString
@AllArgsConstructor
public class S3Property {

    private Credential credential;
    private S3 s3;
    private Region region;


    @AllArgsConstructor
    @Getter
    @ToString
    public static class Credential{
        private String accessKey;
        private String secretKey;
    }

    @AllArgsConstructor
    @Getter
    @ToString
    public static class S3{
        private String bucket;
    }

    @AllArgsConstructor
    @Getter
    @ToString
    public static class Region{
        private String region;
    }


}
