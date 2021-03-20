package tech.haonan.server.filter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;
import tech.haonan.server.util.JwtUtils;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class JwtAuthorizationTokenFilter extends OncePerRequestFilter {

    @Value("${jwt.tokenHeader}")
    private String tokenHeader;
    @Value("${jwt.tokenHead}")
    private String tokenHead;

    @Autowired
    private UserDetailsService userDetailsService;

    @Autowired
    private JwtUtils jwtUtils;


    @Override
    protected void doFilterInternal(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, FilterChain filterChain) throws ServletException, IOException {


        String authHeader = httpServletRequest.getHeader(tokenHeader);
        // 如果没有叫做 tokenHeader(变量) 的键值对 那么说明没登录过
        if (null != authHeader && authHeader.startsWith(tokenHead) ){
            String authToken = authHeader.substring(tokenHead.length());
            String username = jwtUtils.getUsernameFromToken(authToken);
            // username只要不为空 那么 验证通过 (如果token过期或者伪造token 都不可以通过下边的if 也就是获取信息)

            // 理论上不可能出现token存在但是未登录的情况 因为只要token满足规则(未过期或者非伪造) username 写了就写了吧
            // 上边说错了  因为可能出现 服务器重启之后 的情况 还有分布式服务器的情况
            // token 存在 但是未登录
            if (username != null && null == SecurityContextHolder.getContext().getAuthentication()){
                // 登录
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                UsernamePasswordAuthenticationToken authenticationToken =
                        new UsernamePasswordAuthenticationToken(userDetails,null,userDetails.getAuthorities());
                authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(httpServletRequest));
                SecurityContextHolder.getContext().setAuthentication(authenticationToken);
            }

        }
        filterChain.doFilter(httpServletRequest,httpServletResponse);
    }
}
