package com.example.demo.service.funtionalProgramming;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public class FuntionalProgramming {


    public static void main(String[] args) {
        List< Integer> listInteger = List.of(2, 3, 5, 7, 1, 7, 10).stream() // Lấy ra stream từ List
                .map(num -> num * 2) // Mỗi phần tử sẽ được nhân đôi
                .filter(num -> true) // Chỉ giữ lại các phần tử nhỏ hơn 10
                .collect(Collectors.toList()); // In ra các phần tử còn lại

        System.out.println(listInteger);


        // Lấy số lẻ, nhân đôi, tính tổng
        List<Integer> nums = List.of(1,2,3,4,5,6);

        int sum = nums.stream().
                filter(num -> num % 2 == 1)
                .map(num -> num * 2)
                .mapToInt(Integer::intValue).sum();

        System.out.println("sum = " + sum);


        // Bỏ null & rỗng, viết hoa, đưa về List
        List<String> names = List.of("An", "", "Binh", null, "Cuong");
        List<String> newNames = names
                .stream()
                .filter(Objects::nonNull)
                .filter(name -> !name.isBlank())
                .map(String::toUpperCase)
                .toList();
        System.out.println(newNames);

        List<User> users = List.of(
                new User("A", Status.ACTIVE),
                new User("B", Status.INACTIVE),
                new User("C", Status.ACTIVE),
                new User("D", Status.BANNED),
                new User("E", Status.ACTIVE)
        );

        Map<Status, Long> result = users.stream()
                .collect(Collectors.groupingBy(
                        User::getStatus,
                        Collectors.counting()
                ));



    }
    static class User {
        private String name;
        private Status status;

        public User(String name, Status status) {
            this.name = name;
            this.status = status;
        }

        public Status getStatus() {
            return status;
        }
    }
    enum Status {
        ACTIVE, INACTIVE, BANNED
    }


}
