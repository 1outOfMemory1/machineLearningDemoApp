package tech.haonan.server.service;

import com.baomidou.mybatisplus.extension.service.IService;
import tech.haonan.server.entity.CommonResponse;
import tech.haonan.server.entity.User;

import javax.servlet.http.HttpServletRequest;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author haonan
 * @since 2021-02-20
 */
public interface IUserService extends IService<User> {

    CommonResponse login(String username, String password, String verificationCode, HttpServletRequest request);

    User getAdminByUsername(String username);

}
