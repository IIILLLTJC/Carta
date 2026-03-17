package com.cartaxi;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@MapperScan("com.cartaxi.mapper")
@SpringBootApplication
public class CarTaxiApplication {

    public static void main(String[] args) {
        SpringApplication.run(CarTaxiApplication.class, args);
    }
}
