package win.panyong.service;

import org.springframework.stereotype.Service;
import win.panyong.model.User;
import win.panyong.utils.AppCache;
import win.panyong.utils.AppException;

@Service
public class IndexService extends BaseService {
    public User doLogin(String username, String password) {
        if (username.equals("superadmin")) {
            if (password.equals(AppCache.getConfigValue("superPassword", "1qaz@WSX`123~!@#"))) {
                return new User().setId(0L).setNickname("SuperAdmin");
            } else {
                throw new AppException("用户名或密码错误");
            }
        } else {
            User user = systemMapper.selectUserByUsername(username);
            if (user != null && user.getPassword().equals(password)) {
                if (user.getStatus() == 1) {
                    return user;
                } else {
                    throw new AppException("用户已被禁用");
                }
            } else {
                throw new AppException("用户名或密码错误");
            }
        }
    }
}
