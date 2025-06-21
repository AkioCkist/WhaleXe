package com.midterm.mobiledesignfinalterm.BookingCar;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.util.Random;

import com.midterm.mobiledesignfinalterm.MainActivity;
import com.midterm.mobiledesignfinalterm.R;
public class CheckoutActivity extends AppCompatActivity {

    private TextView tvPickupDetails, tvDropoffDetails, tvUserDetails, tvTotalAmount;
    private RadioGroup rgPaymentMethod;
    private Button btnConfirmBooking;
    private ImageView ivBack;

    private String pickupLocation, dropoffLocation, pickupDate, pickupTime, dropoffDate, dropoffTime;
    private String fullName, email, phone, aadharNumber, panNumber;
    private String userId, userName, userPhone;
    private double totalAmount = 80.00;
    private String carPriceStr;
    private double carPriceRaw = 0;
    private String carId = ""; // Added field to store car ID
    private String carName = ""; // Added field to store car name for better tracking

    private static final String TAG = "BookingFlow"; // Added tag for consistent logging

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_checkout);

        getIntentData();
        initViews();
        setupClickListeners();
        displayBookingDetails();
    }

    private void getIntentData() {
        Intent intent = getIntent();
        pickupLocation = intent.getStringExtra("pickup_location");
        dropoffLocation = intent.getStringExtra("dropoff_location");
        pickupDate = intent.getStringExtra("pickup_date");
        pickupTime = intent.getStringExtra("pickup_time");
        dropoffDate = intent.getStringExtra("dropoff_date");
        dropoffTime = intent.getStringExtra("dropoff_time");
        fullName = intent.getStringExtra("full_name");
        email = intent.getStringExtra("email");
        phone = intent.getStringExtra("phone");
        aadharNumber = intent.getStringExtra("citizen_id");
        panNumber = intent.getStringExtra("tax_id");
        userId = intent.getStringExtra("user_id");
        userName = intent.getStringExtra("user_name");
        userPhone = intent.getStringExtra("user_phone");
        carPriceStr = intent.getStringExtra("car_price");
        // Ưu tiên lấy car_price_raw từ intent, nếu không có thì mới lấy carPriceStr
        if (intent.hasExtra("car_price_raw")) {
            carPriceRaw = intent.getDoubleExtra("car_price_raw", -1);
        } else {
            carPriceRaw = -1;
        }
        carId = intent.getStringExtra("car_id"); // Get car ID from intent
        carName = intent.getStringExtra("car_name"); // Get car name from intent
        calculateTotalAmount();
    }

    private void calculateTotalAmount() {
        // Get car price
        double carPrice = 0;
        if (carPriceRaw > 0) {
            carPrice = carPriceRaw;
        } else if (carPriceStr != null) {
            try {
                String cleanPrice = carPriceStr.replaceAll("[^\\d.]", "");
                carPrice = Double.parseDouble(cleanPrice);
            } catch (Exception e) {
                carPrice = 0;
            }
        }

        // Calculate the total rental duration including time
        double rentalDuration = calculateRentalDuration(pickupDate, pickupTime, dropoffDate, dropoffTime);

        // Calculate the total amount based on the car price and rental duration
        totalAmount = carPrice * rentalDuration;
    }

    /**
     * Calculate the rental duration in days, including time
     * Even a partial day counts as a full day
     */
    private double calculateRentalDuration(String pickupDate, String pickupTime, String dropoffDate, String dropoffTime) {
        try {
            // Log input dates and times
            android.util.Log.d("CarRental", "Input dates - Pickup: " + pickupDate + " " + pickupTime +
                               ", Dropoff: " + dropoffDate + " " + dropoffTime);

            // Detect date format
            java.text.SimpleDateFormat dateFormatSlash = new java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault());
            java.text.SimpleDateFormat dateFormatMonthName = new java.text.SimpleDateFormat("dd MMMM yyyy", java.util.Locale.getDefault());
            java.text.SimpleDateFormat timeFormat = new java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault());

            // Parse the pickup and dropoff dates with appropriate format
            java.util.Date pickupDateObj, dropoffDateObj;

            try {
                // First try dd/MM/yyyy format
                pickupDateObj = dateFormatSlash.parse(pickupDate);
                dropoffDateObj = dateFormatSlash.parse(dropoffDate);
            } catch (java.text.ParseException e) {
                // If that fails, try dd MMMM yyyy format
                try {
                    pickupDateObj = dateFormatMonthName.parse(pickupDate);
                    dropoffDateObj = dateFormatMonthName.parse(dropoffDate);
                } catch (java.text.ParseException e2) {
                    // Log the error and rethrow
                    android.util.Log.e("CarRental", "Failed to parse dates with both formats", e2);
                    throw e2;
                }
            }

            // Log parsed date objects
            android.util.Log.d("CarRental", "Parsed dates - Pickup: " + pickupDateObj +
                               ", Dropoff: " + dropoffDateObj);

            // Create calendar instances for accurate date comparison
            java.util.Calendar pickupCal = java.util.Calendar.getInstance();
            pickupCal.setTime(pickupDateObj);
            pickupCal.set(java.util.Calendar.HOUR_OF_DAY, 0);
            pickupCal.set(java.util.Calendar.MINUTE, 0);
            pickupCal.set(java.util.Calendar.SECOND, 0);
            pickupCal.set(java.util.Calendar.MILLISECOND, 0);

            java.util.Calendar dropoffCal = java.util.Calendar.getInstance();
            dropoffCal.setTime(dropoffDateObj);
            dropoffCal.set(java.util.Calendar.HOUR_OF_DAY, 23);
            dropoffCal.set(java.util.Calendar.MINUTE, 59);
            dropoffCal.set(java.util.Calendar.SECOND, 59);
            dropoffCal.set(java.util.Calendar.MILLISECOND, 999);

            // Log calendar objects after setting time
            android.util.Log.d("CarRental", "Calendar objects - Pickup: " +
                              pickupCal.getTime() + " (" + pickupCal.getTimeInMillis() + ")" +
                              ", Dropoff: " + dropoffCal.getTime() + " (" + dropoffCal.getTimeInMillis() + ")");

            // Simple calculation of days difference
            long dayDiff = (dropoffCal.getTimeInMillis() - pickupCal.getTimeInMillis()) / (24 * 60 * 60 * 1000);
            android.util.Log.d("CarRental", "Simple day difference calculation: " + dayDiff);

            // Calculate days by counting each day
            int days = 0;
            java.util.Calendar tempCal = (java.util.Calendar) pickupCal.clone();

            StringBuilder dayLog = new StringBuilder("Days counted: ");
            while (tempCal.getTimeInMillis() <= dropoffCal.getTimeInMillis()) {
                dayLog.append(tempCal.get(java.util.Calendar.DAY_OF_MONTH)).append("/")
                      .append(tempCal.get(java.util.Calendar.MONTH) + 1).append(" ");
                days++;
                tempCal.add(java.util.Calendar.DATE, 1);
            }
            android.util.Log.d("CarRental", dayLog.toString());
            android.util.Log.d("CarRental", "Final days count: " + days);

            // Ensure at least one day
            days = Math.max(days, 1);
            android.util.Log.d("CarRental", "Final rental days (after min check): " + days);

            return days;
        } catch (Exception e) {
            android.util.Log.e("CarRental", "Error calculating rental days", e);
            e.printStackTrace();
            // Default to 1 day if there's an error
            return 1;
        }
    }

    private int getRentalDays(String startDate, String endDate) {
        try {
            // Create a SimpleDateFormat with the correct pattern "dd/MM/yyyy"
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd/MM/yyyy");

            // Parse the dates using the correct format
            java.util.Date d1 = sdf.parse(startDate);
            java.util.Date d2 = sdf.parse(endDate);

            // Calculate difference in milliseconds
            long diff = d2.getTime() - d1.getTime();

            // Convert to days and round up (even partial days count as full days)
            int days = (int) Math.ceil(diff / (1000.0 * 60 * 60 * 24));

            // Return at least 1 day, even if pickup and return are on the same day
            return Math.max(days, 1);
        } catch (Exception e) {
            e.printStackTrace();
            return 1; // Default to 1 day if there's an error
        }
    }

    private void initViews() {
        tvPickupDetails = findViewById(R.id.tv_pickup_details);
        tvDropoffDetails = findViewById(R.id.tv_dropoff_details);
        tvUserDetails = findViewById(R.id.tv_user_details);
        tvTotalAmount = findViewById(R.id.tv_total_amount);
        rgPaymentMethod = findViewById(R.id.rg_payment_method);
        btnConfirmBooking = findViewById(R.id.btn_confirm_booking);
        ivBack = findViewById(R.id.iv_back);
    }

    private void setupClickListeners() {
        ivBack.setOnClickListener(v -> finish());

        btnConfirmBooking.setOnClickListener(v -> {
            int selectedPaymentId = rgPaymentMethod.getCheckedRadioButtonId();
            if (selectedPaymentId == -1) {
                Toast.makeText(this, "Please select a payment method", Toast.LENGTH_SHORT).show();
                return;
            }

            RadioButton selectedPayment = findViewById(selectedPaymentId);
            String paymentMethod = selectedPayment.getText().toString();

            // Generate booking ID
            String bookingId = generateBookingId();

            Intent intent = new Intent(CheckoutActivity.this, ThankYouActivity.class);
            intent.putExtra("booking_id", bookingId);
            intent.putExtra("pickup_location", pickupLocation);
            intent.putExtra("dropoff_location", dropoffLocation);
            intent.putExtra("pickup_date", pickupDate);
            intent.putExtra("pickup_time", pickupTime);
            intent.putExtra("dropoff_date", dropoffDate);
            intent.putExtra("dropoff_time", dropoffTime);
            intent.putExtra("full_name", fullName);
            intent.putExtra("email", email);
            intent.putExtra("phone", phone);
            intent.putExtra("citizen_id", aadharNumber);
            intent.putExtra("tax_id", panNumber);
            intent.putExtra("payment_method", paymentMethod);
            intent.putExtra("total_amount", String.valueOf(totalAmount));
            // Pass user data to ThankYouActivity
            intent.putExtra("user_id", userId);
            intent.putExtra("user_name", userName);
            intent.putExtra("user_phone", userPhone);
            // Pass car ID and name to ThankYouActivity
            intent.putExtra("car_id", carId);
            intent.putExtra("car_name", carName);
            startActivity(intent);
            finish();
        });
    }

    private void displayBookingDetails() {
        tvPickupDetails.setText(String.format("Pickup: %s\n%s at %s",
                pickupLocation, pickupDate, pickupTime));

        tvDropoffDetails.setText(String.format("Drop-off: %s\n%s at %s",
                dropoffLocation, dropoffDate, dropoffTime));

        tvUserDetails.setText(String.format("Name: %s\nEmail: %s\nPhone: %s",
                fullName, email, phone));

        // Calculate rental days for display
        int rentalDays = (int) calculateRentalDuration(pickupDate, pickupTime, dropoffDate, dropoffTime);

        // Get the daily car price
        double dailyPrice = 0;
        if (carPriceRaw > 0) {
            dailyPrice = carPriceRaw;
        } else if (carPriceStr != null) {
            try {
                String cleanPrice = carPriceStr.replaceAll("[^\\d.]", "");
                dailyPrice = Double.parseDouble(cleanPrice);
            } catch (Exception e) {
                dailyPrice = 0;
            }
        }

        // Display the detailed cost breakdown
        String priceDetails = String.format("%s x %d day(s) = %s",
                formatCurrencyVND(dailyPrice),
                rentalDays,
                formatCurrencyVND(totalAmount));

        tvTotalAmount.setText(priceDetails);
    }

    private String formatCurrencyVND(double amount) {
        java.text.NumberFormat formatter = java.text.NumberFormat.getInstance(new java.util.Locale("vi", "VN"));
        return formatter.format(amount) + " VND";
    }

    private String generateBookingId() {
        Random random = new Random();
        return "BK" + String.format("%06d", random.nextInt(1000000));
    }
}
