package ex20.pyrmont.standardmbeantest;

public class Car implements CarMBean {
  private String color = "red";
  
  public String getColor() {
    return color;
  }
  public void setColor(String color) {
    this.color = color;
  }
  
  public void drive() {
    System.out.println("Baby you can drive my car.");
  }
}
