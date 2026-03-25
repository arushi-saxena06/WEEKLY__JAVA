import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;

public class Problem6_RateLimiter {

    private static class TokenBucket {
        AtomicInteger tokens;
        long lastRefillTime;
        final int maxTokens;
        final int refillRate;

        TokenBucket(int maxTokens, int refillRate) {
            this.maxTokens = maxTokens;
            this.refillRate = refillRate;
            this.tokens = new AtomicInteger(maxTokens);
            this.lastRefillTime = System.currentTimeMillis();
        }

        synchronized boolean consume() {
            refill();
            if (tokens.get() > 0) {
                tokens.decrementAndGet();
                return true;
            }
            return false;
        }

        synchronized void refill() {
            long now = System.currentTimeMillis();
            long elapsed = now - lastRefillTime;
            int tokensToAdd = (int) (elapsed / 1000) * refillRate;
            if (tokensToAdd > 0) {
                tokens.set(Math.min(maxTokens, tokens.get() + tokensToAdd));
                lastRefillTime = now;
            }
        }

        long getRetryAfter() {
            long now = System.currentTimeMillis();
            long elapsed = now - lastRefillTime;
            long remainingMs = 3600_000L - elapsed;
            return remainingMs / 1000;
        }
    }

    private ConcurrentHashMap<String, TokenBucket> clientBuckets = new ConcurrentHashMap<>();
    private final int maxRequestsPerHour;

    public Problem6_RateLimiter(int maxRequestsPerHour) {
        this.maxRequestsPerHour = maxRequestsPerHour;
    }

    public String checkRateLimit(String clientId) {
        clientBuckets.putIfAbsent(clientId, new TokenBucket(maxRequestsPerHour, maxRequestsPerHour / 3600));
        TokenBucket bucket = clientBuckets.get(clientId);

        if (bucket.consume()) {
            return "Allowed (" + bucket.tokens.get() + " requests remaining)";
        } else {
            long retryAfter = bucket.getRetryAfter();
            return "Denied (0 requests remaining, retry after " + retryAfter + "s)";
        }
    }

    public void getRateLimitStatus(String clientId) {
        TokenBucket bucket = clientBuckets.get(clientId);
        if (bucket == null) {
            System.out.println("Client not found.");
            return;
        }
        int used = maxRequestsPerHour - bucket.tokens.get();
        long resetTime = (System.currentTimeMillis() / 1000) + bucket.getRetryAfter();
        System.out.printf("getRateLimitStatus(\"%s\") → {used: %d, limit: %d, reset: %d}%n",
                clientId, used, maxRequestsPerHour, resetTime);
    }

    public static void main(String[] args) {
        Problem6_RateLimiter limiter = new Problem6_RateLimiter(1000);

        System.out.println("checkRateLimit(\"abc123\") → " + limiter.checkRateLimit("abc123"));
        System.out.println("checkRateLimit(\"abc123\") → " + limiter.checkRateLimit("abc123"));

        for (int i = 2; i < 1000; i++) {
            limiter.checkRateLimit("abc123");
        }

        System.out.println("checkRateLimit(\"abc123\") → " + limiter.checkRateLimit("abc123"));
        limiter.getRateLimitStatus("abc123");
    }
}
