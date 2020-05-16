package com.mybatis.greatanan.dao;

import com.mybatis.greatanan.model.Person;
import org.apache.ibatis.annotations.Param;

public interface PersonDao {

  int insert(Person p);

  Person select (@Param("id")Long id);

}
