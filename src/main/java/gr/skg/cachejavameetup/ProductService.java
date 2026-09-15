package gr.skg.cachejavameetup;

import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class ProductService {
   private final ProductRepository productRepository;
   // Simple Cache, but thread safe
   private final Map<Long, Product> cache = new ConcurrentHashMap<>();

   public ProductService(ProductRepository productRepository) {this.productRepository = productRepository;}

   public Product getProduct(Long id) {
      // Thread Safe operation, and more elegant
      return cache.computeIfAbsent(id, productRepository::findById);
   }
}