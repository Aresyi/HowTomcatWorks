package ex20.pyrmont.modelmbeantest2;

public class Car {
  public Car() {
    System.out.println("Car constructor");
  }
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
