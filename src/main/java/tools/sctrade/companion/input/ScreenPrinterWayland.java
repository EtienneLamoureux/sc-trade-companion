import javafx.application.Application;
import javafx.geometry.Rectangle2D;
import javafx.scene.SnapshotParameters;
import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;
import javafx.scene.image.WritableImage;
import javafx.stage.Screen;
import javafx.stage.Stage;

import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;

public class WaylandScreenCapture {

    // Assuming you have OpenCV setup correctly in your project

    public static Mat captureScreenshot() {
        // Get primary screen bounds
        Rectangle2D screenBounds = Screen.getPrimary().getBounds();

        // Create a SnapshotParameters object
        SnapshotParameters params = new SnapshotParameters();
        params.setFill(javafx.scene.paint.Color.TRANSPARENT); // Transparent background for accurate capture

        // Capture the screenshot as a WritableImage
        WritableImage image = Screen.getPrimary().snapshot(params, null);

        // Get pixel reader from the captured image
        PixelReader reader = image.getPixelReader();

        // Create OpenCV Mat to store captured pixels
        Mat mat = new Mat((int) screenBounds.getHeight(), (int) screenBounds.getWidth(), org.opencv.core.CvType.CV_8UC3);

        // Iterate over the pixels and copy them into the OpenCV Mat
        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                javafx.scene.paint.Color color = reader.getColor(x, y);

                // Convert JavaFX Color to OpenCV BGR format
                mat.put(y, x, new double[]{color.getBlue() * 255, color.getGreen() * 255, color.getRed() * 255});
            }
        }

        return mat;
    }
