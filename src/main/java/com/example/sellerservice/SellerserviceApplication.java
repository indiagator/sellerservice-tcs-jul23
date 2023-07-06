package com.example.sellerservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;

@SpringBootApplication
@EnableEurekaClient
public class SellerserviceApplication
{
    public static void main(String[] args)
    {
        SpringApplication.run(SellerserviceApplication.class, args);
    }

}
