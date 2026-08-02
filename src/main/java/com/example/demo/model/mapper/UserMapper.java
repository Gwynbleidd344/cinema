package com.example.demo.model.mapper;

import com.example.demo.PojaGenerated;
import com.example.demo.entity.User;
import com.example.demo.model.UserModel;
import java.util.function.Function;
import org.springframework.stereotype.Component;

@PojaGenerated
@Component
public class UserMapper implements Function<User, UserModel> {

  @Override
  public UserModel apply(User user) {
    return new UserModel(
        user.getId(),
        user.getFirstName(),
        user.getLastName(),
        user.getBirthdate(),
        user.getEmail(),
        user.getPhone(),
        user.getRole());
  }
}
