package com.mybatis.greatanan.model;

import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;

@Data
@Accessors(chain = true)
public class Person implements Serializable {

  private int id;
  private String name;
  private int age;
  private String phone;
  private String email;
  private String address;

}
