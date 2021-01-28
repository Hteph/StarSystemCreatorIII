package com.github.hteph.main;

import com.github.hteph.generators.StarFactory;
import com.github.hteph.repository.CentralRegistry;
import com.github.hteph.repository.objects.Planet;
import com.github.hteph.repository.objects.Star;
import com.github.hteph.repository.objects.StellarObject;
import com.github.hteph.utils.PrintThisPage;
import com.github.hteph.utils.SaveThisLocally;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Side;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.ArrayList;

public class StartGUI extends Application {

    @Override
    public void start(Stage primaryStage) {

        ArrayList<ArrayList<StellarObject>> oldSystems = new ArrayList<>();

        try {
            oldSystems = SaveThisLocally.restoreSaved();
        } catch (Exception e2) {
            System.out.println("restore object fail");
            e2.printStackTrace();
        }

        StellarObject star =  StarFactory.generate("Main", 'A', null);

        ArrayList<StellarObject> systemList = StarSystemGenerator.Generator((Star) star);

        oldSystems.add(systemList);

        final ArrayList<ArrayList<StellarObject>> newSystems = oldSystems;

        //Generate GUI--------------------------------------------------------------------------------------
        try {
            Group root = new Group();
            Scene scene = new Scene(root,800,1000);
            primaryStage.setTitle("StarView");
            BorderPane borderPane = new BorderPane();
            borderPane.prefHeightProperty().bind(scene.heightProperty());
            borderPane.prefWidthProperty().bind(scene.widthProperty());

            TabPane tabPane = new TabPane();
            tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
            borderPane.setCenter(tabPane);

            for (int i = 0; i < newSystems.size(); i++) {
                Tab tab = new Tab();

                String starSystemName = newSystems.get(i).get(0).toString();
                Star centralStar = (Star) newSystems.get(i).get(0);

                tab.setText(starSystemName);
                VBox starBox = new VBox();

                starBox.getChildren().add(new Label(starSystemName));
                starBox.setAlignment(Pos.CENTER);

                TabPane orbitTabs = new TabPane();
                orbitTabs.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
                orbitTabs.setSide(Side.RIGHT);
                orbitTabs.prefHeightProperty().bind(scene.heightProperty());
                orbitTabs.prefWidthProperty().bind(scene.widthProperty());

                for (int n = 0; n < centralStar.getOrbitalObjects().size(); n++) {
                    Tab orbit = new Tab();

                    // String orbitingStarName = centralStar.getOrbitalObjects().get(n).getName();
                    StellarObject thingOrbitingStar = CentralRegistry.getFromArchive(centralStar.getOrbitalObjects().get(n));

                    orbit.setText(thingOrbitingStar.getName());
                    VBox orbitbox = new VBox();
                    //orbitbox.getChildren().add(new Label(text2));

                    TabPane moonTabs = new TabPane();
                    moonTabs.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
                    moonTabs.setSide(Side.BOTTOM);
                    moonTabs.prefHeightProperty().bind(scene.heightProperty());
                    moonTabs.prefWidthProperty().bind(scene.widthProperty());

                    int aNumber=1;
                    if(thingOrbitingStar instanceof Planet) aNumber +=((Planet) thingOrbitingStar).getMoonList().size();

                    for (int j = 0; j < aNumber; j++) {

                        Tab moon = new Tab();
                        VBox moonbox = new VBox();
                        if(j==0) {
                            String objectName = thingOrbitingStar.getName();
                            moon.setText(objectName);
                            moonbox.getChildren().add(new Label(objectName));
                            moonbox.getChildren().add(Pagemaker.generator(thingOrbitingStar));
                        }else {
                            String objectName = CentralRegistry.getFromArchive(((Planet) thingOrbitingStar).getMoonList().get(j-1)).getName();
                            moon.setText(objectName);
                            moonbox.getChildren().add(new Label(objectName));

                            moonbox.getChildren().add(Pagemaker.generator(CentralRegistry.getFromArchive(((Planet) thingOrbitingStar).getMoonList().get(j-1))));
                        }

                        //Utility box

                        HBox utility = new HBox();
                        utility.setAlignment(Pos.BOTTOM_CENTER);
                        utility.setPadding(new Insets(15, 12, 15, 12));
                        //Utskrift
                        Button printButton = new Button("Print");
                        utility.getChildren().add(printButton);
                        printButton.setOnAction(event -> PrintThisPage.print(root));
                        //Save
                        Button saveButton = new Button("Save");
                        utility.getChildren().add(saveButton);
                        saveButton.setOnAction(event -> SaveThisLocally.saveThis(newSystems));

//                        saveButton.setOnAction(new EventHandler <ActionEvent>() {
//                            public void handle(ActionEvent event) {
//                                saveThisLocally.saveThis(newSystems);
//                            }
//
//                        });

                        moonbox.getChildren().add(utility);
                        moon.setContent(moonbox);
                        moonTabs.getTabs().add(moon);
                    }
                    orbitbox.getChildren().add(moonTabs);
                    orbit.setContent(orbitbox);
                    orbitTabs.getTabs().add(orbit);
                }
                starBox.getChildren().add(orbitTabs);
                tab.setContent(starBox);
                tabPane.getTabs().add(tab);
            }

            root.getChildren().add(borderPane);
            scene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());
            primaryStage.setScene(scene);
            primaryStage.show();

        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
