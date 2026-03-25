import java.util.*;

public class Problem9_FinancialTransactions {

    private static class Transaction {
        int id;
        double amount;
        String merchant;
        String time;
        String account;

        Transaction(int id, double amount, String merchant, String time) {
            this.id = id;
            this.amount = amount;
            this.merchant = merchant;
            this.time = time;
            this.account = "acc" + id;
        }
    }

    private List<Transaction> transactions = new ArrayList<>();

    public void addTransaction(int id, double amount, String merchant, String time) {
        transactions.add(new Transaction(id, amount, merchant, time));
    }

    public List<int[]> findTwoSum(double target) {
        HashMap<Double, Transaction> seen = new HashMap<>();
        List<int[]> result = new ArrayList<>();

        for (Transaction tx : transactions) {
            double complement = target - tx.amount;
            if (seen.containsKey(complement)) {
                Transaction other = seen.get(complement);
                result.add(new int[]{other.id, tx.id});
            }
            seen.put(tx.amount, tx);
        }
        return result;
    }

    private int parseTimeMinutes(String time) {
        String[] parts = time.split(":");
        return Integer.parseInt(parts[0]) * 60 + Integer.parseInt(parts[1]);
    }

    public List<int[]> findTwoSumWithTimeWindow(double target, int windowMinutes) {
        List<int[]> result = new ArrayList<>();
        for (int i = 0; i < transactions.size(); i++) {
            for (int j = i + 1; j < transactions.size(); j++) {
                Transaction a = transactions.get(i);
                Transaction b = transactions.get(j);
                int timeDiff = Math.abs(parseTimeMinutes(a.time) - parseTimeMinutes(b.time));
                if (timeDiff <= windowMinutes && a.amount + b.amount == target) {
                    result.add(new int[]{a.id, b.id});
                }
            }
        }
        return result;
    }

    public List<int[]> findKSum(int k, double target) {
        List<int[]> result = new ArrayList<>();
        kSumHelper(transactions, k, target, 0, new int[k], 0, result);
        return result;
    }

    private void kSumHelper(List<Transaction> txs, int k, double remaining, int start, int[] current, int idx, List<int[]> result) {
        if (idx == k) {
            if (remaining == 0) result.add(Arrays.copyOf(current, current.length));
            return;
        }
        for (int i = start; i < txs.size(); i++) {
            current[idx] = txs.get(i).id;
            kSumHelper(txs, k, remaining - txs.get(i).amount, i + 1, current, idx + 1, result);
        }
    }

    public void detectDuplicates() {
        HashMap<String, List<Transaction>> key = new HashMap<>();
        for (Transaction tx : transactions) {
            String k = tx.amount + "_" + tx.merchant;
            key.computeIfAbsent(k, x -> new ArrayList<>()).add(tx);
        }

        System.out.println("detectDuplicates() →");
        for (Map.Entry<String, List<Transaction>> entry : key.entrySet()) {
            if (entry.getValue().size() > 1) {
                Transaction first = entry.getValue().get(0);
                List<String> accounts = new ArrayList<>();
                for (Transaction t : entry.getValue()) accounts.add(t.account);
                System.out.printf("[{amount: %.0f, merchant: \"%s\", accounts: %s}]%n",
                        first.amount, first.merchant, accounts);
            }
        }
    }

    public static void main(String[] args) {
        Problem9_FinancialTransactions detector = new Problem9_FinancialTransactions();

        detector.addTransaction(1, 500, "Store A", "10:00");
        detector.addTransaction(2, 300, "Store B", "10:15");
        detector.addTransaction(3, 200, "Store C", "10:30");
        detector.addTransaction(4, 500, "Store A", "10:45");

        List<int[]> twoSumResult = detector.findTwoSum(500);
        System.out.print("findTwoSum(target=500) → [");
        for (int[] pair : twoSumResult) System.out.print("(id:" + pair[0] + ", id:" + pair[1] + ")");
        System.out.println("]");

        detector.detectDuplicates();

        List<int[]> kSumResult = detector.findKSum(3, 1000);
        System.out.print("findKSum(k=3, target=1000) → [");
        for (int[] group : kSumResult) {
            System.out.print("(");
            for (int i = 0; i < group.length; i++) {
                if (i > 0) System.out.print(", ");
                System.out.print("id:" + group[i]);
            }
            System.out.print(")");
        }
        System.out.println("]");
    }
}
