package com.pranchal.ecommerce.service;

import com.pranchal.ecommerce.entity.Product;
import com.pranchal.ecommerce.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

@Service
public class ProductService
{

    @Autowired
    ProductRepository productRepository;

    public boolean productExistsByName(String productName)
    {
        return productRepository.existsByProductName(productName);
    }

    public void createProduct(Product product)
    {
         productRepository.save(product);
    }

    public Product updateProduct(Long id, Product product)
    {

        Optional<Product> existingProduct = productRepository.findById(id);

        if(existingProduct.isPresent())
        {
            Product updatedProduct = existingProduct.get();

            updatedProduct.setProductName(product.getProductName());
            updatedProduct.setProductDescription(product.getProductDescription());
            updatedProduct.setProductPrice(product.getProductPrice());
            updatedProduct.setProductQuantity(product.getProductQuantity());

            return productRepository.save(updatedProduct);
        }
        return null;
    }

    public void deleteProduct(Long id)
    {
        productRepository.deleteById(id);
    }

    public List<Product> getAllProducts()
    {
        return productRepository.findAll();
    }

    public Product getProductById(Long id)
    {
        return productRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Product with id " + id + " not found"));
    }
    public Product getProductByName(String productName)
    {
        return productRepository.findByProductName(productName)
                .orElseThrow(() -> new NoSuchElementException("Product with name " + productName + " not found"));
    }
}

