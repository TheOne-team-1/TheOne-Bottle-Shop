package one.theone.server.domain.point.service;

import lombok.RequiredArgsConstructor;
import one.theone.server.common.dto.PageResponse;
import one.theone.server.domain.point.dto.PointLogsGetRequest;
import one.theone.server.domain.point.dto.PointLogsGetResponse;
import one.theone.server.domain.point.repository.PointLogRepository;
import one.theone.server.domain.point.repository.PointRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PointService {

    private final PointRepository pointRepository;
    private final PointLogRepository pointLogRepository;

    @Transactional(readOnly = true)
    public PageResponse<PointLogsGetResponse> getPointLogs(Long memberId, PointLogsGetRequest request, Pageable pageable) {
        Page<PointLogsGetResponse> page = pointLogRepository.findPointLogs(memberId, request, pageable);
        return PageResponse.register(page);
    }
}
