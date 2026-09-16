package gr.skg.cachejavameetup;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
public class ProductService {
   private final ProductRepository productRepository;

   public ProductService(ProductRepository productRepository) {
      this.productRepository = productRepository;
   }

   @Cacheable("products")
   public Product getProduct(Long id) {
      return productRepository.findById(id);
   }

   @CacheEvict(cacheNames = "products", key = "#id")
   public Product update(Long id, Double newPrice) {
      return productRepository.update(id, newPrice);
   }
}