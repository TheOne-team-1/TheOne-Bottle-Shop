package one.theone.server.domain.freebie.repository;

import one.theone.server.domain.freebie.dto.response.FreebieGetResponse;
import one.theone.server.domain.freebie.dto.response.FreebiesGetResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface FreebieQueryRepository {

    Page<FreebiesGetResponse> findAllFreebies(Pageable pageable);

    FreebieGetResponse findFreebieById(Long id);
}
