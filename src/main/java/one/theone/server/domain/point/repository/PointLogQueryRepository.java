package one.theone.server.domain.point.repository;

import one.theone.server.domain.point.dto.PointLogsGetRequest;
import one.theone.server.domain.point.dto.PointLogsGetResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface PointLogQueryRepository {

    Page<PointLogsGetResponse> findPointLogs(Long memberId, PointLogsGetRequest request, Pageable pageable);
}
