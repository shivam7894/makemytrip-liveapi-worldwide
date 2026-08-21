package com.mmt;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
@SpringBootApplication
@EnableScheduling
public class MakeMyTripApplication { public static void main(String[] args){ SpringApplication.run(MakeMyTripApplication.class,args); } }
