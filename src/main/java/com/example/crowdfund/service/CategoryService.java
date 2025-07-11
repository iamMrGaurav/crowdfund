package com.example.crowdfund.service;

import com.example.crowdfund.entity.Category;
import com.example.crowdfund.repository.CategoryRepository;
import com.example.crowdfund.GloablExceptionHandler.ResourceAlreadyExistsException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public Category findById(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceAlreadyExistsException("Category not found with id: " + id));
    }

    public List<Category> findAll() {
        return categoryRepository.findAll();
    }

    public List<Category> findActiveCategories() {
        return categoryRepository.findByActiveTrue();
    }


}