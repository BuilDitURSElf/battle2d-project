// project 

package view;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D; // Import needed for Screen bounds
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Screen; // Import needed to get monitor size
import javafx.stage.Stage;
import models.Entity;
import models.Projectile;
import models.characters.*; 

import java.util.ArrayList;
import java.util.List;

public class ArenaView {

    private Stage stage;
    private Scene scene;
    private Canvas canvas;
    private GraphicsContext gc;

    private Fighter player1;
    private Fighter player2;
    private List<Entity> entities;

    // UI Elements
    private ProgressBar p1Bar;
    private ProgressBar p2Bar;
    private Label p1Label;
    private Label p2Label;
    private Label p1AmmoLabel;
    private Label p2AmmoLabel;

    private ImageView backgroundView;

    public ArenaView(Stage stage, String p1Type, String p2Type) {
        this.stage = stage;
        this.entities = new ArrayList<>();

        // --- NEW: Get Maximum Screen Width & Height ---
        Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
        double width = screenBounds.getWidth();
        double height = screenBounds.getHeight();

        // Apply to Stage (Window)
        stage.setX(screenBounds.getMinX());
        stage.setY(screenBounds.getMinY());
        stage.setWidth(width);
        stage.setHeight(height);
        stage.setMaximized(true); // Force Maximized Mode

        // Create Fighters (Positioned based on screen size)
        this.player1 = createFighter(p1Type, 100, height / 2);
        this.player2 = createFighter(p2Type, width - 200, height / 2);
        
        // Initial Rotation
        this.player1.setRotation(0);   
        this.player2.setRotation(180); 
        
        entities.add(player1);
        entities.add(player2);

        // Background
        try {
            if (getClass().getResource("/imagesandstyles/1.png") != null) {
                Image bgImage = new Image(getClass().getResourceAsStream("/imagesandstyles/1.png"));
                this.backgroundView = new ImageView(bgImage);
                // Make background fill the screen
                this.backgroundView.setFitWidth(width);
                this.backgroundView.setFitHeight(height);
            }
        } catch (Exception e){
            System.out.println("Error loading background: " + e.getMessage());
        }

        // Canvas (Set to full screen size)
        this.canvas = new Canvas(width, height);
        this.gc = canvas.getGraphicsContext2D();

        // UI Layer
        BorderPane uiLayer = new BorderPane();
        uiLayer.setPadding(new Insets(20));

        // P1 UI
        p1Label = new Label("P1: " + p1Type);
        p1Label.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 18px;");
        p1Bar = new ProgressBar(1.0);
        p1Bar.getStyleClass().add("health-bar");
        p1Bar.setPrefWidth(300);
        p1AmmoLabel = new Label("AMMO: --");
        p1AmmoLabel.setStyle("-fx-text-fill: #e1b12c; -fx-font-weight: bold; -fx-font-size: 14px;");

        VBox p1Box = new VBox(5, p1Label, p1Bar, p1AmmoLabel);
        uiLayer.setLeft(p1Box);

        // P2 UI
        p2Label = new Label("P2: " + p2Type);
        p2Label.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 18px;");
        p2Bar = new ProgressBar(1.0);
        p2Bar.getStyleClass().add("health-bar");
        p2Bar.setPrefWidth(300);
        p2AmmoLabel = new Label("AMMO: --");
        p2AmmoLabel.setStyle("-fx-text-fill: #e1b12c; -fx-font-weight: bold; -fx-font-size: 14px;");

        VBox p2Box = new VBox(5, p2Label, p2Bar, p2AmmoLabel);
        p2Box.setAlignment(Pos.TOP_RIGHT);
        uiLayer.setRight(p2Box);

        // Scene
        StackPane root = new StackPane();
        root.setStyle("-fx-background-color: black;");
        if (backgroundView != null) root.getChildren().add(backgroundView);
        root.getChildren().addAll(canvas, uiLayer);
        
        this.scene = new Scene(root, width, height);
        this.scene.setFill(Color.BLACK);
        
        try {
            if (getClass().getResource("/imagesandstyles/uistyle.css") != null) {
                this.scene.getStylesheets().add(getClass().getResource("/imagesandstyles/uistyle.css").toExternalForm());
            }
        } catch (Exception e) {}
        
        render();
    }

    public void render() {
        double w = canvas.getWidth();
        double h = canvas.getHeight();
        gc.clearRect(0, 0, w, h);
        
        // Middle Line
        gc.setStroke(Color.RED);
        gc.setLineWidth(4);
        gc.strokeLine(w / 2, 0, w / 2, h);

        // Draw Entities
        for (int i = 0; i < entities.size(); i++) {
            drawEntity(entities.get(i));
        }

        updateUI();
    }

