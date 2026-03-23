package one.theone.server.domain.product.service;

import lombok.RequiredArgsConstructor;
import net.datafaker.Faker;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class DummyDateService {

    private final JdbcTemplate jdbcTemplate;

    @Transactional
    public void insertDummyProducts() {
        Faker faker = new Faker(Locale.forLanguageTag("ko"));
        String sql = "INSERT INTO products (name, price, status, abv, volume_ml, category_detail_id, quantity, deleted, created_at, updated_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        String[] types = {"와인", "위스키", "맥주", "소주", "막걸리"};
        String[] grades = {"프리미엄", "스페셜", "리미티드", "클래식", "골드"};
        int[] volumes = {375, 500, 750, 1000, 1750};

        List<Object[]> batchArgs = new ArrayList<>();

        for (int i = 1; i <= 50000; i++) {
            String name = types[faker.random().nextInt(5)] + " "
                    + grades[faker.random().nextInt(5)] + " "
                    + faker.beer().name();
            long price = faker.number().numberBetween(10000L, 1000000L);
            long quantity = faker.number().numberBetween(0L, 100L);
            String status = quantity == 0 ? "SOLD_OUT" : "SALES";
            double abv = faker.number().randomDouble(3, 1, 60);
            int volume = volumes[faker.random().nextInt(5)];
            long categoryDetailId = faker.number().numberBetween(1L, 11L);

            batchArgs.add(new Object[]{
                    name, price, status, abv, volume,
                    categoryDetailId, quantity, false,
                    LocalDateTime.now(), LocalDateTime.now()
            });

            if (i % 1000 == 0) {
                jdbcTemplate.batchUpdate(sql, batchArgs);
                batchArgs.clear();
            }
        }

        if (!batchArgs.isEmpty()) {
            jdbcTemplate.batchUpdate(sql, batchArgs);
        }
    }
}
