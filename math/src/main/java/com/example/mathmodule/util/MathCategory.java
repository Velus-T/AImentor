package com.example.mathmodule.util;
import java.util.Locale;
public final class MathCategory {
 public static final String ALGEBRA="algebra",GEOMETRY="geometry";
 private MathCategory() {}
 public static String canonical(String value) {
  String text=value==null?"":value.trim();String lower=text.toLowerCase(Locale.ROOT);
  if(lower.equals("đại số")||lower.equals("algebra"))return ALGEBRA;
  if(lower.equals("hình học")||lower.equals("geometry"))return GEOMETRY;
  return text;
 }
 public static String[] aliases(String value) {
  String key=canonical(value);
  if(ALGEBRA.equals(key))return new String[]{ALGEBRA,"Đại số","Algebra"};
  if(GEOMETRY.equals(key))return new String[]{GEOMETRY,"Hình học","Geometry"};
  return new String[]{key,key,key};
 }
}
