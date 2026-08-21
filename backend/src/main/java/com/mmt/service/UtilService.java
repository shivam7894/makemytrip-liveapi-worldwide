package com.mmt.service;
import java.nio.charset.StandardCharsets; import java.security.MessageDigest; import java.time.*; import java.util.*;
import org.springframework.stereotype.Service;
@Service public class UtilService { public String hash(String s){ try{byte[] b=MessageDigest.getInstance("SHA-256").digest(s.getBytes(StandardCharsets.UTF_8)); StringBuilder x=new StringBuilder(); for(byte v:b)x.append(String.format("%02x",v)); return x.toString();}catch(Exception e){throw new RuntimeException(e);} }
 public double multiplier(int demand){ double m=1+(Math.max(0,Math.min(100,demand))/100.0)*0.25; LocalDate d=LocalDate.now(); if(d.getMonthValue()==12 || d.getMonthValue()==5 || d.getDayOfWeek()==DayOfWeek.SATURDAY || d.getDayOfWeek()==DayOfWeek.SUNDAY)m*=1.20; if(d.getDayOfMonth()==15 && d.getMonthValue()==8)m*=1.20; return Math.round(m*100.0)/100.0; }
 public String pricingReason(int demand){ LocalDate d=LocalDate.now(); if(d.getMonthValue()==12||d.getMonthValue()==5||d.getDayOfWeek()==DayOfWeek.SATURDAY||d.getDayOfWeek()==DayOfWeek.SUNDAY)return "Peak/seasonal period + demand"; return demand>70?"High demand":"Normal demand"; }
}
