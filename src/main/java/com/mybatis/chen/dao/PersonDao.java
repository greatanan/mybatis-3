package com.mybatis.chen.dao;

import com.mybatis.chen.model.Person;
import org.apache.ibatis.annotations.Param;

public interface PersonDao {

  int insert(Person p);

  Person select (@Param("id")Long id);

}
