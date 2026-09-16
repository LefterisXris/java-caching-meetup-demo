package gr.skg.cachejavameetup;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/products")
class ProductController {
   private final ProductService productService;

   ProductController(ProductService productService) {this.productService = productService;}

   @GetMapping("/{id}")
   public Product findById(@PathVariable Long id) {
      return productService.getProduct(id);
   }

}