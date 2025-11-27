package cn.edu.ccst.sims.utils;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.Map;

@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private Long expiration;

    // 生成 Token
    public String generateToken(Long userId, String username, Integer role) {
        return Jwts.builder()
                .claim("userId", userId)
                .claim("role", role)
                .subject(username)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expiration * 1000))
                .signWith(Keys.hmacShaKeyFor(secret.getBytes()))
                .compact();
    }

    // 解析 Token 获取 Payload（返回 Map）
    public Map<String, Object> getPayload(String token) {
        return Jwts.parser()
                .verifyWith(Keys.hmacShaKeyFor(secret.getBytes()))
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    // 获取用户ID
    public Long getUserId(String token) {
        Object userIdObj = getPayload(token).get("userId");
        if (userIdObj == null) {
            return null;
        }
        if (userIdObj instanceof Integer) {
            return ((Integer) userIdObj).longValue();
        } else if (userIdObj instanceof Long) {
            return (Long) userIdObj;
        } else {
            throw new IllegalArgumentException("userId claim 类型不是数字: " + userIdObj.getClass());
        }
    }

    // 获取用户名
    public String getUsername(String token) {
        return (String) getPayload(token).get("sub");
    }

    // 获取角色
    public Integer getRole(String token) {
        return (Integer) getPayload(token).get("role");
    }

    // Token 是否过期
    public boolean isExpired(String token) {
        Date exp = (Date) getPayload(token).get("exp");
        return exp != null && exp.before(new Date());
    }

    // 简单验证 Token（可用于拦截器）
    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(Keys.hmacShaKeyFor(secret.getBytes()))
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}