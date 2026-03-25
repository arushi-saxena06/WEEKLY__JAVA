import java.util.*;
import java.util.concurrent.*;

public class Problem3_DNSCache {

    private static class DNSEntry {
        String domain;
        String ipAddress;
        long timestamp;
        long expiryTime;

        DNSEntry(String domain, String ipAddress, int ttlSeconds) {
            this.domain = domain;
            this.ipAddress = ipAddress;
            this.timestamp = System.currentTimeMillis();
            this.expiryTime = this.timestamp + (ttlSeconds * 1000L);
        }

        boolean isExpired() {
            return System.currentTimeMillis() > expiryTime;
        }
    }

    private LinkedHashMap<String, DNSEntry> cache;
    private final int maxSize;
    private long hits = 0;
    private long misses = 0;
    private long totalLookupTime = 0;
    private long totalLookups = 0;

    private HashMap<String, String> upstreamDNS = new HashMap<>();

    public Problem3_DNSCache(int maxSize) {
        this.maxSize = maxSize;
        this.cache = new LinkedHashMap<String, DNSEntry>(16, 0.75f, true) {
            protected boolean removeEldestEntry(Map.Entry<String, DNSEntry> eldest) {
                return size() > maxSize;
            }
        };
        upstreamDNS.put("google.com", "172.217.14.206");
        upstreamDNS.put("facebook.com", "157.240.22.35");
        upstreamDNS.put("amazon.com", "176.32.98.166");

        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.scheduleAtFixedRate(this::cleanExpiredEntries, 10, 10, TimeUnit.SECONDS);
    }

    private void cleanExpiredEntries() {
        cache.entrySet().removeIf(entry -> entry.getValue().isExpired());
    }

    public String resolve(String domain) {
        long start = System.nanoTime();
        totalLookups++;

        DNSEntry entry = cache.get(domain);
        if (entry != null && !entry.isExpired()) {
            hits++;
            long elapsed = (System.nanoTime() - start) / 1_000_000;
            totalLookupTime += elapsed;
            System.out.println("resolve(\"" + domain + "\") → Cache HIT → " + entry.ipAddress + " (retrieved in " + elapsed + "ms)");
            return entry.ipAddress;
        }

        if (entry != null && entry.isExpired()) {
            cache.remove(domain);
            misses++;
            String ip = upstreamDNS.getOrDefault(domain, "0.0.0.0");
            DNSEntry newEntry = new DNSEntry(domain, ip, 300);
            cache.put(domain, newEntry);
            long elapsed = (System.nanoTime() - start) / 1_000_000;
            totalLookupTime += elapsed;
            System.out.println("resolve(\"" + domain + "\") → Cache EXPIRED → Query upstream → " + ip);
            return ip;
        }

        misses++;
        String ip = upstreamDNS.getOrDefault(domain, "0.0.0.0");
        DNSEntry newEntry = new DNSEntry(domain, ip, 300);
        cache.put(domain, newEntry);
        long elapsed = (System.nanoTime() - start) / 1_000_000;
        totalLookupTime += elapsed;
        System.out.println("resolve(\"" + domain + "\") → Cache MISS → Query upstream → " + ip + " (TTL: 300s)");
        return ip;
    }

    public void getCacheStats() {
        double hitRate = totalLookups > 0 ? (hits * 100.0 / totalLookups) : 0;
        double avgLookupTime = totalLookups > 0 ? (totalLookupTime * 1.0 / totalLookups) : 0;
        System.out.printf("getCacheStats() → Hit Rate: %.1f%%, Avg Lookup Time: %.1fms%n", hitRate, avgLookupTime);
    }

    public static void main(String[] args) {
        Problem3_DNSCache dnsCache = new Problem3_DNSCache(1000);

        dnsCache.resolve("google.com");
        dnsCache.resolve("google.com");
        dnsCache.resolve("facebook.com");
        dnsCache.resolve("google.com");
        dnsCache.getCacheStats();
    }
}
