package gr.skg.cachejavameetup;

import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class ProductService {
   private final ProductRepository productRepository;
   // Simple Cache
   private final Map<Long, Product> cache = new HashMap<>();

   public ProductService(ProductRepository productRepository) {this.productRepository = productRepository;}

   public Product getProduct(Long id) {
      Product cached = cache.get(id);
      if (cached != null) {
         return cached;
      }

      Product product = productRepository.findById(id);
      cache.put(id, product);
      return product;
   }
}