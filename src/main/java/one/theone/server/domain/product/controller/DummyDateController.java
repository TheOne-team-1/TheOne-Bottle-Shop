package one.theone.server.domain.product.controller;

import lombok.RequiredArgsConstructor;
import one.theone.server.common.dto.BaseResponse;
import one.theone.server.domain.product.service.DummyDateService;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
@Profile("dev")
public class DummyDateController {

    private final DummyDateService dummyDataService;

    @PostMapping("/admin/dummy/products")
    public ResponseEntity<BaseResponse<Void>> insertDummyProducts() {
        dummyDataService.insertDummyProducts();
        return ResponseEntity.ok(BaseResponse.success(
                HttpStatus.OK.name(),
                "더미데이터 적재 성공",
                null
        ));
    }
}
