package one.theone.server.domain.event.controller;

import lombok.RequiredArgsConstructor;
import one.theone.server.domain.event.service.EventService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class EventController {

    private final EventService eventService;
}
