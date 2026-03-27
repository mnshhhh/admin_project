package com.university.asset;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@MapperScan("com.university.asset.mapper")
@EnableScheduling
public class AssetManagementApplication {
    public static void main(String[] args) {
        SpringApplication.run(AssetManagementApplication.class, args);
    }
}
