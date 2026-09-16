package gr.skg.cachejavameetup;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/products")
class ProductController {
   private final ProductService productService;

   ProductController(ProductService productService) {this.productService = productService;}

   @GetMapping("/{id}")
   public Product findById(@PathVariable Long id) {
      return productService.getProduct(id);
   }

   @PutMapping("/{id}")
   public Product update(@PathVariable Long id,
                         @RequestParam Double newPrice) {
      return productService.update(id, newPrice);
   }

}