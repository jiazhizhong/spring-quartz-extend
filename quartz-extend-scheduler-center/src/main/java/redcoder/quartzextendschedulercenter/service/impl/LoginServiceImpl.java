package redcoder.quartzextendschedulercenter.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.DisabledAccountException;
import org.apache.shiro.authc.IncorrectCredentialsException;
import org.apache.shiro.authc.UnknownAccountException;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.springframework.stereotype.Service;
import redcoder.quartzextendschedulercenter.dto.login.LoginDTO;
import redcoder.quartzextendschedulercenter.service.LoginService;

/**
 * 基于shiro实现的登录
 *
 * @author redcoder54
 * @since 2022-01-11
 */
@Service
@Slf4j
public class LoginServiceImpl implements LoginService {

    @Override
    public boolean login(LoginDTO loginDTO) {
        String username = loginDTO.getUsername();
        String password = loginDTO.getPassword();
        try {
            // 登录
            SecurityUtils.getSubject().login(new UsernamePasswordToken(username, password));
            return true;
        } catch (UnknownAccountException | DisabledAccountException | IncorrectCredentialsException e) {
            log.warn("登录失败：{}", e.getMessage());
            return false;
        }
    }

    @Override
    public void logout() {
        SecurityUtils.getSubject().logout();
    }
}
