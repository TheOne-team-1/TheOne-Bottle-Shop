package one.theone.server.domain.freebieCategory.service;

import lombok.RequiredArgsConstructor;
import one.theone.server.common.dto.PageResponse;
import one.theone.server.common.exception.ServiceErrorException;
import one.theone.server.common.exception.domain.FreebieCategoryExceptionEnum;
import one.theone.server.domain.freebieCategory.dto.request.FreebieCategoryCreateRequest;
import one.theone.server.domain.freebieCategory.dto.request.FreebieCategoryDetailCreateRequest;
import one.theone.server.domain.freebieCategory.dto.request.FreebieCategoryDetailUpdateRequest;
import one.theone.server.domain.freebieCategory.dto.request.FreebieCategoryUpdateRequest;
import one.theone.server.domain.freebieCategory.dto.response.*;
import one.theone.server.domain.freebieCategory.entity.FreebieCategory;
import one.theone.server.domain.freebieCategory.entity.FreebieCategoryDetail;
import one.theone.server.domain.freebieCategory.repository.FreebieCategoryDetailRepository;
import one.theone.server.domain.freebieCategory.repository.FreebieCategoryRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FreebieCategoryService {

    private final FreebieCategoryRepository freebieCategoryRepository;
    private final FreebieCategoryDetailRepository freebieCategoryDetailRepository;

    @Transactional
    public FreebieCategoryCreateResponse createFreebieCategory(FreebieCategoryCreateRequest request) {
        if (request.name() != null && freebieCategoryRepository.existsByName(request.name())) {
            throw new ServiceErrorException(FreebieCategoryExceptionEnum.ERR_DUPLICATE_FREEBIE_CATEGORY_NAME);
        }

        if (request.sortNum() != null && freebieCategoryRepository.existsBySortNum(request.sortNum())) {
            List<FreebieCategory> targets = freebieCategoryRepository.findAllBySortNumGreaterThanEqual(request.sortNum());
            for (FreebieCategory target : targets) {
                target.updateSortNum(target.getSortNum() + 1);
            }
        }

        FreebieCategory freebieCategory = FreebieCategory.register(request.name(), request.sortNum());
        freebieCategoryRepository.save(freebieCategory);
        return FreebieCategoryCreateResponse.from(freebieCategory);
    }

    @Transactional
    public FreebieCategoryUpdateResponse updateFreebieCategory(Long id, FreebieCategoryUpdateRequest request) {
        FreebieCategory freebieCategory = freebieCategoryRepository.findById(id)
                .orElseThrow(() -> new ServiceErrorException(FreebieCategoryExceptionEnum.ERR_FREEBIE_CATEGORY_NOT_FOUND));

        if (request.name() != null && freebieCategoryRepository.existsByName(request.name())) {
            throw new ServiceErrorException(FreebieCategoryExceptionEnum.ERR_DUPLICATE_FREEBIE_CATEGORY_NAME);
        }

        if (request.sortNum() != null && freebieCategoryRepository.existsBySortNum(request.sortNum())) {
            List<FreebieCategory> targets = freebieCategoryRepository.findAllBySortNumGreaterThanEqual(request.sortNum());
            for (FreebieCategory target : targets) {
                target.updateSortNum(target.getSortNum() + 1);
            }
        }

        freebieCategory.update(request);
        return FreebieCategoryUpdateResponse.from(freebieCategory);
    }

    @Transactional
    public FreebieCategoryDeleteResponse deleteFreebieCategory(Long id) {
        FreebieCategory freebieCategory = freebieCategoryRepository.findById(id)
                .orElseThrow(() -> new ServiceErrorException(FreebieCategoryExceptionEnum.ERR_FREEBIE_CATEGORY_NOT_FOUND));

        if (freebieCategoryDetailRepository.existsByFreebieCategoryIdAndDeletedFalse(id)) {
            throw new ServiceErrorException(FreebieCategoryExceptionEnum.ERR_FREEBIE_CATEGORY_HAS_DETAILS);
        }

        freebieCategory.delete();
        return FreebieCategoryDeleteResponse.from(freebieCategory);
    }

    @Transactional
    public FreebieCategoryDetailCreateResponse createFreebieCategoryDetail(FreebieCategoryDetailCreateRequest request) {
        freebieCategoryRepository.findById(request.freebieCategoryId())
                .orElseThrow(() -> new ServiceErrorException(FreebieCategoryExceptionEnum.ERR_FREEBIE_CATEGORY_NOT_FOUND));

        if (freebieCategoryDetailRepository.existsByFreebieCategoryIdAndName(request.freebieCategoryId(), request.name())) {
            throw new ServiceErrorException(FreebieCategoryExceptionEnum.ERR_DUPLICATE_FREEBIE_CATEGORY_DETAIL_NAME);
        }

        if (request.sortNum() != null && freebieCategoryDetailRepository.existsByFreebieCategoryIdAndSortNum(request.freebieCategoryId(), request.sortNum())) {
            List<FreebieCategoryDetail> targets = freebieCategoryDetailRepository.findAllByFreebieCategoryIdAndSortNumGreaterThanEqual(request.freebieCategoryId(), request.sortNum());
            for (FreebieCategoryDetail target : targets) {
                target.updateSortNum(target.getSortNum() + 1);
            }
        }

        FreebieCategoryDetail detail = FreebieCategoryDetail.register(request.freebieCategoryId(), request.name(), request.sortNum());
        freebieCategoryDetailRepository.save(detail);
        return FreebieCategoryDetailCreateResponse.from(detail);
    }

    @Transactional
    public FreebieCategoryDetailUpdateResponse updateFreebieCategoryDetail(Long id, FreebieCategoryDetailUpdateRequest request) {
        FreebieCategoryDetail detail = freebieCategoryDetailRepository.findById(id)
                .orElseThrow(() -> new ServiceErrorException(FreebieCategoryExceptionEnum.ERR_FREEBIE_CATEGORY_DETAIL_NOT_FOUND));

        Long targetCategoryId = request.freebieCategoryId() != null ? request.freebieCategoryId() : detail.getFreebieCategoryId();

        if (request.freebieCategoryId() != null && !freebieCategoryRepository.existsById(request.freebieCategoryId())) {
            throw new ServiceErrorException(FreebieCategoryExceptionEnum.ERR_FREEBIE_CATEGORY_NOT_FOUND);
        }

        if (request.name() != null && freebieCategoryDetailRepository.existsByFreebieCategoryIdAndName(targetCategoryId, request.name())) {
            throw new ServiceErrorException(FreebieCategoryExceptionEnum.ERR_DUPLICATE_FREEBIE_CATEGORY_DETAIL_NAME);
        }

        if (request.sortNum() != null && freebieCategoryDetailRepository.existsByFreebieCategoryIdAndSortNum(targetCategoryId, request.sortNum())) {
            List<FreebieCategoryDetail> targets = freebieCategoryDetailRepository.findAllByFreebieCategoryIdAndSortNumGreaterThanEqual(targetCategoryId, request.sortNum());
            for (FreebieCategoryDetail target : targets) {
                target.updateSortNum(target.getSortNum() + 1);
            }
        }

        detail.update(request);
        return FreebieCategoryDetailUpdateResponse.from(detail);
    }

    @Transactional
    public FreebieCategoryDetailDeleteResponse deleteFreebieCategoryDetail(Long id) {
        FreebieCategoryDetail detail = freebieCategoryDetailRepository.findById(id)
                .orElseThrow(() -> new ServiceErrorException(FreebieCategoryExceptionEnum.ERR_FREEBIE_CATEGORY_DETAIL_NOT_FOUND));

        detail.delete();
        return FreebieCategoryDetailDeleteResponse.from(detail);
    }

    @Transactional(readOnly = true)
    public PageResponse<FreebieCategoriesGetResponse> getFreebieCategories(Pageable pageable) {
        Page<FreebieCategoriesGetResponse> page = freebieCategoryRepository.findAllFreebieCategories(pageable);
        return PageResponse.register(page);
    }
}
