import java.util.*;

public class Problem10_MultiLevelCache {

    private static class VideoData {
        String videoId;
        byte[] data;

        VideoData(String videoId) {
            this.videoId = videoId;
            this.data = new byte[0];
        }
    }

    private LinkedHashMap<String, VideoData> l1Cache;
    private LinkedHashMap<String, String> l2Cache;
    private HashMap<String, VideoData> l3Database = new HashMap<>();
    private HashMap<String, Integer> accessCount = new HashMap<>();

    private long l1Hits = 0, l1Misses = 0;
    private long l2Hits = 0, l2Misses = 0;
    private long l3Hits = 0, l3Misses = 0;
    private long totalTime = 0;
    private long totalRequests = 0;

    private final int L1_MAX = 10_000;
    private final int L2_MAX = 100_000;
    private final int PROMOTION_THRESHOLD = 2;

    public Problem10_MultiLevelCache() {
        l1Cache = new LinkedHashMap<String, VideoData>(16, 0.75f, true) {
            protected boolean removeEldestEntry(Map.Entry<String, VideoData> eldest) {
                return size() > L1_MAX;
            }
        };
        l2Cache = new LinkedHashMap<String, String>(16, 0.75f, true) {
            protected boolean removeEldestEntry(Map.Entry<String, String> eldest) {
                return size() > L2_MAX;
            }
        };
    }

    public void seedDatabase(String videoId) {
        l3Database.put(videoId, new VideoData(videoId));
    }

    public VideoData getVideo(String videoId) {
        long start = System.nanoTime();
        totalRequests++;

        if (l1Cache.containsKey(videoId)) {
            l1Hits++;
            long elapsed = (System.nanoTime() - start) / 1_000_000;
            totalTime += elapsed;
            System.out.println("getVideo(\"" + videoId + "\")\n→ L1 Cache HIT (0.5ms)");
            return l1Cache.get(videoId);
        }
        l1Misses++;

        if (l2Cache.containsKey(videoId)) {
            l2Hits++;
            VideoData data = new VideoData(videoId);
            int count = accessCount.getOrDefault(videoId, 0) + 1;
            accessCount.put(videoId, count);

            if (count >= PROMOTION_THRESHOLD) {
                l1Cache.put(videoId, data);
                System.out.println("getVideo(\"" + videoId + "\")\n→ L1 Cache MISS (0.5ms)\n→ L2 Cache HIT (5ms)\n→ Promoted to L1\n→ Total: 5.5ms");
            } else {
                System.out.println("getVideo(\"" + videoId + "\")\n→ L1 Cache MISS (0.5ms)\n→ L2 Cache HIT (5ms)\n→ Total: 5.5ms");
            }

            totalTime += 5;
            return data;
        }
        l2Misses++;

        if (l3Database.containsKey(videoId)) {
            l3Hits++;
            VideoData data = l3Database.get(videoId);
            accessCount.put(videoId, accessCount.getOrDefault(videoId, 0) + 1);
            l2Cache.put(videoId, "/ssd/path/" + videoId);
            totalTime += 150;
            System.out.println("getVideo(\"" + videoId + "\")\n→ L1 Cache MISS\n→ L2 Cache MISS\n→ L3 Database HIT (150ms)\n→ Added to L2 (access count: " + accessCount.get(videoId) + ")");
            return data;
        }

        l3Misses++;
        System.out.println("getVideo(\"" + videoId + "\") → Not found in any cache tier.");
        return null;
    }

    public void invalidate(String videoId) {
        l1Cache.remove(videoId);
        l2Cache.remove(videoId);
        accessCount.remove(videoId);
        System.out.println("invalidate(\"" + videoId + "\") → Removed from L1 and L2 caches.");
    }

    public void getStatistics() {
        long l1Total = l1Hits + l1Misses;
        long l2Total = l2Hits + l2Misses;
        long l3Total = l3Hits + l3Misses;
        double overallHitRate = totalRequests > 0 ? ((l1Hits + l2Hits + l3Hits) * 100.0 / totalRequests) : 0;
        double avgTime = totalRequests > 0 ? (totalTime * 1.0 / totalRequests) : 0;

        System.out.println("\ngetStatistics() →");
        System.out.printf("L1: Hit Rate %.0f%%, Avg Time: 0.5ms%n", l1Total > 0 ? (l1Hits * 100.0 / l1Total) : 0);
        System.out.printf("L2: Hit Rate %.0f%%, Avg Time: 5ms%n", l2Total > 0 ? (l2Hits * 100.0 / l2Total) : 0);
        System.out.printf("L3: Hit Rate %.0f%%, Avg Time: 150ms%n", l3Total > 0 ? (l3Hits * 100.0 / l3Total) : 0);
        System.out.printf("Overall: Hit Rate %.0f%%, Avg Time: %.1fms%n", overallHitRate, avgTime);
    }

    public static void main(String[] args) {
        Problem10_MultiLevelCache cache = new Problem10_MultiLevelCache();

        for (int i = 0; i < 200; i++) {
            cache.seedDatabase("video_" + i);
        }

        cache.getVideo("video_999");
        cache.getVideo("video_123");
        cache.getVideo("video_123");
        System.out.println();
        cache.getVideo("video_999");
        System.out.println();
        cache.getVideo("video_999");

        cache.getStatistics();
    }
}
