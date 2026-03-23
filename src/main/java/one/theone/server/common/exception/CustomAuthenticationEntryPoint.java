package one.theone.server.common.exception;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import one.theone.server.common.dto.BaseResponse;
import one.theone.server.common.exception.domain.AuthExceptionEnum;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;
import java.io.IOException;

@Component
@RequiredArgsConstructor
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException authException) throws IOException {

        AuthExceptionEnum exceptionEnum = (AuthExceptionEnum) request.getAttribute("exception");

        // 배달된 게 없으면 기본값(A001) 사용
        if (exceptionEnum == null) {
            exceptionEnum = AuthExceptionEnum.ERR_UNAUTHORIZED;
        }

        // 고정값이 아닌 변수(exceptionEnum)를 사용해서 응답 생성
        BaseResponse<Void> errorResponse = BaseResponse.fail(
                exceptionEnum.getCode(),
                exceptionEnum.getMessage()
        );

        response.setContentType("application/json;charset=UTF-8");
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

        response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
    }
}
