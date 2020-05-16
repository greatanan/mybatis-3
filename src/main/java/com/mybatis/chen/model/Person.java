package com.mybatis.chen.model;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class Person {
  private int id;
  private String name;
  private int age;
  private String phone;
  private String email;
  private String address;


}
