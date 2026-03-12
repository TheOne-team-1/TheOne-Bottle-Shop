package one.theone.server.domain.category.service;

import lombok.RequiredArgsConstructor;
import one.theone.server.common.exception.ServiceErrorException;
import one.theone.server.common.exception.domain.CategoryExceptionEnum;
import one.theone.server.domain.category.dto.CategoryCreateRequest;
import one.theone.server.domain.category.dto.CategoryCreateResponse;
import one.theone.server.domain.category.dto.CategoryUpdateRequest;
import one.theone.server.domain.category.dto.CategoryUpdateResponse;
import one.theone.server.domain.category.entity.Category;
import one.theone.server.domain.category.repository.CategoryDetailRepository;
import one.theone.server.domain.category.repository.CategoryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final CategoryDetailRepository categoryDetailRepository;

    @Transactional
    public CategoryCreateResponse createCategory(CategoryCreateRequest request) {
        if (categoryRepository.existsByName(request.name())) {
            throw new ServiceErrorException(CategoryExceptionEnum.ERR_DUPLICATE_CATEGORY_NAME);
        }

        if (categoryRepository.existsBySortNum(request.sortNum())) {
            List<Category> targets = categoryRepository.findAllBySortNumGreaterThanEqual(request.sortNum());
            for (Category category : targets) {
                category.updateSortNum(category.getSortNum() + 1);
            }
        }

        Category category = Category.register(request.name(), request.sortNum());
        categoryRepository.save(category);
        return CategoryCreateResponse.from(category);
    }

    @Transactional
    public CategoryUpdateResponse updateCategory(Long id, CategoryUpdateRequest request) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ServiceErrorException(CategoryExceptionEnum.ERR_CATEGORY_NOT_FOUND));

        if (request.name() != null && categoryRepository.existsByName(request.name())) {
            throw new ServiceErrorException(CategoryExceptionEnum.ERR_DUPLICATE_CATEGORY_NAME);
        }

        if (request.sortNum() != null && categoryRepository.existsBySortNum(request.sortNum())) {
            List<Category> targets = categoryRepository.findAllBySortNumGreaterThanEqual(request.sortNum());
            for (Category target : targets) {
                target.updateSortNum(target.getSortNum() + 1);
            }
        }

        category.update(request);
        return CategoryUpdateResponse.from(category);
    }
}
