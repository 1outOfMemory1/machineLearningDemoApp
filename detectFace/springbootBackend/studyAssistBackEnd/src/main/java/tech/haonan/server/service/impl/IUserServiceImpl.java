package tech.haonan.server.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import tech.haonan.server.entity.CommonResponse;
import tech.haonan.server.entity.User;
import tech.haonan.server.mapper.UserMapper;
import tech.haonan.server.service.IUserService;
import tech.haonan.server.util.JwtUtils;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

@Service
public class IUserServiceImpl extends ServiceImpl<UserMapper, User> implements IUserService  {
    @Value("${jwt.tokenHead}")
    private String tokenHead;

    @Value("${jwt.tokenHeader}")
    private String tokenHeader;

    @Autowired
    private UserDetailsService userDetailsService;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private JwtUtils jwtUtils;

    /**
     * 登录之后返回token
     * @param username 用户名
     * @param password 密码
     * @param verificationCode 验证码
     * @param request 请求
     * @return 返回token
     */
    @Override
    public CommonResponse login(String username, String password, String verificationCode, HttpServletRequest request) {

        String captcha = (String)request.getSession().getAttribute("captcha");
        // 这里需要判断captcha是否是null 因为怕之前没有请求过验证码接口 不过前端不会发生这种情况 因为 前端肯定先请求验证码
        if(verificationCode==null || verificationCode.equals("") || captcha==null || !captcha.equals(verificationCode)){
            return CommonResponse.error("验证码错误");
        }

        // 登录
        UserDetails userDetails = userDetailsService.loadUserByUsername(username);
        if(null == userDetails || !passwordEncoder.matches(password,userDetails.getPassword())){
            return CommonResponse.error("用户名或者密码不正确");
        }
        if(!userDetails.isEnabled()){
            return CommonResponse.error("账号被禁用,请联系管理员");
        }

        // 更新security登录用户对象
        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(userDetails,
                        null,userDetails.getAuthorities());

        // 生成token
        String token = jwtUtils.getToken(userDetails);
        Map<String,String> map = new HashMap<>();
        map.put("token",token);
        map.put("tokenHead",tokenHead);
        map.put("tokenHeader",tokenHeader);
        return CommonResponse.success("登录成功",map);
    }

    /**
     * 根据用户名获取用户信息
     * @return
     */
    @Override
    public User getAdminByUsername(String username) {
        return userMapper.selectOne(new QueryWrapper<User>().eq("username",username)
                .eq("enabled",true));
    }
}
