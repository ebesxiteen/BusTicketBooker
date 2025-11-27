package com.example.ticketbooker.DTO.Account;

import com.example.ticketbooker.Entity.Users;
import com.example.ticketbooker.Util.Enum.AccountStatus;
import com.example.ticketbooker.Util.Enum.Role;
import lombok.Data;

@Data
public class AddAccountDTO {
    private String username = "";
    private Integer userId;
    private String email = "";
    private Role role;
    private AccountStatus accountStatus;

    public AddAccountDTO() {
        this.username = "";
        this.userId = null;
        this.email = "";
        this.role = null;
        this.accountStatus = null;
    }

    public AddAccountDTO(String username, Integer userId, String email, Role role, AccountStatus accountStatus) {
        this.username = username;
        this.userId = userId;
        this.email = email;
        this.role = role;
        this.accountStatus = accountStatus;
    }
}
