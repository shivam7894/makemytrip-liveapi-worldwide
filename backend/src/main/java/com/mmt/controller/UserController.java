package com.mmt.controller;
import com.mmt.model.User; import com.mmt.repository.UserRepository; import org.springframework.web.bind.annotation.*; import java.util.Map;
@RestController @RequestMapping("/api/users") @CrossOrigin(origins="*") public class UserController { private final UserRepository repo; public UserController(UserRepository r){repo=r;}
@GetMapping("/{id}/preferences") public User get(@PathVariable Long id){return repo.findById(id).orElseThrow();}
@PutMapping("/{id}/preferences") public User save(@PathVariable Long id,@RequestBody Map<String,String> b){User u=repo.findById(id).orElseThrow(); if(b.containsKey("preferredSeat"))u.setPreferredSeat(b.get("preferredSeat")); if(b.containsKey("preferredRoomType"))u.setPreferredRoomType(b.get("preferredRoomType")); return repo.save(u);}
}
