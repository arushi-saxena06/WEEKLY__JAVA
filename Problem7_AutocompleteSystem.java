import java.util.*;

public class Problem7_AutocompleteSystem {

    private HashMap<String, Integer> queryFrequency = new HashMap<>();

    private static class TrieNode {
        HashMap<Character, TrieNode> children = new HashMap<>();
        boolean isEnd = false;
        String query = null;
    }

    private TrieNode root = new TrieNode();

    private void insertTrie(String query) {
        TrieNode node = root;
        for (char c : query.toCharArray()) {
            node.children.putIfAbsent(c, new TrieNode());
            node = node.children.get(c);
        }
        node.isEnd = true;
        node.query = query;
    }

    public void addQuery(String query, int frequency) {
        queryFrequency.put(query, frequency);
        insertTrie(query.toLowerCase());
    }

    public void updateFrequency(String query) {
        int newFreq = queryFrequency.getOrDefault(query, 0) + 1;
        queryFrequency.put(query, newFreq);
        insertTrie(query.toLowerCase());
        System.out.println("updateFrequency(\"" + query + "\") → Frequency: " + (newFreq - 1) + " → " + newFreq);
    }

    private void collectSuggestions(TrieNode node, List<String> results) {
        if (node == null) return;
        if (node.isEnd && node.query != null) {
            results.add(node.query);
        }
        for (TrieNode child : node.children.values()) {
            collectSuggestions(child, results);
        }
    }

    public List<String> search(String prefix) {
        TrieNode node = root;
        for (char c : prefix.toLowerCase().toCharArray()) {
            if (!node.children.containsKey(c)) {
                return Collections.emptyList();
            }
            node = node.children.get(c);
        }

        List<String> candidates = new ArrayList<>();
        collectSuggestions(node, candidates);

        candidates.sort((a, b) -> queryFrequency.getOrDefault(b, 0) - queryFrequency.getOrDefault(a, 0));

        return candidates.subList(0, Math.min(10, candidates.size()));
    }

    public void printSuggestions(String prefix) {
        List<String> suggestions = search(prefix);
        System.out.println("search(\"" + prefix + "\") →");
        int rank = 1;
        for (String s : suggestions) {
            System.out.printf("%d. \"%s\" (%,d searches)%n", rank++, s, queryFrequency.getOrDefault(s, 0));
        }
    }

    public static void main(String[] args) {
        Problem7_AutocompleteSystem system = new Problem7_AutocompleteSystem();

        system.addQuery("java tutorial", 1_234_567);
        system.addQuery("javascript", 987_654);
        system.addQuery("java download", 456_789);
        system.addQuery("java 21 features", 1);
        system.addQuery("java spring boot", 300_000);
        system.addQuery("java vs python", 220_000);

        system.printSuggestions("jav");

        system.updateFrequency("java 21 features");
        system.updateFrequency("java 21 features");
    }
}
