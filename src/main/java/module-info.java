module com.inventory.javaexamcomponents {
    requires javafx.controls;
    requires javafx.fxml;
    requires static lombok;

    opens com.javafx.javafx.models to javafx.base;
    opens com.javafx.javafx.controller to javafx.fxml;

    requires org.controlsfx.controls;
    requires java.sql;

    opens com.inventory.javaexamcomponents to javafx.fxml;
    exports com.inventory.javaexamcomponents;
}