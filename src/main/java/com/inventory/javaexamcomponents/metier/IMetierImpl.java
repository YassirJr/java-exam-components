package com.inventory.javaexamcomponents.metier;


public class IMetierImpl implements IMetier {
    private final Connection connection = SingletonConnexionDB.getConnexion();

}