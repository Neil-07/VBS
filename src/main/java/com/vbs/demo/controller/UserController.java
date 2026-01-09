package com.vbs.demo.controller;

import com.vbs.demo.dto.DisplayDto;
import com.vbs.demo.dto.LoginDto;
import com.vbs.demo.dto.UpdateDto;
import com.vbs.demo.models.History;
import com.vbs.demo.models.Transaction;
import com.vbs.demo.models.User;
import com.vbs.demo.repositories.HistoryRepo;
import com.vbs.demo.repositories.TransactionRepo;
import com.vbs.demo.repositories.UserRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin(origins = "*")
public class UserController {
    @Autowired
    UserRepo userRepo;
    @Autowired
    HistoryRepo historyRepo;
    @Autowired
    TransactionRepo transactionRepo;
    @PostMapping("/register")
    public String register(@RequestBody User user)
    {
        userRepo.save(user);
        return "Signup successful";
    }

    @PostMapping("/login")
    public String login(@RequestBody LoginDto u)
    {
        User user = userRepo.findByUsername(u.getUsername());
        if(user == null)
        {
            return "User not found";
        }
        if(!u.getPassword().equals(user.getPassword()))
        {
            return "Password Incorrect";
        }
        if(!u.getRole().equals(user.getRole()))
        {
            return "Role Incorrect";
        }
        return String.valueOf(user.getId());
    }

    @GetMapping("/get-details/{id}")
    public DisplayDto display(@PathVariable int id)
    {
        User user = userRepo.findById(id)
                .orElseThrow(()->new RuntimeException("User not found"));
        DisplayDto displayDto = new DisplayDto();
        displayDto.setUsername(user.getUsername());
        displayDto.setBalance(user.getBalance());
        return displayDto;
    }

    @PostMapping("/update")
    public String update(@RequestBody UpdateDto obj)
    {
        History h1 = new History();
        User user = userRepo.findById(obj.getId())
                .orElseThrow(()-> new RuntimeException("Not found"));
        if(obj.getKey().equalsIgnoreCase("name"))
        {
            if(obj.getValue().equals(user.getName())) return "Cannot be same";
            user.setName(obj.getValue());
            h1.setDescription(user.getUsername()+" Changed their name to "+obj.getValue());
        }
        else if(obj.getKey().equalsIgnoreCase("password"))
        {
            if(obj.getValue().equals(user.getPassword())) return "Cannot be same";
            user.setPassword(obj.getValue());
            h1.setDescription(user.getUsername()+" Changed their password");
        }
        else if(obj.getKey().equalsIgnoreCase("email"))
        {
            if(obj.getValue().equals(user.getEmail())) return "Cannot be same";
            User user2 = userRepo.findByEmail(obj.getValue());
            if(user2 != null) return "Email already Exists";
            user.setEmail(obj.getValue());
            h1.setDescription(user.getUsername()+" Changed their email to "+obj.getValue());
        }
        else
        {
            return "Invalid Key";
        }
        userRepo.save(user);
        historyRepo.save(h1);
        return "Updated Successfully";
    }

    @PostMapping("/add/{adminId}")
    public String add(@RequestBody User user, @PathVariable int adminId)
    {
        User user3 = userRepo.findById(adminId)
                .orElseThrow(()-> new RuntimeException("User not found"));
        String role = user3.getRole();
        History h1 = new History();
        h1.setDescription(role+" "+user3.getName()+" Created user "+user.getName());
        userRepo.save(user);
        if(user.getBalance() > 0)
        {
            User user2 = userRepo.findByUsername(user.getUsername());
            Transaction t = new Transaction();
            t.setAmount(user.getBalance());
            t.setCurrBalance(user.getBalance());
            t.setDescription("Rs "+user.getBalance()+" Deposit Successful");
            t.setUserId(user2.getId());
            transactionRepo.save(t);
        }
        historyRepo.save(h1);
        return "Updated Successfully";
    }

    @DeleteMapping("delete-user/{userId}/admin/{adminId}")
    public String delete(@PathVariable int userId, @PathVariable int adminId)
    {
        User user = userRepo.findById(userId)
                .orElseThrow(()-> new RuntimeException("Not found"));
        User user2 = userRepo.findById(adminId)
                .orElseThrow(()-> new RuntimeException("Not found"));
        String role = user2.getRole();
        if(user.getBalance() > 0)
        {
            return "Balance should be zero";
        }
        History h1 = new History();
        h1.setDescription(role+" "+user2.getName()+" Deleted User "+user.getName());
        historyRepo.save(h1);
        userRepo.delete(user);
        return "User Deleted Successfully";
    }

    @GetMapping("/users")
    public List<User> getAllUsers(@RequestParam String sortBy, @RequestParam String order, @RequestParam int id)
    {
        User user1 = userRepo.findById(id)
                .orElseThrow(()-> new RuntimeException("Not found"));
        Sort sort;
        if(order.equalsIgnoreCase("desc"))
        {
            sort = Sort.by(sortBy).descending();
        }
        else
        {
            sort = Sort.by(sortBy).ascending();
        }
        if(user1.getRole().equalsIgnoreCase("Admin"))
        {
            return userRepo.findAllByRoleIn(List.of("customer"),sort);
        }
        if(user1.getRole().equalsIgnoreCase("Manager"))
        {
            return userRepo.findAllByRoleIn(List.of("customer", "admin"),sort);
        }
        return List.of();
    }

    @GetMapping("/users/{keyword}")
    public List<User> getUser(@PathVariable String keyword)
    {
        return userRepo.findByUsernameContainingIgnoreCaseAndRole(keyword, "customer");
    }
}
