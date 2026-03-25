import java.util.*;

public class Problem1_UsernameChecker {

    private HashMap<String, Integer> usernameToUserId = new HashMap<>();
    private HashMap<String, Integer> attemptFrequency = new HashMap<>();

    public void registerUser(String username, int userId) {
        usernameToUserId.put(username, userId);
    }

    public boolean checkAvailability(String username) {
        attemptFrequency.put(username, attemptFrequency.getOrDefault(username, 0) + 1);
        return !usernameToUserId.containsKey(username);
    }

    public List<String> suggestAlternatives(String username) {
        List<String> suggestions = new ArrayList<>();
        for (int i = 1; i <= 3; i++) {
            String numbered = username + i;
            if (!usernameToUserId.containsKey(numbered)) {
                suggestions.add(numbered);
            }
        }
        String dotVariant = username.replace("_", ".");
        if (!usernameToUserId.containsKey(dotVariant) && !dotVariant.equals(username)) {
            suggestions.add(dotVariant);
        }
        String underscoreVariant = username + "_";
        if (!usernameToUserId.containsKey(underscoreVariant)) {
            suggestions.add(underscoreVariant);
        }
        return suggestions;
    }

    public String getMostAttempted() {
        String mostAttempted = null;
        int maxCount = 0;
        for (Map.Entry<String, Integer> entry : attemptFrequency.entrySet()) {
            if (entry.getValue() > maxCount) {
                maxCount = entry.getValue();
                mostAttempted = entry.getKey();
            }
        }
        return mostAttempted + " (" + maxCount + " attempts)";
    }

    public static void main(String[] args) {
        Problem1_UsernameChecker checker = new Problem1_UsernameChecker();

        checker.registerUser("john_doe", 1001);
        checker.registerUser("admin", 1002);
        checker.registerUser("superuser", 1003);

        for (int i = 0; i < 10543; i++) {
            checker.attemptFrequency.put("admin", checker.attemptFrequency.getOrDefault("admin", 0) + 1);
        }

        System.out.println("checkAvailability(\"john_doe\") → " + checker.checkAvailability("john_doe"));
        System.out.println("checkAvailability(\"jane_smith\") → " + checker.checkAvailability("jane_smith"));
        System.out.println("suggestAlternatives(\"john_doe\") → " + checker.suggestAlternatives("john_doe"));
        System.out.println("getMostAttempted() → " + checker.getMostAttempted());
    }
}
