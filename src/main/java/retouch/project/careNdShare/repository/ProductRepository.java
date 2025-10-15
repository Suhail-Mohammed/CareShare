package retouch.project.careNdShare.repository;

import retouch.project.careNdShare.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import retouch.project.careNdShare.entity.ProductStatus;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    List<Product> findByUserId(Long userId);
    List<Product> findByStatus(ProductStatus status);
    List<Product> findByUserIdAndStatus(Long userId, ProductStatus status);

    @Query("SELECT COUNT(p) FROM Product p WHERE p.status = 'PENDING'")
    long countPendingProducts();

    @Query("SELECT COUNT(p) FROM Product p WHERE p.status = 'APPROVED'")
    long countApprovedProducts();
}