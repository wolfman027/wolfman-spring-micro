package discovery.eureka.user.service.service;

import discovery.eureka.user.api.domain.User;
import discovery.eureka.user.api.service.UserService;
import discovery.eureka.user.service.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;

/**
 * {@link UserService 用户服务} 提供者实现
 *
 * @author 小马哥 QQ 1191971402
 * @copyright 咕泡学院出品
 * @since 2017/10/28
 */
@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository userRepository;

    @Override
    public boolean save(User user) {
        System.out.println("保存了一个用户");
        return userRepository.save(user);
    }

    @Override
    public Collection<User> findAll() {
        System.out.println("查询所有用户");
        return userRepository.findAll();
    }
}
