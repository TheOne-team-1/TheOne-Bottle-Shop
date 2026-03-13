package one.theone.server.domain.point.controller;

import lombok.RequiredArgsConstructor;
import one.theone.server.domain.point.service.PointService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/points")
public class PointController {

    private final PointService pointService;
}
