package gr.skg.cachejavameetup;

import org.springframework.cache.annotation.CachePut;
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

   @CachePut(cacheNames = "products", key = "#result.id")
   public Product update(Long id, Double newPrice) {
      return productRepository.update(id, newPrice);
   }
}