package one.theone.server.domain.favorite.controller;

import lombok.RequiredArgsConstructor;
import one.theone.server.domain.favorite.service.FavoriteService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/favorites")
public class FavoriteController {

    private final FavoriteService favoriteService;
}
