package com.example.ledgercore.auth.adapter.outbound;

import com.example.ledgercore.auth.command.port.outbound.VerificationRateLimitPort;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class RedisVerificationRateLimitAdapter
        implements VerificationRateLimitPort {

    private static final String COOLDOWN_KEY_PREFIX =
            "auth:verification:cooldown:";

    private static final String COUNT_KEY_PREFIX =
            "auth:verification:count:";

    private static final long COOLDOWN_SECONDS = 30;

    private static final long WINDOW_SECONDS = 15 * 60;

    private static final long MAX_REQUESTS = 5;

    private static final DefaultRedisScript<Long> RATE_LIMIT_SCRIPT =
            new DefaultRedisScript<>(
                    """
                    local cooldownKey = KEYS[1]
                    local countKey = KEYS[2]

                    local cooldownSeconds = tonumber(ARGV[1])
                    local windowSeconds = tonumber(ARGV[2])
                    local maxRequests = tonumber(ARGV[3])

                    if redis.call('EXISTS', cooldownKey) == 1 then
                        return 1
                    end

                    local count = redis.call('GET', countKey)

                    if count and tonumber(count) >= maxRequests then
                        return 2
                    end

                    local newCount = redis.call('INCR', countKey)

                    if newCount == 1 then
                        redis.call(
                            'EXPIRE',
                            countKey,
                            windowSeconds
                        )
                    end

                    redis.call(
                        'SET',
                        cooldownKey,
                        '1',
                        'EX',
                        cooldownSeconds
                    )

                    return 0
                    """,
                    Long.class
            );

    private final StringRedisTemplate redisTemplate;

    @Override
    public RateLimitResult checkAndRecord(UUID userId) {

        String cooldownKey =
                COOLDOWN_KEY_PREFIX + userId;

        String countKey =
                COUNT_KEY_PREFIX + userId;

        Long result = redisTemplate.execute(
                RATE_LIMIT_SCRIPT,
                List.of(
                        cooldownKey,
                        countKey
                ),
                String.valueOf(COOLDOWN_SECONDS),
                String.valueOf(WINDOW_SECONDS),
                String.valueOf(MAX_REQUESTS)
        );

        if (result == null) {
            throw new IllegalStateException(
                    "Failed to execute verification rate limit"
            );
        }

        return switch (result.intValue()) {
            case 0 -> RateLimitResult.ALLOWED;
            case 1 -> RateLimitResult.COOLDOWN;
            case 2 -> RateLimitResult.LIMIT_EXCEEDED;
            default -> throw new IllegalStateException(
                    "Unknown rate limit result: " + result
            );
        };
    }
}