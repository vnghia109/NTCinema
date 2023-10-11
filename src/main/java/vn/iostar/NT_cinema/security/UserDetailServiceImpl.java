package vn.iostar.NT_cinema.security;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import vn.iostar.NT_cinema.entity.User;
import vn.iostar.NT_cinema.repository.UserRepository;

@Service
public class UserDetailServiceImpl implements UserDetailsService {


    UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return null;
    }

    public UserDetails loadUserByUserId(String id){
        User user = (User) userRepository.findByUserIdAndActiveIsTrue(id)
                .orElseThrow(()->new UsernameNotFoundException("user is not found"));
        return new UserDetail(user);
    }
}
