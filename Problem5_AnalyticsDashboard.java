import java.util.*;
import java.util.concurrent.*;

public class Problem5_AnalyticsDashboard {

    private HashMap<String, Integer> pageViews = new HashMap<>();
    private HashMap<String, Set<String>> uniqueVisitors = new HashMap<>();
    private HashMap<String, Integer> trafficSources = new HashMap<>();
    private int totalEvents = 0;

    public void processEvent(String url, String userId, String source) {
        pageViews.put(url, pageViews.getOrDefault(url, 0) + 1);
        uniqueVisitors.computeIfAbsent(url, k -> new HashSet<>()).add(userId);
        trafficSources.put(source, trafficSources.getOrDefault(source, 0) + 1);
        totalEvents++;
    }

    public List<Map.Entry<String, Integer>> getTopPages(int n) {
        List<Map.Entry<String, Integer>> entries = new ArrayList<>(pageViews.entrySet());
        entries.sort((a, b) -> b.getValue() - a.getValue());
        return entries.subList(0, Math.min(n, entries.size()));
    }

    public void getDashboard() {
        System.out.println("\n--- Dashboard ---");
        System.out.println("Top Pages:");
        List<Map.Entry<String, Integer>> topPages = getTopPages(10);
        int rank = 1;
        for (Map.Entry<String, Integer> entry : topPages) {
            int unique = uniqueVisitors.getOrDefault(entry.getKey(), Collections.emptySet()).size();
            System.out.printf("%d. %s - %,d views (%,d unique)%n", rank++, entry.getKey(), entry.getValue(), unique);
        }

        System.out.println("\nTraffic Sources:");
        int total = trafficSources.values().stream().mapToInt(Integer::intValue).sum();
        trafficSources.entrySet().stream()
                .sorted((a, b) -> b.getValue() - a.getValue())
                .forEach(e -> System.out.printf("%s: %.0f%%%n", e.getKey(), (e.getValue() * 100.0 / total)));
    }

    public static void main(String[] args) {
        Problem5_AnalyticsDashboard dashboard = new Problem5_AnalyticsDashboard();

        String[] urls = {"/article/breaking-news", "/sports/championship", "/tech/ai-updates", "/health/tips", "/finance/markets"};
        String[] sources = {"google", "facebook", "direct", "twitter", "other"};
        Random rand = new Random(42);

        for (int i = 0; i < 50000; i++) {
            String url = urls[rand.nextInt(urls.length)];
            String userId = "user_" + rand.nextInt(20000);
            String source = sources[rand.nextInt(sources.length)];
            dashboard.processEvent(url, userId, source);
        }

        dashboard.processEvent("/article/breaking-news", "user_123", "google");
        dashboard.processEvent("/article/breaking-news", "user_456", "facebook");

        dashboard.getDashboard();
    }
}
