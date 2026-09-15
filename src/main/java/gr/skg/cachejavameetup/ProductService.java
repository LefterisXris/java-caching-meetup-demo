package gr.skg.cachejavameetup;

import org.springframework.stereotype.Service;

@Service
public class ProductService {
   private final ProductRepository productRepository;

   public ProductService(ProductRepository productRepository) {this.productRepository = productRepository;}

   public Product getProduct(Long id) {
      return productRepository.findById(id);
   }
}