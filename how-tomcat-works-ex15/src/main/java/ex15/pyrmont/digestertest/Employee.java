package ex15.pyrmont.digestertest;

import java.util.ArrayList;

public class Employee {
  private String firstName;
  private String lastName;
  private ArrayList offices = new ArrayList();
    
  public Employee() {
    System.out.println("Creating Employee");
  }
  public String getFirstName() {
    return firstName;
  }
  public void setFirstName(String firstName) {
    System.out.println("Setting firstName : " + firstName);
    this.firstName = firstName;
  }
  public String getLastName() {
    return lastName;
  }
  public void setLastName(String lastName) {
    System.out.println("Setting lastName : " + lastName);
    this.lastName = lastName;
  }
  public void addOffice(Office office) {
    System.out.println("Adding Office to this employee");
    offices.add(office);
  }
  public ArrayList getOffices() {
    return offices;
  }
  public void printName() {
    System.out.println("My name is " + firstName + " " + lastName);
  }
}
