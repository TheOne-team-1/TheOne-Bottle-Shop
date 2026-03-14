package one.theone.server.domain.freebie.service;

import lombok.RequiredArgsConstructor;
import one.theone.server.common.annotation.RedisLock;
import one.theone.server.common.dto.PageResponse;
import one.theone.server.common.exception.ServiceErrorException;
import one.theone.server.common.exception.domain.FreebieCategoryExceptionEnum;
import one.theone.server.common.exception.domain.FreebieExceptionEnum;
import one.theone.server.domain.freebie.dto.request.FreebieCreateRequest;
import one.theone.server.domain.freebie.dto.request.FreebieUpdateRequest;
import one.theone.server.domain.freebie.dto.response.*;
import one.theone.server.domain.freebie.entity.Freebie;
import one.theone.server.domain.freebie.repository.FreebieRepository;
import one.theone.server.domain.freebieCategory.repository.FreebieCategoryDetailRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class FreebieService {

    private final FreebieRepository freebieRepository;
    private final FreebieCategoryDetailRepository freebieCategoryDetailRepository;

    @Transactional
    public FreebieCreateResponse createFreebie(FreebieCreateRequest request) {
        freebieCategoryDetailRepository.findById(request.freebieCategoryDetailId())
                .orElseThrow(() -> new ServiceErrorException(FreebieCategoryExceptionEnum.ERR_FREEBIE_CATEGORY_DETAIL_NOT_FOUND));

        Freebie freebie = Freebie.register(
                request.freebieCategoryDetailId(),
                request.name(),
                request.quantity()
        );
        freebieRepository.save(freebie);

        return new FreebieCreateResponse(
                freebie.getId(),
                freebie.getFreebieCategoryDetailId(),
                freebie.getName(),
                freebie.getQuantity(),
                freebie.getStatus()
        );
    }

    @Transactional(readOnly = true)
    public PageResponse<FreebiesGetResponse> getFreebies(Pageable pageable) {
        Page<FreebiesGetResponse> page = freebieRepository.findAllFreebies(pageable);
        return PageResponse.register(page);
    }

    @Transactional(readOnly = true)
    public FreebieGetResponse getFreebie(Long id) {
        FreebieGetResponse response = freebieRepository.findFreebieById(id);
        if (response == null) {
            throw new ServiceErrorException(FreebieExceptionEnum.ERR_FREEBIE_NOT_FOUND);
        }
        return response;
    }

    @Transactional
    public FreebieUpdateResponse updateFreebie(Long id, FreebieUpdateRequest request) {
        Freebie freebie = freebieRepository.findById(id)
                .orElseThrow(() -> new ServiceErrorException(FreebieExceptionEnum.ERR_FREEBIE_NOT_FOUND));

        if (request.freebieCategoryDetailId() != null) {
            freebieCategoryDetailRepository.findById(request.freebieCategoryDetailId())
                    .orElseThrow(() -> new ServiceErrorException(FreebieCategoryExceptionEnum.ERR_FREEBIE_CATEGORY_DETAIL_NOT_FOUND));
        }

        freebie.update(request);

        return new FreebieUpdateResponse(
                freebie.getId(),
                freebie.getFreebieCategoryDetailId(),
                freebie.getName(),
                freebie.getQuantity(),
                freebie.getStatus()
        );
    }

    @Transactional
    public FreebieDeleteResponse deleteFreebie(Long id) {
        Freebie freebie = freebieRepository.findById(id)
                .orElseThrow(() -> new ServiceErrorException(FreebieExceptionEnum.ERR_FREEBIE_NOT_FOUND));

        freebie.delete();

        return new FreebieDeleteResponse(
                freebie.getId(),
                freebie.getName(),
                freebie.getDeleted(),
                freebie.getDeletedAt()
        );
    }

    @Transactional
    public void decreaseStock(Long id, Long quantity) {
        Freebie freebie = freebieRepository.findById(id)
                .orElseThrow(() -> new ServiceErrorException(FreebieExceptionEnum.ERR_FREEBIE_NOT_FOUND));
        freebie.decreaseStock(quantity);
    }

    @Transactional
    public void increaseStock(Long id, Long quantity) {
        Freebie freebie = freebieRepository.findById(id)
                .orElseThrow(() -> new ServiceErrorException(FreebieExceptionEnum.ERR_FREEBIE_NOT_FOUND));
        freebie.increaseStock(quantity);
    }
}
