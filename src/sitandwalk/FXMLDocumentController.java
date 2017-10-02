package sitandwalk;

import java.awt.AWTException;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.Toolkit;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.net.URL;
import java.util.Date;
import java.util.ResourceBundle;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;
import javax.swing.Timer;

public class FXMLDocumentController implements Initializable, ActionListener {
    
    @FXML
    private Label label;
    
    @FXML
    private Label noteLabel;
    
    @FXML
    private TextField intervalMinutesInput;
    
    @FXML
    private Button toggleSitWalkButton;
    
    @FXML
    private Button updateIntervalButton;
    
    private Timer timer;
    private Date startTime;
    private String prefix;
    private Boolean hasNotified;
    private TrayIcon trayIcon;
    private Boolean isWalking;
    private long intervalMinutes;
    private final String title;
    private final String exitText;
    private final String sittingPrefix;
    private final String walkingPrefix;
    private final String helpMessage;
    private final String notificationText;
    private final String iAmWalking;
    private final String iAmSitting;
    private final String updateTime;

    /**
     * Initialise all the initial values in the constructor.
     */
    public FXMLDocumentController() {
        this.title = "Sit And Walk";
        this.exitText = "Exit";
        this.intervalMinutes = 30;
        this.sittingPrefix = "Sitting for: ";
        this.walkingPrefix = "Walking for: ";
        this.helpMessage = "We'll show a notification at %d minute%s of sitting.";
        this.notificationText = "%d minute%s up. Time to take a walk!";
        this.iAmWalking = "I am going for a walk";
        this.iAmSitting = "I just sat down";
        this.updateTime = "Update Interval";
    }
    
    /**
     * Toggles between the system recognising the user is walking and the user
     * is sitting.
     * @param event 
     */
    @FXML
    private void toggleWalkSitButton(ActionEvent event) {
        isWalking = !isWalking;
        startTime = new Date();
        prefix = isWalking ? walkingPrefix : sittingPrefix;
        hasNotified = isWalking;
        toggleSitWalkButton.setText(isWalking ? iAmSitting : iAmWalking);
        this.updateTimerDisplay();
    }
    
    /**
     * Handles the user updating the time in the minutes text field.
     * @param event 
     */
    @FXML
    private void handleUpdateTimeButton(ActionEvent event) {
        intervalMinutes = Long.parseLong(intervalMinutesInput.getText());
        if (intervalMinutes < 1l) {
            intervalMinutes = 1l;
            intervalMinutesInput.setText("1");
        }
        hasNotified = false;
        this.updateNoteLabelText();
    }
    
    /**
     * Sets the applications help message based on the currently set interval.
     */
    private void updateNoteLabelText() {
        noteLabel.setText(String.format(helpMessage, (int)(intervalMinutes), ((int)intervalMinutes == 1 ? "" : "s")));
    }
    
    /**
     * Initialise the application.
     * @param url
     * @param rb 
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        isWalking = false;
        prefix = sittingPrefix;
        startTime = new Date();
        timer = new Timer(1000, this);
        timer.start();
        hasNotified = false;
        label.setText(String.format("%s%d:%d", prefix, 0, 0));
        this.updateNoteLabelText();
        this.toggleSitWalkButton.setText(iAmWalking);
        this.updateIntervalButton.setText(updateTime);
        intervalMinutesInput.addEventFilter(KeyEvent.KEY_TYPED, (KeyEvent inputevent) -> {
            if (!inputevent.getCharacter().matches("\\d")) {
                inputevent.consume();
            }
        });
        if (SystemTray.isSupported()) {
            this.initialiseSystemTrayIcon();
        }
    }    

    /**
     * An action to update the running time of the timer.
     * @param e 
     */
    @Override
    public void actionPerformed(java.awt.event.ActionEvent e) {
        this.updateTimerDisplay();
    }
    
    private void updateTimerDisplay() {
        Date now = new Date();
        long runningTime = Math.abs(now.getTime() - startTime.getTime());
        long minutes = runningTime / 60000;
        long seconds = minutes > 0 ? ((runningTime % (minutes * 60000)) / 1000) : runningTime / 1000;
        
        // If the user has not been notified and they are not walking and the
        // timer shows that they have been walking for a great number of minutes
        // than the threshold for which they should get up and walk, then show
        // a notification.
        if (!hasNotified && !isWalking && minutes >= intervalMinutes) {
            Platform.runLater(() -> {
                showNotification();
            });
        }
        
        // Update the number.
        Platform.runLater(() -> {
            label.setText(String.format("%s%d:%d", prefix, minutes, seconds));
        });
    }
    
    /**
     * Shows the notification.
     */
    private void showNotification() {
        hasNotified = true;
        if (SystemTray.isSupported() && trayIcon != null) {
            trayIcon.displayMessage(title, String.format(notificationText, ((int)intervalMinutes), ((int)intervalMinutes == 1 ? "" : "s")), TrayIcon.MessageType.NONE);
        }
    }

    /**
     * Initialise the system tray icon.
     */
    private void initialiseSystemTrayIcon() {
        SystemTray tray = SystemTray.getSystemTray();
        Toolkit toolkit = Toolkit.getDefaultToolkit();
        java.awt.Image image = toolkit.createImage(getClass().getResource("sitandwalk.png"));
        trayIcon = new TrayIcon(image, title);
        trayIcon.setImageAutoSize(true);
        trayIcon.setToolTip(title);
        final PopupMenu popup = new PopupMenu();
        MenuItem exitItem = new MenuItem(exitText);
        exitItem.addActionListener(e -> {
            timer.stop();
            SystemTray.getSystemTray().remove(trayIcon);
            Platform.exit();
            System.exit(0);
        });
        popup.add(exitItem);
        trayIcon.setPopupMenu(popup);
        trayIcon.addMouseListener(new TaskIconMouseListener());
        try {
            tray.add(trayIcon);
        }
        catch (AWTException ex) {
            System.out.println("Error adding icon to tray.");
        }
    }
    
    /**
     * Class used handle clicks on the task bar icon.
     */
    private class TaskIconMouseListener implements MouseListener {

        @Override
        public void mouseClicked(MouseEvent e) { }

        /**
         * When the icon is double-clicked, bring the program to the front.
         * @param e The mouse event.
         */
        @Override
        public void mousePressed(MouseEvent e) { 
            if (e.getClickCount() >= 2) {
                Platform.runLater(() -> {
                    Stage stage = SitAndWalk.getPrimaryStage();
                    stage.setIconified(false);
                    stage.show();
                    stage.toFront();
                });
            }
        }

        @Override
        public void mouseReleased(MouseEvent e) { }
        
        @Override
        public void mouseEntered(MouseEvent e) { }
        
        @Override 
        public void mouseExited(MouseEvent e) { }
    }
}