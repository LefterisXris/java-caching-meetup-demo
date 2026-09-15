package gr.skg.cachejavameetup;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
public class ProductService {
   private final ProductRepository productRepository;
   // Loading cache with Caffeine
   final LoadingCache<Long, Product> cache;

   public ProductService(ProductRepository productRepository) {
      this.productRepository = productRepository;
      this.cache = Caffeine.newBuilder()
            .maximumSize(10)
            .refreshAfterWrite(Duration.ofSeconds(10)) // refresh is useful, to keep data "fresh"
            .expireAfterWrite(Duration.ofMinutes(1)) // keep for a fixed duration
            // .expireAfterAccess(Duration.ofMinutes(1)) // keep frequently accessed data
            .evictionListener((key, value, cause) -> {
               System.out.println("Evicting product: " + key);
            })
            .recordStats()
            .build(productRepository::findById);
   }

   public Product getProduct(Long id) {
      // The cache now knows how to obtain a missing value.
      return cache.get(id);
   }

}