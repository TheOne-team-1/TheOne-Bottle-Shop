package one.theone.server.common.config.security;

import lombok.RequiredArgsConstructor;
import one.theone.server.common.exception.CustomAccessDeniedHandler;
import one.theone.server.common.exception.CustomAuthenticationEntryPoint;
import one.theone.server.common.exception.OAuth2SuccessHandler;
import one.theone.server.domain.auth.service.CustomOAuth2UserService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtProvider jwtProvider;
    private final CustomAccessDeniedHandler customAccessDeniedHandler;
    private final CustomAuthenticationEntryPoint customAuthenticationEntryPoint;

    private final CustomOAuth2UserService customOAuth2UserService;
    private final OAuth2SuccessHandler oAuth2SuccessHandler;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/signup", "/api/login", "/api/signup/admin","/login/oauth2/**", "/oauth2/redirect","/index.html", "/").permitAll()

                        //region 쿠폰 관련
                        .requestMatchers("/api/admin/coupons/**").hasRole("ADMIN")
                        .requestMatchers("/api/admin/member/*/coupons/*/recall").hasRole("ADMIN")
                        //endregion

                        //region 환불 관련
                        .requestMatchers("/api/admin/refunds").hasRole("ADMIN")
                        //endregion

                        //region 이벤트 관련
                        .requestMatchers("/api/admin/events/**").hasRole("ADMIN")
                        //endregion

                        //region 사은품 관련
                        .requestMatchers("/api/admin/freebies/**").hasRole("ADMIN")
                        //endregion

                        //region 사은품 카테고리 관련
                        .requestMatchers("/api/admin/freebie-categories/**").hasRole("ADMIN")
                        .requestMatchers("/api/admin/freebie-category-details/**").hasRole("ADMIN")
                        //endregion

                        //region 상품 관련
                        .requestMatchers("/api/products/**", "/api/best/products").permitAll()
                        .requestMatchers("/api/admin/products/**").hasRole("ADMIN")
                        .requestMatchers("/api/admin/dummy/products").hasRole("ADMIN")
                        //endregion

                        //region 상품 카테고리 관련
                        .requestMatchers("/api/admin/categories/**").hasRole("ADMIN")
                        .requestMatchers("/api/admin/category-details/**").hasRole("ADMIN")
                        //endregion

                        //region 포인트 관련
                        .requestMatchers("/api/admin/points/**").hasRole("ADMIN")
                        //endregion

                        //region 검색어 관련
                        .requestMatchers("/api/search/**", "/api/best/search").permitAll()
                        //endregion

                        .anyRequest().authenticated()
                )
                .oauth2Login(oauth2 -> oauth2
                        .userInfoEndpoint(userInfo -> userInfo.userService(customOAuth2UserService))
                        .successHandler(oAuth2SuccessHandler)
                )
                // 핸들러 등록
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint(customAuthenticationEntryPoint) // 401 처리
                        .accessDeniedHandler(customAccessDeniedHandler)           // 403 처리
                )
                // JWT 필터를 ID/PW 필터 앞에 배치하여 토큰이 있으면 먼저 인증되도록 함
                .addFilterBefore(new JwtAuthenticationFilter(jwtProvider), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}