package com.pranchal.ecommerce.controller;

import com.pranchal.ecommerce.entity.Product;
import com.pranchal.ecommerce.service.ProductService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import jakarta.servlet.http.HttpSession;
import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ProductControllerTest
{

    @Mock
    private ProductService productService;

    @Mock
    private HttpSession session;

    @InjectMocks
    private ProductController productController;

    @BeforeEach
    void setUp()
    {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void createProduct_Success()
    {
        Product product = new Product();
        product.setProductName("Test Product");
        product.setProductDescription("Test Description");
        product.setProductPrice(99.99);
        product.setProductQuantity(10);

        when(session.isNew()).thenReturn(false);
        when(session.getAttribute("role")).thenReturn("ADMIN");
        when(productService.productExistsByName(anyString())).thenReturn(false);

        ResponseEntity<String> response = productController.createProduct(product, session);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals("Product created successfully.", response.getBody());
    }

    @Test
    void createProduct_Unauthorized()
    {
        Product product = new Product();
        when(session.isNew()).thenReturn(true);

        ResponseEntity<String> response = productController.createProduct(product, session);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals("Access Denied: Please login to proceed.", response.getBody());
    }

    @Test
    void getAllProducts_Success()
    {
        Product product1 = new Product();
        product1.setProductName("Product 1");
        Product product2 = new Product();
        product2.setProductName("Product 2");
        List<Product> products = Arrays.asList(product1, product2);

        when(productService.getAllProducts()).thenReturn(products);

        ResponseEntity<?> response = productController.getAllProducts();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(products, response.getBody());
    }

    @Test
    void getProductById_NotFound()
    {
        Long id = 1L;
        when(productService.getProductById(id)).thenThrow(new NoSuchElementException("Product not found"));

        ResponseEntity<?> response = productController.getProductById(id);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void deleteProduct_Forbidden()
    {
        Long id = 1L;
        when(session.isNew()).thenReturn(false);
        when(session.getAttribute("role")).thenReturn("USER");

        ResponseEntity<String> response = productController.deleteProduct(id, session);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertEquals("Access Denied: Only Admin can perform this operation.", response.getBody());
    }
}