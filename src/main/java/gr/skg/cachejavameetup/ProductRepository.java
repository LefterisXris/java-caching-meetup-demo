package gr.skg.cachejavameetup;

import org.springframework.stereotype.Repository;

/**
 * A deliberately slow repository/service simulates an expensive dependency
 */
@Repository
class ProductRepository {

   public Product findById(Long id) {
      // simulate delay (1s)
      dbSlowOperation();

      return new Product(id, "iPhone", 1200.0);
   }

   private void dbSlowOperation() {
      try {
         Thread.sleep(1 * 1000);
      } catch (InterruptedException e) {
         throw new RuntimeException(e);
      }
   }
}