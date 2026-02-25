package util;

import javafx.animation.FadeTransition;
import javafx.application.Platform;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.util.Duration;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class IdleScreenService {

    private final Parent root;
    private final Scene scene;
    private long lastTouchTime = System.currentTimeMillis();
    private final int idleTime = 10000; // 10s
    private boolean isIdle = false;

    private ArrayList<String> imageUrls = new ArrayList<>();
    private int currentIndex = 0;

    public interface IdleExitListener {
        void onExitIdle();
    }

    private final IdleExitListener exitListener;

    public IdleScreenService(Scene scene, Parent root, IdleExitListener exitListener) {
        this.scene = scene;
        this.root = root;
        this.exitListener = exitListener;

        hookUserActivity();
        startWatcherThread();
    }

    private void hookUserActivity() {
        scene.setOnMouseMoved(e -> onUserInteraction());
        scene.setOnMousePressed(e -> onUserInteraction());
        scene.setOnTouchPressed(e -> onUserInteraction());
    }

    private void onUserInteraction() {
        lastTouchTime = System.currentTimeMillis();

        if (isIdle) {
            isIdle = false;
            Platform.runLater(exitListener::onExitIdle);
        }
    }

    private void startWatcherThread() {
        Thread t = new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep(800);

                    long now = System.currentTimeMillis();

                    if (!isIdle && now - lastTouchTime >= idleTime) {
                        isIdle = true;
                        loadImagesAndStartSlideShow();
                    }

                } catch (Exception ignored) {}
            }
        });

        t.setDaemon(true);
        t.start();
    }

    private void loadImagesAndStartSlideShow() {
        new Thread(() -> {
            imageUrls = fetchImageListFromServer();

            Platform.runLater(() -> {
                if (imageUrls.isEmpty()) {
                    root.getChildrenUnmodifiable().clear();

                    Label test = new Label("NO IMAGES FOUND");
                    test.setStyle("-fx-font-size: 35px; -fx-text-fill: red;");
                    root.getChildrenUnmodifiable().add(test);

                } else {
                    startSlideShow();
                }
            });
        }).start();
    }

    private ArrayList<String> fetchImageListFromServer() {
        ArrayList<String> urls = new ArrayList<>();

        try {
            URL url = new URL("https://menschwoodworks.ir/API/getIdleImages.php");
            HttpURLConnection c = (HttpURLConnection) url.openConnection();

            BufferedReader br = new BufferedReader(new InputStreamReader(c.getInputStream()));
            String line;
            while ((line = br.readLine()) != null) urls.add(line.trim());

            br.close();
        } catch (Exception ignored) {}

        return urls;
    }

    private void startSlideShow() {
        if (!isIdle) return;

        currentIndex = 0;
        showNextImage();
    }

    private void showNextImage() {
        if (!isIdle) return;

        Platform.runLater(() -> {
            try {
                String imgUrl = imageUrls.get(currentIndex);
                Image img = new Image(imgUrl);

                ImageView iv = new ImageView(img);
                iv.setPreserveRatio(true);
                iv.setFitWidth(900);

                root.getChildrenUnmodifiable().clear();
                root.getChildrenUnmodifiable().add(iv);

                FadeTransition ft = new FadeTransition(Duration.millis(1200), iv);
                ft.setFromValue(0);
                ft.setToValue(1);
                ft.play();

                currentIndex = (currentIndex + 1) % imageUrls.size();

                new Thread(() -> {
                    try {
                        Thread.sleep(5000);
                        showNextImage();
                    } catch (Exception ignored) {}
                }).start();

            } catch (Exception ignored) {}
        });
    }
}
