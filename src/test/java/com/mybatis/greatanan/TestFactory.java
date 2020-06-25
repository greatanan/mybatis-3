/**
 *    Copyright 2009-2020 the original author or authors.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package com.mybatis.greatanan;

import com.mybatis.greatanan.dao.PersonDao;
import com.mybatis.greatanan.model.Person;
import com.mybatis.greatanan.util.SqlSessionFactoryUtil;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.junit.Test;

public class TestFactory {

  @Test
  public void insertTest() {

    SqlSessionFactory sqlSessionFactory = SqlSessionFactoryUtil.getSqlSessionFactory();
    //通过SqlSessionFactory获取SqlSession
    SqlSession sqlSession = sqlSessionFactory.openSession();

    PersonDao personDao = sqlSession.getMapper(PersonDao.class);

    Person p = new Person();
    p.setAddress("上海市").setAge(12).setEmail("ccc@163.com").setName("cccc").setPhone2("66666666666");

    personDao.insert(p);

    System.out.println(p.toString());
    sqlSession.commit();
    sqlSession.close();
  }

  @Test
  public void selectTest() {

    SqlSessionFactory sqlSessionFactory = SqlSessionFactoryUtil.getSqlSessionFactory();
    SqlSession sqlSession = sqlSessionFactory.openSession();

    PersonDao personDao = sqlSession.getMapper(PersonDao.class);
    Person person = personDao.select(5L);

//    Person person = sqlSession.selectOne("com.mybatis.chen.dao.PersonDao.select", 5L);
    System.out.println(person);
    Person person2 = personDao.select(5L);
    sqlSession.commit();
    sqlSession.close();
  }

}
