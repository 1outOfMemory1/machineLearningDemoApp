package tech.haonan.server.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import tech.haonan.server.entity.CommonResponse;
import tech.haonan.server.entity.UserLoginParam;
import tech.haonan.server.service.IUserService;

import javax.servlet.http.HttpServletRequest;

@Api(tags = "登录接口")
@RestController
public class LoginController {


    @Autowired
    private IUserService userService;

    @ApiOperation(value = "登录获取token")
    @PostMapping("/login")
    public CommonResponse login(@RequestBody UserLoginParam userLoginParam, HttpServletRequest request){
        return userService.login(userLoginParam.getUsername(),userLoginParam.getPassword(),userLoginParam.getVerificationCode(),request);
    }

    @ApiOperation(value = "退出登录")
    @PostMapping("/logout")
    public CommonResponse logout(){
        return CommonResponse.success("注销成功");
    }


}


