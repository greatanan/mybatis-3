/**
 * Copyright 2009-2020 the original author or authors.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
    p.setAddress("上海市").setAge(12).setEmail("ccc@163.com").setName("cccc").setPhone("66666666666");

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
        // 代理对象调用接口中的任意方法 执行的都会动态代理中的invoke方法  MapperProxy中的invoke方法
        Person person = personDao.select(1L);

    //    Person person = sqlSession.selectOne("com.mybatis.chen.dao.PersonDao.select", 5L);
        System.out.println(person);
        //Person person2 = personDao.select(1L);
        sqlSession.commit();
        sqlSession.close();
  }

  @Test
  public void selectTest1() {

        SqlSessionFactory sqlSessionFactory = SqlSessionFactoryUtil.getSqlSessionFactory();
        SqlSession sqlSession = sqlSessionFactory.openSession();

        Person person = sqlSession.selectOne("com.mybatis.greatanan.dao.select", 5L);
        System.out.println(person);
        sqlSession.commit();
        sqlSession.close();
  }



  /**
   * 测试一级缓存的存在 一级缓存是默认开启的
   */
  @Test
  public void testFirstCache() {

        SqlSessionFactory sqlSessionFactory = SqlSessionFactoryUtil.getSqlSessionFactory();
        SqlSession sqlSession1 = sqlSessionFactory.openSession();
        PersonDao personDao1 = sqlSession1.getMapper(PersonDao.class);
        Person person1 = personDao1.select(1L);
        // Person person2 = personDao1.select(1L);
        // System.out.println(person1==person2);

  }

  /**
   * 测试二级缓存的存在 Person必须实现序列化接口
   * 二级缓存需要手动开启同时对应的pojo要实现序列化
   */
  @Test
  public void testSecondCache() {

        SqlSessionFactory sqlSessionFactory = SqlSessionFactoryUtil.getSqlSessionFactory();
        SqlSession sqlSession1 = sqlSessionFactory.openSession();
        SqlSession sqlSession2 = sqlSessionFactory.openSession();
        PersonDao personDao1 = sqlSession1.getMapper(PersonDao.class);
        PersonDao personDao2 =sqlSession2.getMapper(PersonDao.class);

        Person person1 = personDao1.select(1L);
        // 清空一级缓存
        sqlSession1.close();
        Person person2 = personDao2.select(1L);

        // 可以查看控制台打印的 Cache Hit Ratio缓存命中率

        // 二级缓存的地址是不一样的 一级缓存的话对象就是一样的
        System.out.println(personDao1==personDao2);

  }




}
