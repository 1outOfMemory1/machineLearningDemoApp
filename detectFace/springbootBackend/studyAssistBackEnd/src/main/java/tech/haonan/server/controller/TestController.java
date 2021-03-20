package tech.haonan.server.controller;

import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import tech.haonan.server.util.JwtUtils;

import javax.servlet.http.HttpServletRequest;

@Api(tags = "测试接口")
@RestController
@RequestMapping("test")
public class TestController {
    @Value("${jwt.tokenHeader}")
    private String tokenHeader;
    @Value("${jwt.tokenHead}")
    private String tokenHead;

    @Autowired
    private JwtUtils jwtUtils;



    @GetMapping("hello")
    public String  hello(HttpServletRequest request){
        String headerToken = request.getHeader(tokenHeader);
        String jwtToken = headerToken.substring(tokenHead.length());
        String username = jwtUtils.getUsernameFromToken(jwtToken);
        return "hello," + username ;
    }
}
