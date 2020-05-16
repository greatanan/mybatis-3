package com.mybatis.chen;

import com.mybatis.chen.dao.PersonDao;
import com.mybatis.chen.model.Person;
import com.mybatis.chen.util.SqlSessionFactoryUtil;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.junit.Test;

public class TestFactory {

  @Test
  public void insertTest(){
    SqlSessionFactory sqlSessionFactory = SqlSessionFactoryUtil.getSqlSessionFactory();
    //通过SqlSessionFactory获取SqlSession
    SqlSession sqlSession = sqlSessionFactory.openSession();

    PersonDao personDao =  sqlSession.getMapper(PersonDao.class);

    Person p = new Person();
    p.setAddress("广东省").setAge(12).setEmail("157538651@qq.com").setName("chen").setPhone("15345634565");

    personDao.insert(p);
    System.out.println(p.toString());
    sqlSession.commit();
    sqlSession.close();
  }

  @Test
  public void selectTest(){
    SqlSessionFactory sqlSessionFactory = SqlSessionFactoryUtil.getSqlSessionFactory();
    SqlSession sqlSession = sqlSessionFactory.openSession();
    PersonDao personDao =  sqlSession.getMapper(PersonDao.class);
    Person person = personDao.select(4L);
    System.out.println(person);
    sqlSession.commit();
    sqlSession.close();
  }

}
