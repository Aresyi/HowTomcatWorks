package ex20.pyrmont.modelmbeantest1;

public class Car {
  
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
