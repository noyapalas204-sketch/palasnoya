package com.example.manu;

import android.app.Notification;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.location.FusedLocationProviderClient;

import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private TextView tvStatus;
    private TextView tvLocation;

    private Button btnLocation;
    private Button btnNotification;
    private Button btnCheck;

    private FusedLocationProviderClient fusedLocationClient;

    private static final String CHANNEL_ID = "class_notifications";

    // מנהל בקשת הרשאות המיקום
    private ActivityResultLauncher<String[]> locationPermissionLauncher;

    // מנהל בקשת הרשאת ההתראות
    private ActivityResultLauncher<String> notificationPermissionLauncher;






    @Override

    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_item,menu);
        return true;

    }


    @Override
    public boolean onOptionsItemSelected( MenuItem item) {


        int id = item.getItemId();

        if (id == R.id.setting) {

            Toast.makeText(this, "You have clicked on setting", Toast.LENGTH_SHORT).show();
            return true;
        }
        if (id == R.id.share) {

            Toast.makeText(this, "You have clicked on share", Toast.LENGTH_SHORT).show();
            return true;
        }
        if (id == R.id.exit) {

            Toast.makeText(this, "You have clicked on exit", Toast.LENGTH_SHORT).show();
            return true;
        }
        if (id == R.id.info) {

            Toast.makeText(this, "You have clicked on info", Toast.LENGTH_SHORT).show();
            return true;
        }

        return super.onOptionsItemSelected(item);


    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);




        // קישור בין רכיבי ה-XML לקוד Java
        tvStatus = findViewById(R.id.tvStatus);
        tvLocation = findViewById(R.id.tvLocation);

        btnLocation = findViewById(R.id.btnLocation);
        btnNotification = findViewById(R.id.btnNotification);
        btnCheck = findViewById(R.id.btnCheck);

        // יצירת אובייקט שמאפשר לקבל מיקום
        fusedLocationClient =
                LocationServices.getFusedLocationProviderClient(this);

        // רישום מנגנון קבלת תוצאת בקשת המיקום
        locationPermissionLauncher =
                registerForActivityResult(
                        new ActivityResultContracts.RequestMultiplePermissions(),
                        result -> handleLocationPermissionResult(result)
                );

        // רישום מנגנון קבלת תוצאת בקשת ההתראות
        notificationPermissionLauncher =
                registerForActivityResult(
                        new ActivityResultContracts.RequestPermission(),
                        isGranted -> {
                            if (isGranted) {
                                showNotification();
                            } else {
                                tvStatus.setText(
                                        "המשתמש לא אישר שליחת התראות"
                                );
                            }
                        }
                );

        // יצירת ערוץ התראות
        createNotificationChannel();

        // לחיצה על כפתור המיקום
        btnLocation.setOnClickListener(v -> checkLocationPermission());

        // לחיצה על כפתור ההתראות
        btnNotification.setOnClickListener(v -> checkNotificationPermission());

        // לחיצה על כפתור בדיקת ההרשאות
        btnCheck.setOnClickListener(v -> showPermissionStatus());
    }

    // שלב 1: בדיקה אם כבר קיימת הרשאת מיקום
    private void checkLocationPermission() {

        boolean fineGranted =
                ContextCompat.checkSelfPermission(
                        this,
                        Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED;

        boolean coarseGranted =
                ContextCompat.checkSelfPermission(
                        this,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED;

        if (fineGranted || coarseGranted) {

            // יש הרשאה, ולכן אפשר לנסות לקבל מיקום
            getCurrentLocation();

        } else {

            // אין הרשאה: מבקשים מהמשתמש
            tvStatus.setText("מבקש הרשאת מיקום...");

            locationPermissionLauncher.launch(
                    new String[]{
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                    }
            );
        }
    }

    // שלב 2: טיפול בתשובת מערכת Android
    private void handleLocationPermissionResult(
            Map<String, Boolean> result) {

        boolean fineGranted = Boolean.TRUE.equals(
                result.get(Manifest.permission.ACCESS_FINE_LOCATION)
        );

        boolean coarseGranted = Boolean.TRUE.equals(
                result.get(Manifest.permission.ACCESS_COARSE_LOCATION)
        );

        if (fineGranted) {

            tvStatus.setText("אושרה הרשאת מיקום מדויק");

            getCurrentLocation();

        } else if (coarseGranted) {

            tvStatus.setText("אושרה הרשאת מיקום משוער");

            getCurrentLocation();

        } else {

            tvStatus.setText("לא ניתנה הרשאת מיקום");

            tvLocation.setText(
                    "לא ניתן לקבל מיקום ללא הרשאה מתאימה"
            );
        }
    }

    // שלב 3: קבלת מיקום המכשיר
    private void getCurrentLocation() {

        boolean fineGranted =
                ContextCompat.checkSelfPermission(
                        this,
                        Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED;

        boolean coarseGranted =
                ContextCompat.checkSelfPermission(
                        this,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED;

        // בדיקה נוספת לפני גישה למידע המוגן
        if (!fineGranted && !coarseGranted) {
            tvStatus.setText("אין הרשאת מיקום");
            return;
        }

        tvStatus.setText("מנסה לקבל את מיקום המכשיר...");
        tvLocation.setText("ממתין לתוצאה...");

        // בקשת מיקום חד-פעמית.
        // BALANCED מאפשרת להסתפק במיקום משוער כשזה מה שאושר.
        fusedLocationClient.getCurrentLocation(
                Priority.PRIORITY_BALANCED_POWER_ACCURACY,
                null
        ).addOnSuccessListener(location -> {

            if (location != null) {

                double latitude = location.getLatitude();
                double longitude = location.getLongitude();

                tvLocation.setText(
                        "Latitude (קו רוחב): " + latitude +
                                "\nLongitude (קו אורך): " + longitude +
                                "\nדיוק משוער במטרים: " +
                                location.getAccuracy()
                );

                tvStatus.setText("המיקום התקבל בהצלחה");

            } else {

                tvStatus.setText("לא התקבל מיקום");

                tvLocation.setText(
                        "בדקו שהמיקום במכשיר מופעל " +
                                "ושיש נתוני מיקום זמינים"
                );
            }

        }).addOnFailureListener(exception -> {

            tvStatus.setText("אירעה שגיאה בקבלת המיקום");

            tvLocation.setText(
                    "פרטי השגיאה: " + exception.getMessage()
            );
        });
    }

    // בדיקת הרשאת ההתראות
    private void checkNotificationPermission() {

        // Android 13 (API 33) ומעלה דורש הרשאת התראות בזמן ריצה
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {

            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED) {

                showNotification();

            } else {

                notificationPermissionLauncher.launch(
                        Manifest.permission.POST_NOTIFICATIONS
                );
            }

        } else {

            // בגרסאות ישנות יותר אין בקשת הרשאה זו
            showNotification();
        }
    }

    // יצירת ערוץ התראות, נדרש מ-Android 8 ומעלה
    private void createNotificationChannel() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            CharSequence name = "התראות לימודיות";
            String description = "התראות לדוגמה בשיעור אנדרואיד";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;

            NotificationChannel channel =
                    new NotificationChannel(
                            CHANNEL_ID,
                            name,
                            importance
                    );

            channel.setDescription(description);

            NotificationManager manager =
                    getSystemService(NotificationManager.class);

            manager.createNotificationChannel(channel);
        }
    }

    // הצגת התראה
    private void showNotification() {

        // בדיקה לפני הצגת התראה
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                ContextCompat.checkSelfPermission(
                        this,
                        Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED) {

            tvStatus.setText("אין הרשאה להצגת התראות");
            return;
        }

        Notification notification =
                new NotificationCompat.Builder(this, CHANNEL_ID)
                        .setSmallIcon(
                                android.R.drawable.ic_dialog_info
                        )
                        .setContentTitle("PermissionLab")
                        .setContentText("ההתראה הראשונה שלי באנדרואיד!")
                        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                        .setAutoCancel(true)
                        .build();

        NotificationManagerCompat.from(this).notify(
                1001,
                notification
        );

        tvStatus.setText("נשלחה בקשת הצגת התראה");
    }

    // הצגת מצב ההרשאות
    private void showPermissionStatus() {

        boolean fineGranted =
                ContextCompat.checkSelfPermission(
                        this,
                        Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED;

        boolean coarseGranted =
                ContextCompat.checkSelfPermission(
                        this,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED;

        boolean notificationGranted;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {

            notificationGranted =
                    ContextCompat.checkSelfPermission(
                            this,
                            Manifest.permission.POST_NOTIFICATIONS
                    ) == PackageManager.PERMISSION_GRANTED;

        } else {

            // אין צורך בהרשאת POST_NOTIFICATIONS בגרסאות אלה
            notificationGranted = true;
        }

        String locationStatus;

        if (fineGranted) {
            locationStatus = "מיקום מדויק: אושר";
        } else if (coarseGranted) {
            locationStatus = "מיקום משוער: אושר";
        } else {
            locationStatus = "מיקום: לא אושר";
        }

        String notificationStatus = notificationGranted
                ? "התראות: אושר"
                : "התראות: לא אושר";

        tvStatus.setText(
                locationStatus + "\n" + notificationStatus
        );
    }
    }

