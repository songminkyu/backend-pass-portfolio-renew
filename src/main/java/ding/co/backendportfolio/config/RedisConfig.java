package ding.co.backendportfolio.config;

import io.lettuce.core.SocketOptions;
import io.lettuce.core.TimeoutOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;

import java.time.Duration;

@Configuration
public class RedisConfig {

    @Value("${spring.data.redis.host:localhost}")
    private String host;

    @Value("${spring.data.redis.port:6379}")
    private int port;

    @Bean
    public RedisConnectionFactory redisConnectionFactory() {
        RedisStandaloneConfiguration redisConfiguration = new RedisStandaloneConfiguration(host, port);

        // Lettuce 클라이언트 설정 강화
        LettuceClientConfiguration clientConfig = LettuceClientConfiguration.builder()
                // 커맨드 타임아웃을 2초(기본값 60s 등 환경에 따라 다름) 이상으로 명시적 설정
                // 로컬 테스트 환경이 느릴 경우를 대비해 넉넉하게 설정
                .commandTimeout(Duration.ofSeconds(5))
                .clientOptions(io.lettuce.core.ClientOptions.builder()
                        .socketOptions(SocketOptions.builder()
                                .keepAlive(true) // TCP KeepAlive 활성화
                                .connectTimeout(Duration.ofSeconds(5)) // 연결 타임아웃 설정
                                .build())
                        .timeoutOptions(TimeoutOptions.enabled())
                        .build())
                .build();

        return new LettuceConnectionFactory(redisConfiguration, clientConfig);
    }
}
