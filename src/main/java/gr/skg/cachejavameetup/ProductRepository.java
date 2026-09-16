package gr.skg.cachejavameetup;

import org.springframework.stereotype.Repository;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A deliberately slow repository/service simulates an expensive dependency
 */
@Repository
class ProductRepository {

   private final Map<Long, Product> db = new ConcurrentHashMap<>();

   public ProductRepository() {
      db.put(1L, new Product(1L, "iPhone 16", 1200.0));
      db.put(2L, new Product(2L, "MacBook Air M3", 1350.0));
      db.put(3L, new Product(3L, "Mechanical Keyboard", 129.99));
      db.put(4L, new Product(4L, "Wireless Mouse", 49.90));
      db.put(5L, new Product(5L, "27\" 4K Monitor", 399.00));
      db.put(6L, new Product(6L, "USB-C Docking Station", 179.50));
      db.put(7L, new Product(7L, "Noise Cancelling Headphones", 299.00));
      db.put(8L, new Product(8L, "Webcam 1080p", 79.00));
      db.put(9L, new Product(9L, "Ergonomic Office Chair", 450.00));
      db.put(10L, new Product(10L, "Standing Desk", 620.00));
   }

   public Product findById(Long id) {
      // simulate delay (1s)
      dbSlowOperation();

      Product product = db.get(id);
      if (product == null) {
         throw new RuntimeException("Product Not Found: " + id);
      }
      return product;
   }

   private void dbSlowOperation() {
      try {
         Thread.sleep(1 * 1000);
      } catch (InterruptedException e) {
         throw new RuntimeException(e);
      }
   }

   public Product update(Long id, Double newPrice) {
      Product product = findById(id); // not needed, but just for the 'delay' and 'not found'

      // simulating atomic DB operation
      return db.computeIfPresent(id,
            (key, existing)
                  -> new Product(existing.id(), existing.name(), newPrice));
   }
}