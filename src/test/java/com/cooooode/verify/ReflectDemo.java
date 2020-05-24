package com.cooooode.verify;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

/**
 * @program: verify
 * @description:
 * @author: vua
 * @create: 2020-02-22 14:14
 */
public class ReflectDemo {
    //Test ,you can delete this method

    void test() {
        List l = Arrays.asList(1, 2, 3, 4, 5, 6);
        l.stream().mapToInt(o -> (Integer) o).toArray();
        Map<Integer,String> map= Collections.synchronizedMap(new HashMap<Integer, String>());
    }
    @Test
    void hashMap() throws Exception {
        Class clazz= HashMap.class;

        HashMap<Integer, Integer> map= new HashMap<Integer, Integer>(5);
        map.put(1,1);
        Field[] fs = clazz.getDeclaredFields();
        for (Field f:
             fs) {
            System.out.println(f.getName());
        }
        Field f=clazz.getDeclaredField("threshold");
        Field f1=clazz.getDeclaredField("table");
        f.setAccessible(true);
        f1.setAccessible(true);
        System.out.println(f.get(map));
        System.out.println(((Object[])(f1.get(map))).length);
    }
}
