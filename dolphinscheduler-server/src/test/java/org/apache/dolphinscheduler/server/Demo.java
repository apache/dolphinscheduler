package org.apache.dolphinscheduler.server;

import java.util.ArrayList;
import java.util.List;

public class Demo {

    public static void main(String[] args) {
        List<String> list = new ArrayList<>();

        Integer pageNo=2;
        Integer pageSize=2;
        String searchStr = "3";

        Integer fromIndex = (pageNo - 1) * pageSize;
        Integer toIndex = (pageNo - 1) * pageSize + pageSize;



        list.add("1");
        list.add("2");
        list.add("3");
        list.add("4");
        list.add("5");
        list.add("6");
        list.add("7");
        list.add("8");
        list.add("9");
        list.add("10");

        if (searchStr != null){

        }
        List<String> stringList = list.subList(fromIndex, toIndex);
        System.out.println(stringList);


    }
}
