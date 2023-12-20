package vn.iostar.NT_cinema.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import vn.iostar.NT_cinema.repository.ViewerRepository;

import java.util.Date;
import java.util.Optional;

@Service
public class ViewerService {
    @Autowired
    ViewerRepository viewerRepository;

}
