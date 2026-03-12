package one.theone.server.domain.category.controller;

import lombok.RequiredArgsConstructor;
import one.theone.server.domain.category.service.CategoryService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class CategoryController {

    private final CategoryService categoryService;
}
