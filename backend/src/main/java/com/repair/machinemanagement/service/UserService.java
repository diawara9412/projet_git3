package com.repair.machinemanagement.service;

import com.repair.machinemanagement.dto.UserRequest;
import com.repair.machinemanagement.entity.User;
import com.repair.machinemanagement.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }
    
    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));
    }
    
    public List<User> getUsersByRole(User.Role role) {
        return userRepository.findByRole(role);
    }
    
    public User createUser(UserRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email déjà utilisé");
        }
        
        if (userRepository.existsByNumero(request.getNumero())) {
            throw new RuntimeException("Numéro déjà utilisé");
        }
        
        User user = User.builder()
                .nom(request.getNom())
                .prenom(request.getPrenom())
                .adresse(request.getAdresse())
                .numero(request.getNumero())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(request.getRole())
                .active(true)
                .build();
        
        return userRepository.save(user);
    }
    
    public User updateUser(Long id, UserRequest request) {
        User user = getUserById(id);
        
        user.setNom(request.getNom());
        user.setPrenom(request.getPrenom());
        user.setAdresse(request.getAdresse());
        user.setNumero(request.getNumero());
        user.setEmail(request.getEmail());
        
        if (request.getPassword() != null && !request.getPassword().isEmpty()) {
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }
        
        return userRepository.save(user);
    }
    
    public void deleteUser(Long id) {
        User user = getUserById(id);
        userRepository.delete(user);
    }
}
