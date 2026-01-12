module com.hospital_management.client {

    // JavaFX
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;

    // DB (daca folosesti JDBC pe client)
    requires java.sql;

    // 3rd party (le poti pastra daca le folosesti)
    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires net.synedra.validatorfx;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.bootstrapfx.core;
    requires eu.hansolo.tilesfx;
    requires com.almasb.fxgl.all;

    // Export: doar ce e "entry point"/public API
    exports app;

    // FXML controllers -> trebuie OPEN catre javafx.fxml
    opens app to javafx.fxml;
    opens view.auth to javafx.fxml;
    opens view.admin to javafx.fxml;
    opens view.doctor to javafx.fxml;
    opens view.manager to javafx.fxml;
    opens view.patient to javafx.fxml;

    // DTO-uri folosite via reflectie (ex. TableView PropertyValueFactory)
    opens model.dto to javafx.base;
}
