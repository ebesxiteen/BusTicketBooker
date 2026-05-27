package com.example.ticketbooker.Repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.ticketbooker.Entity.Users;
import com.example.ticketbooker.Util.Enum.Gender;

@Repository
public interface UserRepo extends JpaRepository<Users, Integer> {
    
    // 1. Hàm quan trọng nhất cho đăng nhập
    Users findByEmail(String email);

    // 2. Các hàm tìm kiếm khác (đang được Service gọi)
    List<Users> findByFullNameLike(String name);
    
    List<Users> findAllByGender(Gender gender);
    
    List<Users> findAllByAddress(String address);
    
    List<Users> findAllById(int id);
}