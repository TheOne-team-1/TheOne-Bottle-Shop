package one.theone.server.domain.search.corrector;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;

@Component
public class EngToKorCorrector {

    private static final Map<Character, Character> ENG_TO_KOR = Map.ofEntries(
            Map.entry('r', 'ㄱ'), Map.entry('R', 'ㄲ'), Map.entry('s', 'ㄴ'),
            Map.entry('e', 'ㄷ'), Map.entry('E', 'ㄸ'), Map.entry('f', 'ㄹ'),
            Map.entry('a', 'ㅁ'), Map.entry('q', 'ㅂ'), Map.entry('t', 'ㅅ'),
            Map.entry('T', 'ㅆ'), Map.entry('d', 'ㅇ'), Map.entry('w', 'ㅈ'),
            Map.entry('W', 'ㅉ'), Map.entry('c', 'ㅊ'), Map.entry('z', 'ㅋ'),
            Map.entry('x', 'ㅌ'), Map.entry('v', 'ㅍ'), Map.entry('g', 'ㅎ'),
            Map.entry('k', 'ㅏ'), Map.entry('i', 'ㅑ'), Map.entry('j', 'ㅓ'),
            Map.entry('u', 'ㅕ'), Map.entry('h', 'ㅗ'), Map.entry('y', 'ㅛ'),
            Map.entry('n', 'ㅜ'), Map.entry('b', 'ㅠ'), Map.entry('m', 'ㅡ'),
            Map.entry('l', 'ㅣ'), Map.entry('o', 'ㅐ'), Map.entry('p', 'ㅔ'),
            Map.entry('O', 'ㅒ'), Map.entry('P', 'ㅖ')
    );

    // 초성
    private static final char[] CHOSUNG = {
            'ㄱ','ㄲ','ㄴ','ㄷ','ㄸ','ㄹ','ㅁ','ㅂ','ㅅ',
            'ㅆ','ㅇ','ㅈ','ㅉ','ㅊ','ㅋ','ㅌ','ㅍ','ㅎ'
    };
    // 중성
    private static final char[] JUNGSUNG = {
            'ㅏ','ㅐ','ㅑ','ㅒ','ㅓ','ㅔ','ㅕ','ㅖ','ㅗ',
            'ㅘ','ㅙ','ㅚ','ㅛ','ㅜ','ㅝ','ㅞ','ㅟ','ㅠ','ㅡ','ㅢ','ㅣ'
    };
    // 종성
    private static final char[] JONGSUNG = {
            ' ','ㄱ','ㄲ','ㄳ','ㄴ','ㄵ','ㄶ','ㄷ','ㄹ',
            'ㄺ','ㄻ','ㄼ','ㄽ','ㄾ','ㄿ','ㅀ','ㅁ','ㅂ',
            'ㅄ','ㅅ','ㅆ','ㅇ','ㅈ','ㅊ','ㅋ','ㅌ','ㅍ','ㅎ'
    };

    public Optional<String> correct(String input) {
        // 공백 포함 영문 허용 ✅
        if (!input.chars().allMatch(c -> (c >= 'a' && c <= 'z')
                || (c >= 'A' && c <= 'Z')
                || c == ' ')) {
            return Optional.empty();
        }

        StringBuilder jamos = new StringBuilder();
        for (char c : input.toCharArray()) {
            // 공백은 그대로 유지
            if (c == ' ') {
                jamos.append(' ');
                continue;
            }
            jamos.append(ENG_TO_KOR.getOrDefault(c, c));
        }

        // 자모 → 완성형 한글 조합
        String composed = compose(jamos.toString());
        return composed.equals(input) ? Optional.empty() : Optional.of(composed);
    }

    private String compose(String jamos) {
        StringBuilder result = new StringBuilder();
        int i = 0;

        while (i < jamos.length()) {
            char c = jamos.charAt(i);
            int choIdx = indexOf(CHOSUNG, c);

            if (choIdx >= 0 && i + 1 < jamos.length()) {
                int jungIdx = indexOf(JUNGSUNG, jamos.charAt(i + 1));
                if (jungIdx >= 0) {
                    int jongIdx = 0;
                    if (i + 2 < jamos.length()) {
                        int nextJong = indexOf(JONGSUNG, jamos.charAt(i + 2));
                        boolean nextIsChosung = (i + 3 < jamos.length())
                                && indexOf(JUNGSUNG, jamos.charAt(i + 3)) >= 0;
                        if (nextJong > 0 && !nextIsChosung) {
                            jongIdx = nextJong;
                            i++;
                        }
                    }
                    result.append((char)(0xAC00 + choIdx * 21 * 28 + jungIdx * 28 + jongIdx));
                    i += 2;
                    continue;
                }
            }
            result.append(c);
            i++;
        }
        return result.toString();
    }

    private int indexOf(char[] arr, char target) {
        for (int i = 0; i < arr.length; i++) {
            if (arr[i] == target) return i;
        }
        return -1;
    }
}
