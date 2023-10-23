package vn.iostar.NT_cinema.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import vn.iostar.NT_cinema.dto.GenericResponse;
import vn.iostar.NT_cinema.dto.UserReq;
import vn.iostar.NT_cinema.entity.Manager;
import vn.iostar.NT_cinema.entity.Viewer;
import vn.iostar.NT_cinema.repository.ViewerRepository;

import java.util.Date;
import java.util.Optional;

@Service
public class ViewerService {
    @Autowired
    ViewerRepository viewerRepository;

}
