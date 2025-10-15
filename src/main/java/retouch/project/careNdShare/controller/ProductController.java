package retouch.project.careNdShare.controller;


import retouch.project.careNdShare.dto.ProductResponseDTO;
import retouch.project.careNdShare.entity.Product;
import retouch.project.careNdShare.entity.ProductStatus;
import retouch.project.careNdShare.entity.User;
import retouch.project.careNdShare.service.AuthService;
import retouch.project.careNdShare.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    @Autowired
    private ProductService productService;

    @Autowired
    private AuthService authService;

    @PostMapping("/add")
    public ResponseEntity<?> addProduct(
            @RequestParam String name,
            @RequestParam Double price,
            @RequestParam String category,
            @RequestParam String type,
            @RequestParam String description,
            @RequestParam MultipartFile image) {

        try {
            User currentUser = authService.getCurrentUser();
            if (currentUser == null) {
                return ResponseEntity.badRequest().body("User not authenticated");
            }

            Product product = new Product();
            product.setName(name);
            product.setPrice(price);
            product.setCategory(category);
            product.setType(type);
            product.setDescription(description);

            Product savedProduct = productService.addProduct(product, image, currentUser);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Product submitted successfully. Waiting for admin approval.");
            response.put("product", savedProduct);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("message", "Error adding product: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @GetMapping("/my-products")
    public ResponseEntity<?> getMyProducts() {
        try {
            User currentUser = authService.getCurrentUser();
            if (currentUser == null) {
                return ResponseEntity.badRequest().body("User not authenticated");
            }

            List<ProductResponseDTO> products = productService.getUserProductsDTO(currentUser.getId());
            return ResponseEntity.ok(products);

        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("message", "Error fetching products: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @GetMapping("/my-products/{status}")
    public ResponseEntity<?> getMyProductsByStatus(@PathVariable String status) {
        try {
            User currentUser = authService.getCurrentUser();
            if (currentUser == null) {
                return ResponseEntity.badRequest().body("User not authenticated");
            }

            ProductStatus productStatus;
            try {
                productStatus = ProductStatus.valueOf(status.toUpperCase());
            } catch (IllegalArgumentException e) {
                return ResponseEntity.badRequest().body("Invalid status value");
            }

            List<Product> products = productService.getUserProductsByStatus(currentUser.getId(), productStatus);
            List<ProductResponseDTO> productDTOs = products.stream()
                    .map(ProductResponseDTO::new)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(productDTOs);

        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("message", "Error fetching products: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
}