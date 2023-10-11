package vn.iostar.NT_cinema.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import vn.iostar.NT_cinema.entity.Role;
import vn.iostar.NT_cinema.repository.RoleRepository;

@Service
public class RoleService {
    @Autowired
    RoleRepository roleRepository;

    public Role findByRoleName(String Name) {
        return roleRepository.findByRoleName(Name);
    }
}
