package org.apache.catalina.deploy;

public final class Test {

    public static void main(String args[]) {

        String list[] = null;

        System.out.println("Creating new collection");
        SecurityCollection collection = new SecurityCollection();

        System.out.println("Adding GET and POST methods");
        collection.addMethod("GET");
        collection.addMethod("POST");

        System.out.println("Currently defined methods:");
        list = collection.findMethods();
        for (int i = 0; i < list.length; i++)
            System.out.println(" " + list[i]);
        System.out.println("Is DELETE included? " +
                           collection.findMethod("DELETE"));
        System.out.println("Is POST included? " +
                           collection.findMethod("POST"));

        System.out.println("Removing POST method");
        collection.removeMethod("POST");

        System.out.println("Currently defined methods:");
        list = collection.findMethods();
        for (int i = 0; i < list.length; i++)
            System.out.println(" " + list[i]);
        System.out.println("Is DELETE included? " +
                           collection.findMethod("DELETE"));
        System.out.println("Is POST included? " +
                           collection.findMethod("POST"));

    }

}
