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

        BaseResponse<Void> errorResponse = BaseResponse.fail(
                AuthExceptionEnum.ERR_UNAUTHORIZED.getCode(),
                AuthExceptionEnum.ERR_UNAUTHORIZED.getMessage()
        );

        response.setContentType("application/json;charset=UTF-8");
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

        response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
    }
}
