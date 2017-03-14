package ex15.pyrmont.digestertest;

public class Address {
  private String streetName;
  private String streetNumber;
  public Address() {
    System.out.println("....Creating Address");
  }
  public String getStreetName() {
    return streetName;
  }
  public void setStreetName(String streetName) {
    System.out.println("....Setting streetName : " + streetName);
    this.streetName = streetName;
  }
  public String getStreetNumber() {
    return streetNumber;
  }
  public void setStreetNumber(String streetNumber) {
    System.out.println("....Setting streetNumber : " + streetNumber);
    this.streetNumber = streetNumber;
  }
  public String toString() {
    return "...." + streetNumber + " " + streetName; 
  }
}
