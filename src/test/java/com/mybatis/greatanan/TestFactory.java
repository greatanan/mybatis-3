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
    sqlSession.commit();
    sqlSession.close();
  }

}
