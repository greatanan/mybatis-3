package com.mybatis.greatanan.model;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class Person {
  private int id;
  private String name;
  private int age;
  private String phone2;
  private String email;
  private String address;


}
