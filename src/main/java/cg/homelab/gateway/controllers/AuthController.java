package cg.homelab.gateway.controllers;

import cg.homelab.gateway.models.Dto.SignUpRequest;
import cg.homelab.gateway.models.Entity.User;
import cg.homelab.gateway.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
public class AuthController {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;

    //Tạo tài khoản
    @PostMapping("/api/auth/signup")
    public Mono<ResponseEntity<?>> signUp(@RequestBody SignUpRequest req) {
        if (!req.getPassword().equals(req.getRepeatPassword())) {
            return Mono.just(ResponseEntity.badRequest().body("Mật khẩu không trùng nhau!"));
        }
        Mono<User> userMono = userRepository.findByUsername(req.getUsername());

        Mono<ResponseEntity<?>> handleIfExist = userMono.flatMap(user -> {
            return Mono.just(ResponseEntity.badRequest().body("Tên đăng nhập đã tồn tại!"));
        });

        Mono<ResponseEntity<?>> handleSave = handleIfExist.switchIfEmpty(Mono.defer(() -> {
            User newUser = new User();
            newUser.setUsername(req.getUsername());
            newUser.setPassword(passwordEncoder.encode(req.getPassword()));
            newUser.setRole(req.getRole());
            Mono<User> savedUser = userRepository.save(newUser);
            return savedUser.map(x -> ResponseEntity.ok().body("Tạo thành công tài khoản: " + x.getUsername()));
        }));


        return handleSave;
    }

}
