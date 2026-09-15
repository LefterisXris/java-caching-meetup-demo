package gr.skg.cachejavameetup;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
public class ProductService {
   private final ProductRepository productRepository;
   // Local cache with Caffeine
   Cache<Long, Product> cache = Caffeine.newBuilder()
         .maximumSize(10)
         .expireAfterWrite(Duration.ofMinutes(1)) // keep for a fixed duration
         // .expireAfterAccess(Duration.ofMinutes(1)) // keep frequently accessed data
         .evictionListener((key, value, cause) -> {
            System.out.println("Evicting product: " + key);
         })
         .recordStats()
         .build();

   public ProductService(ProductRepository productRepository) {this.productRepository = productRepository;}

   public Product getProduct(Long id) {
      // Thread Safe operation, and more elegant
      return cache.get(id, productRepository::findById);
   }
}