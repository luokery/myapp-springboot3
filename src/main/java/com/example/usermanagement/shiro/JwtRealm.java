package com.example.usermanagement.shiro;

import com.example.usermanagement.model.User;
import com.example.usermanagement.service.UserService;
import com.example.usermanagement.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.SimpleAuthenticationInfo;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

/**
 * JWT Realm
 * 用于 JWT Token 认证和授权
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtRealm extends AuthorizingRealm {

    private final JwtUtil jwtUtil;
    private final UserService userService;

    /**
     * 支持的 Token 类型
     */
    @Override
    public boolean supports(AuthenticationToken token) {
        return token instanceof JwtToken;
    }

    /**
     * 授权
     */
    @Override
    protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principals) {
        String token = (String) principals.getPrimaryPrincipal();
        String role = jwtUtil.getRoleFromToken(token);

        SimpleAuthorizationInfo authorizationInfo = new SimpleAuthorizationInfo();
        
        // 添加角色
        Set<String> roles = new HashSet<>();
        if (role != null) {
            roles.add(role);
        }
        authorizationInfo.setRoles(roles);

        // 添加权限（基于角色）
        Set<String> permissions = new HashSet<>();
        if ("admin".equals(role)) {
            permissions.add("user:create");
            permissions.add("user:read");
            permissions.add("user:update");
            permissions.add("user:delete");
            permissions.add("project:create");
            permissions.add("project:read");
            permissions.add("project:update");
            permissions.add("project:delete");
        } else if ("user".equals(role)) {
            permissions.add("user:read");
            permissions.add("project:read");
            permissions.add("project:create");
        }
        authorizationInfo.setStringPermissions(permissions);

        return authorizationInfo;
    }

    /**
     * 认证
     */
    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken token) throws AuthenticationException {
        String jwtToken = (String) token.getPrincipal();

        // 验证 Token
        if (!jwtUtil.validateToken(jwtToken)) {
            log.warn("JWT Token 验证失败");
            throw new AuthenticationException("JWT Token 验证失败");
        }

        // 获取用户名
        String username = jwtUtil.getUsernameFromToken(jwtToken);
        if (username == null) {
            log.warn("无法从 Token 获取用户名");
            throw new AuthenticationException("无效的 Token");
        }

        // 验证用户是否存在
        User user = userService.findByUsername(username);
        if (user == null) {
            log.warn("用户不存在: {}", username);
            throw new AuthenticationException("用户不存在");
        }

        // 检查用户状态
        if (user.getStatus() != 1) {
            log.warn("用户已被禁用: {}", username);
            throw new AuthenticationException("用户已被禁用");
        }

        return new SimpleAuthenticationInfo(jwtToken, jwtToken, getName());
    }
}
