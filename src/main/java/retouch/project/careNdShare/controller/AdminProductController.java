package retouch.project.careNdShare.controller;

import retouch.project.careNdShare.dto.ProductResponseDTO;
import retouch.project.careNdShare.entity.Product;
import retouch.project.careNdShare.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/products")
@PreAuthorize("hasRole('ADMIN')")
public class AdminProductController {

    @Autowired
    private ProductService productService;

    @GetMapping("/pending")
    public ResponseEntity<?> getPendingProducts() {
        try {
            List<ProductResponseDTO> pendingProducts = productService.getPendingProductsDTO();
            return ResponseEntity.ok(pendingProducts);
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("message", "Error fetching pending products: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping("/{productId}/approve")
    public ResponseEntity<?> approveProduct(@PathVariable Long productId) {
        try {
            Product approvedProduct = productService.approveProduct(productId);
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Product approved successfully");
            response.put("product", approvedProduct);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("message", "Error approving product: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping("/{productId}/reject")
    public ResponseEntity<?> rejectProduct(@PathVariable Long productId, @RequestBody Map<String, String> request) {
        try {
            String rejectionReason = request.get("rejectionReason");
            Product rejectedProduct = productService.rejectProduct(productId, rejectionReason);
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Product rejected successfully");
            response.put("product", rejectedProduct);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("message", "Error rejecting product: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @GetMapping("/stats")
    public ResponseEntity<?> getProductStats() {
        try {
            Map<String, Object> stats = new HashMap<>();
            stats.put("pendingProducts", productService.getPendingProductsCount());
            stats.put("approvedProducts", productService.getApprovedProductsCount());
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("message", "Error fetching product stats: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
}