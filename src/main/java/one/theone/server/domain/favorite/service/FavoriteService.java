package one.theone.server.domain.favorite.service;

import lombok.RequiredArgsConstructor;
import one.theone.server.domain.favorite.repository.FavoriteRepository;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class FavoriteService {

    private final FavoriteRepository favoriteRepository;
}
