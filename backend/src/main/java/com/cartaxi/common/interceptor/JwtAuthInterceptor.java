package com.cartaxi.common.interceptor;

import com.cartaxi.common.annotation.LoginRequired;
import com.cartaxi.common.context.UserContext;
import com.cartaxi.common.exception.BusinessException;
import com.cartaxi.common.model.LoginUser;
import com.cartaxi.common.util.JwtTokenUtil;
import io.jsonwebtoken.Claims;
import java.util.Arrays;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class JwtAuthInterceptor implements HandlerInterceptor {

    private final JwtTokenUtil jwtTokenUtil;

    public JwtAuthInterceptor(JwtTokenUtil jwtTokenUtil) {
        this.jwtTokenUtil = jwtTokenUtil;
    }

    @Override
    public boolean preHandle(jakarta.servlet.http.HttpServletRequest request,
                             jakarta.servlet.http.HttpServletResponse response,
                             Object handler) {
        if (!(handler instanceof HandlerMethod handlerMethod)) {
            return true;
        }

        LoginRequired loginRequired = handlerMethod.getMethodAnnotation(LoginRequired.class);
        if (loginRequired == null) {
            loginRequired = handlerMethod.getBeanType().getAnnotation(LoginRequired.class);
        }
        if (loginRequired == null) {
            return true;
        }

        String authorization = request.getHeader("Authorization");
        if (!StringUtils.hasText(authorization) || !authorization.startsWith("Bearer ")) {
            throw new BusinessException(401, "未登录或Token缺失");
        }

        String token = authorization.substring(7);
        Claims claims;
        try {
            claims = jwtTokenUtil.parseToken(token);
        } catch (Exception exception) {
            throw new BusinessException(401, "Token 无效或已过期");
        }

        String role = claims.get("role", String.class);
        if (loginRequired.roles().length > 0 && Arrays.stream(loginRequired.roles()).noneMatch(role::equals)) {
            throw new BusinessException(403, "无权限访问该资源");
        }

        LoginUser loginUser = LoginUser.builder()
                .userId(claims.get("userId", Long.class))
                .username(claims.getSubject())
                .displayName(claims.get("displayName", String.class))
                .role(role)
                .build();
        UserContext.set(loginUser);
        return true;
    }

    @Override
    public void afterCompletion(jakarta.servlet.http.HttpServletRequest request,
                                jakarta.servlet.http.HttpServletResponse response,
                                Object handler,
                                Exception ex) {
        UserContext.clear();
    }
}
