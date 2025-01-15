package com.pranchal.ecommerce.service;

import com.pranchal.ecommerce.entity.Product;
import com.pranchal.ecommerce.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductService productService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this); // Initialize mocks
    }

    @Test
    void testProductExistsByName() {
        when(productRepository.existsByProductName("Test Product")).thenReturn(true);

        boolean exists = productService.productExistsByName("Test Product");
        assertTrue(exists);

        verify(productRepository, times(1)).existsByProductName("Test Product");
    }

    @Test
    void testCreateProduct() {
        Product product = new Product();
        product.setProductName("New Product");

        productService.createProduct(product);

        verify(productRepository, times(1)).save(product);
    }

    @Test
    void testUpdateProduct_Success() {
        Product existingProduct = new Product();
        existingProduct.setId(1L);
        existingProduct.setProductName("Old Product");

        Product updatedDetails = new Product();
        updatedDetails.setProductName("Updated Product");
        updatedDetails.setProductDescription("Updated Description");
        updatedDetails.setProductPrice(200.0);
        updatedDetails.setProductQuantity(50);

        when(productRepository.findById(1L)).thenReturn(Optional.of(existingProduct));
        when(productRepository.save(any(Product.class))).thenReturn(existingProduct);

        Product updatedProduct = productService.updateProduct(1L, updatedDetails);

        assertNotNull(updatedProduct);
        assertEquals("Updated Product", updatedProduct.getProductName());
        assertEquals("Updated Description", updatedProduct.getProductDescription());
        assertEquals(200.0, updatedProduct.getProductPrice());
        assertEquals(50, updatedProduct.getProductQuantity());

        verify(productRepository, times(1)).findById(1L);
        verify(productRepository, times(1)).save(existingProduct);
    }

    @Test
    void testUpdateProduct_NotFound() {
        Product updatedDetails = new Product();
        updatedDetails.setProductName("Updated Product");

        when(productRepository.findById(1L)).thenReturn(Optional.empty());

        Product result = productService.updateProduct(1L, updatedDetails);

        assertNull(result);
        verify(productRepository, times(1)).findById(1L);
        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    void testDeleteProduct() {
        productService.deleteProduct(1L);
        verify(productRepository, times(1)).deleteById(1L);
    }

    @Test
    void testGetAllProducts() {
        Product product1 = new Product();
        product1.setProductName("Product 1");

        Product product2 = new Product();
        product2.setProductName("Product 2");

        List<Product> productList = Arrays.asList(product1, product2);

        when(productRepository.findAll()).thenReturn(productList);

        List<Product> result = productService.getAllProducts();

        assertEquals(2, result.size());
        assertEquals("Product 1", result.get(0).getProductName());
        assertEquals("Product 2", result.get(1).getProductName());

        verify(productRepository, times(1)).findAll();
    }

    @Test
    void testGetProductById_Success() {
        Product product = new Product();
        product.setId(1L);
        product.setProductName("Test Product");

        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        Product result = productService.getProductById(1L);

        assertNotNull(result);
        assertEquals("Test Product", result.getProductName());

        verify(productRepository, times(1)).findById(1L);
    }

    @Test
    void testGetProductById_NotFound() {
        when(productRepository.findById(1L)).thenReturn(Optional.empty());

        NoSuchElementException exception = assertThrows(NoSuchElementException.class, () -> productService.getProductById(1L));
        assertEquals("Product with id 1 not found", exception.getMessage());

        verify(productRepository, times(1)).findById(1L);
    }

    @Test
    void testGetProductByName_Success() {
        Product product = new Product();
        product.setProductName("Test Product");

        when(productRepository.findByProductName("Test Product")).thenReturn(Optional.of(product));

        Product result = productService.getProductByName("Test Product");

        assertNotNull(result);
        assertEquals("Test Product", result.getProductName());

        verify(productRepository, times(1)).findByProductName("Test Product");
    }

    @Test
    void testGetProductByName_NotFound() {
        when(productRepository.findByProductName("Unknown Product")).thenReturn(Optional.empty());

        NoSuchElementException exception = assertThrows(NoSuchElementException.class, () -> productService.getProductByName("Unknown Product"));
        assertEquals("Product with name Unknown Product not found", exception.getMessage());

        verify(productRepository, times(1)).findByProductName("Unknown Product");
    }
}
