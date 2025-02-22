package com.pranchal.ecommerce.controller;

import com.pranchal.ecommerce.entity.Product;
import com.pranchal.ecommerce.repository.ProductRepository;
import com.pranchal.ecommerce.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpSession;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

@RestController
@RequestMapping("/api/product")
@Validated
public class ProductController
{

    @Autowired
    ProductRepository productRepository;

    @Autowired
    private ProductService productService;

    private ResponseEntity<String> checkSession(HttpSession session)
    {
        if(session.isNew())
        {
            return new ResponseEntity<>("Access Denied: Please login to proceed.", HttpStatus.UNAUTHORIZED);
        }
        return null;
    }

    private ResponseEntity<String> checkAdminRole(HttpSession session)
    {
        Object roleObj = session.getAttribute("role");
        String role = roleObj.toString();
        if (role == null || !role.equalsIgnoreCase("ADMIN"))
        {
            return new ResponseEntity<>("Access Denied: Only Admin can perform this operation.", HttpStatus.FORBIDDEN);
        }
        return null;
    }
    private void validateProduct(Product product)
    {
        if(product.getProductName() == null || product.getProductDescription() == null ||
                product.getProductPrice() == null || product.getProductQuantity() == null)
        {
            throw new IllegalArgumentException("Invalid product data: All fields are required.");
        }

        if(product.getProductQuantity() < 0)
        {
            throw new IllegalArgumentException("Invalid product data: Quantity cannot be less than zero.");
        }

        if(product.getProductPrice() <= 0)
        {
            throw new IllegalArgumentException("Invalid product data: Price must be greater than zero.");
        }

        Optional<Product> existingProduct = productRepository.findByProductName(product.getProductName());
        if(existingProduct.isPresent())
        {
            throw new IllegalArgumentException("Product already exists: A product with the same name already exists.");
        }
    }

    @PostMapping("/create")
    public ResponseEntity<String> createProduct(@RequestBody Product product, HttpSession session)
    {
        ResponseEntity<String> sessionCheck = checkSession(session);
        if (sessionCheck != null) return sessionCheck;

        ResponseEntity<String> roleCheck = checkAdminRole(session);
        if (roleCheck != null) return roleCheck;

        validateProduct(product);      //here I am validating the product once
        productService.createProduct(product);
        return new ResponseEntity<>("Product created successfully.", HttpStatus.CREATED);
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<?> updateProduct(@PathVariable Long id, @RequestBody Product product, HttpSession session)
    {
        ResponseEntity<String> sessionCheck = checkSession(session);
        if(sessionCheck != null) return sessionCheck;

        ResponseEntity<String> roleCheck = checkAdminRole(session);
        if(roleCheck != null) return roleCheck;
        try
        {
            Product updatedProduct = productService.updateProduct(id, product);
            String updatedProductInfo = "Product updated: ID=" + updatedProduct.getId() +
                    ", Name=" + updatedProduct.getProductName() +
                    ", Price=" + updatedProduct.getProductPrice() +
                    ", Quantity=" + updatedProduct.getProductQuantity();
            return ResponseEntity.ok().body(updatedProductInfo);
        }
        catch(NoSuchElementException ex)
        {
            return ResponseEntity.status(404).body("Product not found with ID: " + id);
        }
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<String> deleteProduct(@PathVariable Long id, HttpSession session)
    {

        ResponseEntity<String> sessionCheck = checkSession(session);
        if(sessionCheck != null) return sessionCheck;

        ResponseEntity<String> roleCheck = checkAdminRole(session);
        if(roleCheck != null) return roleCheck;
        try
        {
            productService.deleteProduct(id);
            return ResponseEntity.ok("Product deleted successfully.");
        }
        catch (NoSuchElementException ex)
        {
            return ResponseEntity.status(404).body("Product not found with ID: " + id);
        }
    }

    @GetMapping("/getAllProduct")
    public ResponseEntity<?> getAllProducts()
    {
        List<Product> products = productService.getAllProducts();
        return ResponseEntity.ok(products);
    }

    @GetMapping("/getProductByID/{id}")
    public ResponseEntity<?> getProductById(@PathVariable Long id)
    {
        try
        {
            Product product = productService.getProductById(id);
            return ResponseEntity.ok(product);
        }
        catch (NoSuchElementException ex)
        {
            return ResponseEntity.status(404).body(ex.getMessage());
        }
    }

    @GetMapping("/getProductByName")
    public ResponseEntity<?> getProductByName(@RequestParam String productName)
    {
        try
        {
            Product product = productService.getProductByName(productName);
            return ResponseEntity.ok(product);
        }
        catch (NoSuchElementException ex)
        {
            return ResponseEntity.status(404).body(ex.getMessage());
        }
    }
}