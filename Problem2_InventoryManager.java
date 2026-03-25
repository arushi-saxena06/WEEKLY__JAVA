import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;

public class Problem2_InventoryManager {

    private ConcurrentHashMap<String, AtomicInteger> stockLevels = new ConcurrentHashMap<>();
    private ConcurrentHashMap<String, LinkedList<Integer>> waitingLists = new ConcurrentHashMap<>();
    private ConcurrentHashMap<String, Integer> waitingListPositions = new ConcurrentHashMap<>();

    public void addProduct(String productId, int stock) {
        stockLevels.put(productId, new AtomicInteger(stock));
        waitingLists.put(productId, new LinkedList<>());
    }

    public int checkStock(String productId) {
        AtomicInteger stock = stockLevels.get(productId);
        return stock != null ? stock.get() : -1;
    }

    public String purchaseItem(String productId, int userId) {
        AtomicInteger stock = stockLevels.get(productId);
        if (stock == null) {
            return "Product not found";
        }

        int remaining;
        do {
            remaining = stock.get();
            if (remaining <= 0) {
                LinkedList<Integer> waitingList = waitingLists.get(productId);
                synchronized (waitingList) {
                    waitingList.add(userId);
                    int position = waitingList.size();
                    return "Added to waiting list, position #" + position;
                }
            }
        } while (!stock.compareAndSet(remaining, remaining - 1));

        return "Success, " + (remaining - 1) + " units remaining";
    }

    public static void main(String[] args) throws InterruptedException {
        Problem2_InventoryManager manager = new Problem2_InventoryManager();
        manager.addProduct("IPHONE15_256GB", 100);

        System.out.println("checkStock(\"IPHONE15_256GB\") → " + manager.checkStock("IPHONE15_256GB") + " units available");

        System.out.println("purchaseItem(\"IPHONE15_256GB\", userId=12345) → " + manager.purchaseItem("IPHONE15_256GB", 12345));
        System.out.println("purchaseItem(\"IPHONE15_256GB\", userId=67890) → " + manager.purchaseItem("IPHONE15_256GB", 67890));

        for (int i = 3; i <= 100; i++) {
            manager.purchaseItem("IPHONE15_256GB", 10000 + i);
        }

        System.out.println("purchaseItem(\"IPHONE15_256GB\", userId=99999) → " + manager.purchaseItem("IPHONE15_256GB", 99999));
    }
}
