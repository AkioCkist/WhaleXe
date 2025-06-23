package com.midterm.mobiledesignfinalterm.CarListing;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentActivity;

import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.datepicker.CalendarConstraints;
import com.google.android.material.datepicker.DateValidatorPointForward;
import com.google.android.material.timepicker.MaterialTimePicker;
import com.google.android.material.timepicker.TimeFormat;
import com.midterm.mobiledesignfinalterm.R;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class DateTimeLocationDialog extends Dialog {

    private static final String TAG = "DateTimeLocationDialog";
    private Spinner spinnerPickupLocation, spinnerReturnLocation;
    private Button btnPickupTime, btnReturnTime, btnCancel, btnConfirm;
    private Calendar pickupCalendar, returnCalendar;
    private SimpleDateFormat dateTimeFormatter;
    private OnDateTimeLocationSelectedListener listener;
    private FragmentActivity fragmentActivity;

    private String pickupLocation;
    private String returnLocation;
    private Calendar selectedPickupDateTime;
    private Calendar selectedReturnDateTime;

    // Store the locations list and adapters as instance variables
    private List<String> locations;
    private ArrayAdapter<String> pickupAdapter;
    private ArrayAdapter<String> returnAdapter;

    // Map to store city name variants
    private Map<String, Integer> cityNameMap = new HashMap<>();

    public interface OnDateTimeLocationSelectedListener {
        void onDateTimeLocationSelected(String pickupLocation, String returnLocation,
                                        Calendar pickupDateTime, Calendar returnDateTime);
    }

    public DateTimeLocationDialog(@NonNull Context context) {
        super(context);
        initializeDefaults();
        if (context instanceof FragmentActivity) {
            fragmentActivity = (FragmentActivity) context;
        }
    }

    public DateTimeLocationDialog(@NonNull FragmentActivity activity) {
        super(activity);
        this.fragmentActivity = activity;
        initializeDefaults();
    }

    private void initializeDefaults() {
        pickupCalendar = Calendar.getInstance();
        returnCalendar = Calendar.getInstance();
        returnCalendar.add(Calendar.HOUR_OF_DAY, 2);
        dateTimeFormatter = new SimpleDateFormat("HH:mm dd/MM/yyyy", Locale.getDefault());

        // Initialize locations list
        locations = new ArrayList<>();
        locations.add("Hà Nội");
        locations.add("Đà Nẵng");
        locations.add("TP.HCM");

        initCityNameMap();
    }

    public void setInitialValues(String pickupLocation, String returnLocation,
                                 Calendar pickupDateTime, Calendar returnDateTime) {
        Log.d(TAG, "Setting initial values - Pickup: " + pickupLocation + ", Return: " + returnLocation);

        this.pickupLocation = pickupLocation;
        this.returnLocation = returnLocation;

        if (pickupDateTime != null) {
            this.pickupCalendar = (Calendar) pickupDateTime.clone();
            Log.d(TAG, "Pickup DateTime: " + dateTimeFormatter.format(pickupDateTime.getTime()));
        }

        if (returnDateTime != null) {
            this.returnCalendar = (Calendar) returnDateTime.clone();
            Log.d(TAG, "Return DateTime: " + dateTimeFormatter.format(returnDateTime.getTime()));
        }
    }

    public void setListener(OnDateTimeLocationSelectedListener listener) {
        this.listener = listener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_datetime_location);

        setupWindow();
        initializeViews();
        setupLocationSpinners();
        updateTimeButtonTexts();
        setupButtonListeners();
    }

    private void setupWindow() {
        Window window = getWindow();
        if (window != null) {
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
            layoutParams.copyFrom(window.getAttributes());
            layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT;
            layoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
            window.setAttributes(layoutParams);
        }
    }

    private void initializeViews() {
        spinnerPickupLocation = findViewById(R.id.spinnerPickupLocation);
        spinnerReturnLocation = findViewById(R.id.spinnerReturnLocation);
        btnPickupTime = findViewById(R.id.btnPickupTime);
        btnReturnTime = findViewById(R.id.btnReturnTime);
        btnCancel = findViewById(R.id.btnCancel);
        btnConfirm = findViewById(R.id.btnConfirm);
    }

    private void setupLocationSpinners() {
        try {
            // A custom adapter that ensures the text color is black for both the
            // selected item (getView) and the dropdown list items (getDropDownView).
            ArrayAdapter<String> customAdapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_spinner_item, locations) {
                @NonNull
                @Override
                public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                    // This is for the currently selected item
                    View view = super.getView(position, convertView, parent);
                    if (view instanceof TextView) {
                        ((TextView) view).setTextColor(Color.BLACK);
                    }
                    return view;
                }

                @Override
                public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                    // This is for the items in the dropdown list
                    View view = super.getDropDownView(position, convertView, parent);
                    if (view instanceof TextView) {
                        ((TextView) view).setTextColor(Color.BLACK);
                    }
                    return view;
                }
            };

            // Use the standard dropdown item layout, the color will be overridden above.
            customAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

            // Set adapters
            spinnerPickupLocation.setAdapter(customAdapter);
            spinnerReturnLocation.setAdapter(customAdapter);


            Log.d(TAG, "Adapters set successfully");
            Log.d(TAG, "DEBUG: Trying to match pickupLocation: '" + pickupLocation + "'");
            Log.d(TAG, "DEBUG: Trying to match returnLocation: '" + returnLocation + "'");
            Log.d(TAG, "DEBUG: Available locations: " + locations.toString());

            // Set initial selections with a delay to ensure adapters are ready
            new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                @Override
                public void run() {
                    setInitialSelections();
                }
            }, 200);

        } catch (Exception e) {
            Log.e(TAG, "Error setting up location spinners: " + e.getMessage(), e);
        }
    }


    private void setInitialSelections() {
        try {
            // Set pickup location
            if (pickupLocation != null && !pickupLocation.trim().isEmpty()) {
                int pickupIndex = findMatchingCityIndex(pickupLocation.trim());
                Log.d(TAG, "Found pickup index: " + pickupIndex + " for location: '" + pickupLocation + "'");
                if (pickupIndex >= 0 && pickupIndex < locations.size()) {
                    spinnerPickupLocation.setSelection(pickupIndex);
                    Log.d(TAG, "Set pickup location selection to index " + pickupIndex + ": " + locations.get(pickupIndex));
                } else {
                    Log.w(TAG, "Could not find matching city for pickup: '" + pickupLocation + "'");
                    // Set to first item as default
                    spinnerPickupLocation.setSelection(0);
                }
            }

            // Set return location
            if (returnLocation != null && !returnLocation.trim().isEmpty()) {
                int returnIndex = findMatchingCityIndex(returnLocation.trim());
                Log.d(TAG, "Found return index: " + returnIndex + " for location: '" + returnLocation + "'");
                if (returnIndex >= 0 && returnIndex < locations.size()) {
                    spinnerReturnLocation.setSelection(returnIndex);
                    Log.d(TAG, "Set return location selection to index " + returnIndex + ": " + locations.get(returnIndex));
                } else {
                    Log.w(TAG, "Could not find matching city for return: '" + returnLocation + "'");
                    // Set to first item as default
                    spinnerReturnLocation.setSelection(0);
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error setting initial selections: " + e.getMessage(), e);
        }
    }

    private void initCityNameMap() {
        // Clear the map first
        cityNameMap.clear();

        // Map common variations of city names to their indices in the locations list
        // Hà Nội variations (index 0)
        cityNameMap.put("hà nội", 0);
        cityNameMap.put("ha noi", 0);
        cityNameMap.put("hanoi", 0);
        cityNameMap.put("hn", 0);
        cityNameMap.put("Hà Nội", 0);
        cityNameMap.put("Ha Noi", 0);
        cityNameMap.put("HANOI", 0);

        // Đà Nẵng variations (index 1)
        cityNameMap.put("đà nẵng", 1);
        cityNameMap.put("da nang", 1);
        cityNameMap.put("danang", 1);
        cityNameMap.put("dn", 1);
        cityNameMap.put("Đà Nẵng", 1);
        cityNameMap.put("Da Nang", 1);
        cityNameMap.put("DANANG", 1);

        // TP.HCM variations (index 2)
        cityNameMap.put("tp. hồ chí minh", 2);
        cityNameMap.put("tp hồ chí minh", 2);
        cityNameMap.put("hồ chí minh", 2);
        cityNameMap.put("ho chi minh", 2);
        cityNameMap.put("tp.hcm", 2);
        cityNameMap.put("tphcm", 2);
        cityNameMap.put("hcm", 2);
        cityNameMap.put("sài gòn", 2);
        cityNameMap.put("sai gon", 2);
        cityNameMap.put("saigon", 2);
        cityNameMap.put("tp. hcm", 2);
        cityNameMap.put("TP.HCM", 2);
        cityNameMap.put("TP HCM", 2);
        cityNameMap.put("Ho Chi Minh", 2);
        cityNameMap.put("Sai Gon", 2);
        cityNameMap.put("SAIGON", 2);

        Log.d(TAG, "City name map initialized with " + cityNameMap.size() + " entries");

        // Log some key mappings for debugging
        for (Map.Entry<String, Integer> entry : cityNameMap.entrySet()) {
            Log.d(TAG, "City mapping: '" + entry.getKey() + "' -> " + entry.getValue());
        }
    }

    private int findMatchingCityIndex(String cityName) {
        if (cityName == null || cityName.trim().isEmpty()) {
            Log.d(TAG, "City name is null or empty");
            return -1;
        }

        String normalizedCityName = cityName.trim();
        Log.d(TAG, "Looking for city: '" + normalizedCityName + "'");

        // Try exact match first (case sensitive)
        for (int i = 0; i < locations.size(); i++) {
            if (locations.get(i).equals(normalizedCityName)) {
                Log.d(TAG, "Found exact match at index " + i);
                return i;
            }
        }

        // Try exact match case insensitive
        for (int i = 0; i < locations.size(); i++) {
            if (locations.get(i).toLowerCase().equals(normalizedCityName.toLowerCase())) {
                Log.d(TAG, "Found case-insensitive match at index " + i);
                return i;
            }
        }

        // Try using the map for common variations
        Integer index = cityNameMap.get(normalizedCityName);
        if (index != null && index >= 0 && index < locations.size()) {
            Log.d(TAG, "Found mapping match at index " + index);
            return index;
        }

        // Try case insensitive map lookup
        index = cityNameMap.get(normalizedCityName.toLowerCase());
        if (index != null && index >= 0 && index < locations.size()) {
            Log.d(TAG, "Found case-insensitive mapping match at index " + index);
            return index;
        }

        // Try partial matches as a fallback
        for (int i = 0; i < locations.size(); i++) {
            String location = locations.get(i).toLowerCase();
            String searchTerm = normalizedCityName.toLowerCase();
            if (location.contains(searchTerm) || searchTerm.contains(location)) {
                Log.d(TAG, "Found partial match at index " + i);
                return i;
            }
        }

        Log.d(TAG, "No match found for: '" + normalizedCityName + "'");
        return -1;
    }

    private void updateTimeButtonTexts() {
        updatePickupTimeButtonText();
        updateReturnTimeButtonText();
    }

    private void updatePickupTimeButtonText() {
        if (pickupCalendar != null) {
            btnPickupTime.setText(dateTimeFormatter.format(pickupCalendar.getTime()));
        } else {
            Calendar defaultCalendar = Calendar.getInstance();
            btnPickupTime.setText(dateTimeFormatter.format(defaultCalendar.getTime()));
        }
    }

    private void updateReturnTimeButtonText() {
        if (returnCalendar != null) {
            btnReturnTime.setText(dateTimeFormatter.format(returnCalendar.getTime()));
        } else {
            Calendar defaultCalendar = Calendar.getInstance();
            defaultCalendar.add(Calendar.HOUR_OF_DAY, 2);
            btnReturnTime.setText(dateTimeFormatter.format(defaultCalendar.getTime()));
        }
    }

    private void setupButtonListeners() {
        btnPickupTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDateTimePicker(pickupCalendar, true);
            }
        });

        btnReturnTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDateTimePicker(returnCalendar, false);
            }
        });

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        btnConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (validateSelections()) {
                    if (listener != null) {
                        String pickupLoc = spinnerPickupLocation.getSelectedItem().toString();
                        String returnLoc = spinnerReturnLocation.getSelectedItem().toString();
                        listener.onDateTimeLocationSelected(pickupLoc, returnLoc, pickupCalendar, returnCalendar);
                    }
                    dismiss();
                }
            }
        });
    }

    private void showDateTimePicker(final Calendar calendar, final boolean isPickup) {
        if (fragmentActivity == null) {
            if (getContext() instanceof FragmentActivity) {
                fragmentActivity = (FragmentActivity) getContext();
            } else {
                Toast.makeText(getContext(),
                        "Unable to show date picker. Please try again.",
                        Toast.LENGTH_SHORT).show();
                return;
            }
        }

        Calendar minDate = Calendar.getInstance();

        MaterialDatePicker.Builder<Long> builder = MaterialDatePicker.Builder.datePicker();
        builder.setTitleText(isPickup ? "Select Pickup Date" : "Select Return Date");
        builder.setSelection(calendar.getTimeInMillis());
        builder.setCalendarConstraints(new CalendarConstraints.Builder()
                .setValidator(DateValidatorPointForward.from(minDate.getTimeInMillis()))
                .build());

        builder.setTheme(R.style.CustomMaterialCalendar);

        MaterialDatePicker<Long> datePicker = builder.build();

        datePicker.addOnPositiveButtonClickListener(selection -> {
            calendar.setTimeInMillis(selection);
            showTimePicker(calendar, isPickup);
        });

        datePicker.show(fragmentActivity.getSupportFragmentManager(), "DATE_PICKER");
    }

    private void showTimePicker(final Calendar calendar, final boolean isPickup) {
        if (fragmentActivity == null) {
            if (getContext() instanceof FragmentActivity) {
                fragmentActivity = (FragmentActivity) getContext();
            } else {
                Toast.makeText(getContext(),
                        "Unable to show time picker. Please try again.",
                        Toast.LENGTH_SHORT).show();
                return;
            }
        }

        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        MaterialTimePicker.Builder builder = new MaterialTimePicker.Builder()
                .setTimeFormat(TimeFormat.CLOCK_24H)
                .setHour(hour)
                .setMinute(minute)
                .setTitleText(isPickup ? "Select Pickup Time" : "Select Return Time")
                .setTheme(R.style.CustomMaterialTimePicker);

        final MaterialTimePicker timePicker = builder.build();

        timePicker.addOnPositiveButtonClickListener(view -> {
            calendar.set(Calendar.HOUR_OF_DAY, timePicker.getHour());
            calendar.set(Calendar.MINUTE, timePicker.getMinute());

            if (isPickup) {
                updatePickupTimeButtonText();

                if (isSameDay(pickupCalendar, returnCalendar) &&
                        pickupCalendar.after(returnCalendar)) {

                    returnCalendar.setTimeInMillis(pickupCalendar.getTimeInMillis());
                    returnCalendar.add(Calendar.HOUR_OF_DAY, 1);
                    updateReturnTimeButtonText();
                    Toast.makeText(getContext(),
                            "Return time adjusted to be after pickup time",
                            Toast.LENGTH_SHORT).show();
                }
            } else {
                updateReturnTimeButtonText();
            }
        });

        timePicker.show(fragmentActivity.getSupportFragmentManager(), "TIME_PICKER");
    }

    private boolean isSameDay(Calendar cal1, Calendar cal2) {
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR);
    }

    private boolean validateSelections() {
        if (returnCalendar.before(pickupCalendar)) {
            Toast.makeText(getContext(), "Thời gian trả xe phải sau thời gian nhận xe", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }
}