    // --- ROTATION DRAWING LOGIC ---
    private void drawEntity(Entity e) {
        if (e == null) return;

        // 1. Pick Color
        if (e instanceof Warrior) gc.setFill(Color.BLUE); 
        else if (e instanceof Mage) gc.setFill(Color.PURPLE);
        else if (e instanceof Sniper) gc.setFill(Color.GREEN);
        else if (e instanceof Projectile) gc.setFill(Color.YELLOW); 
        else gc.setFill(Color.GRAY);

        // 2. Save current canvas state
        gc.save(); 

        // 3. Move 'pen' to center of entity
        double centerX = e.getX() + e.getWidth() / 2;
        double centerY = e.getY() + e.getHeight() / 2;
        gc.translate(centerX, centerY);

        // 4. Rotate canvas
        gc.rotate(e.getRotation());

        // 5. Draw Rectangle centered
        gc.fillRect(-e.getWidth() / 2, -e.getHeight() / 2, e.getWidth(), e.getHeight());
        
        // 6. Draw Outline
        gc.setStroke(Color.BLACK);
        gc.setLineWidth(2);
        gc.strokeRect(-e.getWidth() / 2, -e.getHeight() / 2, e.getWidth(), e.getHeight());



        // 8. Restore canvas state
        gc.restore();
    }

    private void updateUI() {
        if (player1 != null && player1.getMaxHealth() > 0) {
            p1Bar.setProgress(Math.max(0, player1.getHealth() / player1.getMaxHealth()));
            p1Label.setText("P1: " + (int)player1.getHealth());
        }
        if (player2 != null && player2.getMaxHealth() > 0) {
            p2Bar.setProgress(Math.max(0, player2.getHealth() / player2.getMaxHealth()));
            p2Label.setText("P2: " + (int)player2.getHealth());
        }
        updateAmmoText(player1, p1AmmoLabel);
        updateAmmoText(player2, p2AmmoLabel);
    }

    // --- UPDATED AMMO TEXT METHOD FROM PREVIOUS STEP ---
    private void updateAmmoText(Fighter p, Label label) {
        if (p == null || label == null || p.getWeapon() == null) return;
        
        String wName = p.getWeapon().getName().toUpperCase();

        if (p.getWeapon().isReloading()) {
            label.setText(wName + ": RELOADING... " + (int)(p.getWeapon().getReloadProgress() * 100) + "%");
            label.setStyle("-fx-text-fill: red; -fx-font-weight: bold; -fx-font-size: 14px;");
        } else {
            label.setText(wName + ": " + p.getWeapon().getAmmo() + " / " + p.getWeapon().getMaxAmmo());
            if (p.getWeapon().getAmmo() == 0) {
                label.setStyle("-fx-text-fill: gray; -fx-font-weight: bold; -fx-font-size: 14px;");
            } else {
                label.setStyle("-fx-text-fill: #e1b12c; -fx-font-weight: bold; -fx-font-size: 14px;");
            }
        }
    }

    private Fighter createFighter(String type, double x, double y) {
        if (type == null) return new Warrior(x, y);
        switch (type) {
            case "Warrior": return new Warrior(x, y);
            case "Mage":    return new Mage(x, y);
            case "Sniper":  return new Sniper(x, y);
            default:        return new Warrior(x, y);
        }
    }
    
    public List<Entity> getEntities() { return entities; }
    public Fighter getPlayer1() { return player1; }
    public Fighter getPlayer2() { return player2; }
    public Scene getScene() { return scene; }

    public void showWinner(String winnerName) {
        Platform.runLater(() -> {
            Rectangle overlay = new Rectangle(scene.getWidth(), scene.getHeight(), Color.rgb(0, 0, 0, 0.85));
            Label titleLabel = new Label("WE HAVE A WINNER!");
            titleLabel.setStyle("-fx-text-fill: #e1b12c; -fx-font-family: 'Arial Black'; -fx-font-size: 48px;");
            Label winnerLabel = new Label(winnerName + " WINS");
            winnerLabel.setStyle("-fx-text-fill: white; -fx-font-family: 'Arial'; -fx-font-weight: bold; -fx-font-size: 32px;");
            Button exitBtn = new Button("EXIT TO DESKTOP");
            exitBtn.setStyle("-fx-background-color: #333333; -fx-text-fill: white; -fx-font-size: 18px; -fx-font-weight: bold; -fx-padding: 10px;");
            exitBtn.setOnAction(e -> stage.close());
            VBox box = new VBox(20, titleLabel, winnerLabel, exitBtn);
            box.setAlignment(Pos.CENTER);
            box.setMaxSize(500, 300);
            box.setStyle("-fx-background-color: rgba(20, 20, 20, 0.95); -fx-border-color: #e1b12c; -fx-border-width: 3px; -fx-padding: 40px;");
            StackPane root = (StackPane) scene.getRoot();
            root.getChildren().addAll(overlay, box);
        });
    }
}
