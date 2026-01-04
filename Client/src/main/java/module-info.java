module com.hospital_management.client {

    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;

    requires java.sql;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires net.synedra.validatorfx;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.bootstrapfx.core;
    requires eu.hansolo.tilesfx;
    requires com.almasb.fxgl.all;

    opens com.hospital_management to javafx.fxml;
    exports com.hospital_management;

    opens com.hospital_management.client.controller.auth to javafx.fxml;

    exports launcher;
    opens launcher to javafx.fxml;
}