package gr.skg.cachejavameetup;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Set;

@Service
public class ProductService {
   private final ProductRepository productRepository;
   // Shared Cache - Redis
   private final RedisTemplate<String, Product> redisTemplate;

   public ProductService(ProductRepository productRepository,
                         RedisTemplate<String, Product> redisTemplate) {
      this.productRepository = productRepository;
      this.redisTemplate = redisTemplate;
   }

   public Product getProduct(Long id) {
      String key = "product:" + id;

      Product cached = redisTemplate.opsForValue().get(key);
      if (cached != null) {
         return cached;
      }

      Product product = productRepository.findById(id);
      redisTemplate.opsForValue().set(key, product, Duration.ofMinutes(10));
      return product;
   }

   public Set<String> cacheKeys() {
      return redisTemplate.keys("product:*");
   }

}