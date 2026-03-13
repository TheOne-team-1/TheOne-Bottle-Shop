package one.theone.server.domain.search.corrector;

import kr.co.shineware.nlp.komoran.constant.DEFAULT_MODEL;
import kr.co.shineware.nlp.komoran.core.Komoran;
import kr.co.shineware.nlp.komoran.model.Token;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.List;
import java.util.Set;

@Component
public class KomoranCorrector {

    private final Komoran komoran;
    private static final Set<String> KEEP_POS = Set.of("NNG", "NNP", "VV", "VA");

    public KomoranCorrector() {
        this.komoran = new Komoran(DEFAULT_MODEL.FULL);
    }

    public List<String> extractMorphemes (String text) {
        if (!StringUtils.hasText(text)) return Collections.emptyList();

        List<String> morphemes = komoran.analyze(text)
                .getTokenList()
                .stream()
                .filter(token -> KEEP_POS.contains(token.getPos()))
                .map(Token::getMorph)
                .distinct()
                .toList();

        return morphemes.isEmpty() ? List.of(text) : morphemes;
    }
}
