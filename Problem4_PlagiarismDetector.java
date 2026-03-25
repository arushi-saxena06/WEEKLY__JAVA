import java.util.*;

public class Problem4_PlagiarismDetector {

    private HashMap<String, Set<String>> ngramIndex = new HashMap<>();
    private HashMap<String, List<String>> documentNgrams = new HashMap<>();
    private final int N = 5;

    public void indexDocument(String documentId, String content) {
        String[] words = content.toLowerCase().split("\\s+");
        List<String> ngrams = new ArrayList<>();

        for (int i = 0; i <= words.length - N; i++) {
            StringBuilder ngram = new StringBuilder();
            for (int j = i; j < i + N; j++) {
                if (j > i) ngram.append(" ");
                ngram.append(words[j]);
            }
            String ngramStr = ngram.toString();
            ngrams.add(ngramStr);
            ngramIndex.computeIfAbsent(ngramStr, k -> new HashSet<>()).add(documentId);
        }
        documentNgrams.put(documentId, ngrams);
    }

    public void analyzeDocument(String documentId) {
        List<String> ngrams = documentNgrams.get(documentId);
        if (ngrams == null) {
            System.out.println("Document not found.");
            return;
        }

        System.out.println("analyzeDocument(\"" + documentId + "\")");
        System.out.println("→ Extracted " + ngrams.size() + " n-grams");

        HashMap<String, Integer> matchCounts = new HashMap<>();
        for (String ngram : ngrams) {
            Set<String> docs = ngramIndex.get(ngram);
            if (docs != null) {
                for (String otherDoc : docs) {
                    if (!otherDoc.equals(documentId)) {
                        matchCounts.put(otherDoc, matchCounts.getOrDefault(otherDoc, 0) + 1);
                    }
                }
            }
        }

        for (Map.Entry<String, Integer> entry : matchCounts.entrySet()) {
            int matchingNgrams = entry.getValue();
            double similarity = (matchingNgrams * 100.0) / ngrams.size();
            String verdict = similarity > 50 ? "PLAGIARISM DETECTED" : "suspicious";
            System.out.printf("→ Found %d matching n-grams with \"%s\"%n", matchingNgrams, entry.getKey());
            System.out.printf("→ Similarity: %.1f%% (%s)%n", similarity, verdict);
        }
    }

    public static void main(String[] args) {
        Problem4_PlagiarismDetector detector = new Problem4_PlagiarismDetector();

        String essay089 = "the quick brown fox jumps over the lazy dog and the fox was very quick indeed in its movement across the field";
        String essay092 = "the quick brown fox jumps over the lazy dog and the fox was very quick indeed in its movement across the field this is almost identical content with only minor changes at the very end";
        String essay123 = "the quick brown fox jumps over the lazy dog and the fox was very quick indeed in its movement across the field this is almost identical content";

        detector.indexDocument("essay_089.txt", essay089);
        detector.indexDocument("essay_092.txt", essay092);
        detector.indexDocument("essay_123.txt", essay123);

        detector.analyzeDocument("essay_123.txt");
    }
}
