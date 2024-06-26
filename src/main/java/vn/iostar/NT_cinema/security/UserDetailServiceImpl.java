package vn.iostar.NT_cinema.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import vn.iostar.NT_cinema.entity.User;
import vn.iostar.NT_cinema.repository.UserRepository;

@Service
public class UserDetailServiceImpl implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = (User) userRepository.findByUserNameAndIsActiveIsTrue(username)
                    .orElseThrow(() -> new UsernameNotFoundException("Không tìm thấy người dùng."));
        return new UserDetail(user);
    }

    public UserDetails loadUserByUserId(String id) {
        User user = (User) userRepository.findByUserIdAndIsActiveIsTrue(id)
                .orElseThrow(() -> new UsernameNotFoundException("Không tìm thấy người dùng."));
        return new UserDetail(user);
    }
}
