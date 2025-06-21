package com.midterm.mobiledesignfinalterm.CarListing;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.midterm.mobiledesignfinalterm.R;

import java.util.Arrays;
import java.util.List;

public class FilterDialog extends Dialog {

    public interface FilterDialogListener {
        void onFiltersApplied(String brand, String vehicleType, String fuelType, Integer seats);
    }

    private FilterDialogListener listener;
    private String selectedBrand = null;
    private String selectedVehicleType = null;
    private String selectedFuelType = null;
    private Integer selectedSeats = null;

    private RecyclerView recyclerViewBrands;
    private RecyclerView recyclerViewVehicleTypes;
    private RecyclerView recyclerViewFuelTypes;
    private RecyclerView recyclerViewSeats;

    private FilterChipAdapter brandsAdapter;
    private FilterChipAdapter vehicleTypesAdapter;
    private FilterChipAdapter fuelTypesAdapter;
    private FilterChipAdapter seatsAdapter;

    private Button btnReset;
    private Button btnApply;

    // Lists of filter options
    private final List<String> brandOptions = Arrays.asList("All", "Toyota", "Honda", "Mazda", "Ford", "BMW", "Mercedes", "Audi", "Ferrari", "Lamborghini", "Porsche", "McLaren", "Bentley", "Aston Martin");
    private final List<String> vehicleTypeOptions = Arrays.asList("All", "Sedan", "SUV", "Pickup", "Supercar");
    private final List<String> fuelTypeOptions = Arrays.asList("All", "Gasoline", "Diesel", "Electric", "Hybrid");
    private final List<String> seatOptions = Arrays.asList("All", "2 Seats", "4 Seats", "5 Seats", "7+ Seats");

    public FilterDialog(Context context) {
        super(context);
    }

    public void setListener(FilterDialogListener listener) {
        this.listener = listener;
    }

    public void setInitialFilters(String brand, String vehicleType, String fuelType, Integer seats) {
        this.selectedBrand = brand;
        this.selectedVehicleType = vehicleType;
        this.selectedFuelType = fuelType;
        this.selectedSeats = seats;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_filter_carlisting);

        // Set dialog to take almost full width of the screen
        Window window = getWindow();
        if (window != null) {
            WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
            layoutParams.copyFrom(window.getAttributes());
            layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT;
            layoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
            window.setAttributes(layoutParams);
            window.setBackgroundDrawableResource(android.R.color.transparent);
        }

        // Initialize views
        initializeViews();

        // Set up RecyclerViews
        setupRecyclerViews();

        // Apply initial state if filters were previously selected
        applyInitialState();

        // Set up button listeners
        setupButtonListeners();
    }

    private void initializeViews() {
        recyclerViewBrands = findViewById(R.id.recyclerViewBrands);
        recyclerViewVehicleTypes = findViewById(R.id.recyclerViewVehicleTypes);
        recyclerViewFuelTypes = findViewById(R.id.recyclerViewFuelTypes);
        recyclerViewSeats = findViewById(R.id.recyclerViewSeats);
        btnReset = findViewById(R.id.btnReset);
        btnApply = findViewById(R.id.btnApply);
    }

    private void setupRecyclerViews() {
        // Brand RecyclerView setup
        GridLayoutManager brandsLayoutManager = new GridLayoutManager(getContext(), 3);
        recyclerViewBrands.setLayoutManager(brandsLayoutManager);
        brandsAdapter = new FilterChipAdapter(brandOptions, (position, value) -> selectedBrand = value);
        recyclerViewBrands.setAdapter(brandsAdapter);

        // Vehicle Type RecyclerView setup
        GridLayoutManager vehicleTypesLayoutManager = new GridLayoutManager(getContext(), 3);
        recyclerViewVehicleTypes.setLayoutManager(vehicleTypesLayoutManager);
        vehicleTypesAdapter = new FilterChipAdapter(vehicleTypeOptions, (position, value) -> {
            selectedVehicleType = value;
            if (value != null) {
                selectedVehicleType = value.toLowerCase(); // Convert to lowercase to match expected format
            }
        });
        recyclerViewVehicleTypes.setAdapter(vehicleTypesAdapter);

        // Fuel Type RecyclerView setup
        GridLayoutManager fuelTypesLayoutManager = new GridLayoutManager(getContext(), 3);
        recyclerViewFuelTypes.setLayoutManager(fuelTypesLayoutManager);
        fuelTypesAdapter = new FilterChipAdapter(fuelTypeOptions, (position, value) -> {
            selectedFuelType = value;
            if (value != null) {
                selectedFuelType = value.toLowerCase(); // Convert to lowercase to match expected format
            }
        });
        recyclerViewFuelTypes.setAdapter(fuelTypesAdapter);

        // Seats RecyclerView setup
        GridLayoutManager seatsLayoutManager = new GridLayoutManager(getContext(), 3);
        recyclerViewSeats.setLayoutManager(seatsLayoutManager);
        seatsAdapter = new FilterChipAdapter(seatOptions, (position, value) -> {
            // Convert seat text to Integer value
            if (position == 0) {
                selectedSeats = null; // "All"
            } else if (value != null) {
                if (value.startsWith("2")) {
                    selectedSeats = 2;
                } else if (value.startsWith("4")) {
                    selectedSeats = 4;
                } else if (value.startsWith("5")) {
                    selectedSeats = 5;
                } else if (value.startsWith("7")) {
                    selectedSeats = 7;
                }
            }
        });
        recyclerViewSeats.setAdapter(seatsAdapter);
    }

    private void applyInitialState() {
        // Brand selection
        if (selectedBrand != null) {
            int brandPosition = brandOptions.indexOf(selectedBrand);
            if (brandPosition != -1) {
                brandsAdapter.setSelectedPosition(brandPosition);
            }
        }

        // Vehicle type selection
        if (selectedVehicleType != null) {
            // Find the position case-insensitive
            String vehicleTypeCapitalized = selectedVehicleType.substring(0, 1).toUpperCase() + selectedVehicleType.substring(1);
            int typePosition = vehicleTypeOptions.indexOf(vehicleTypeCapitalized);
            if (typePosition != -1) {
                vehicleTypesAdapter.setSelectedPosition(typePosition);
            }
        }

        // Fuel type selection
        if (selectedFuelType != null) {
            // Find the position case-insensitive
            String fuelTypeCapitalized = selectedFuelType.substring(0, 1).toUpperCase() + selectedFuelType.substring(1);
            int fuelPosition = fuelTypeOptions.indexOf(fuelTypeCapitalized);
            if (fuelPosition != -1) {
                fuelTypesAdapter.setSelectedPosition(fuelPosition);
            }
        }

        // Seats selection
        if (selectedSeats != null) {
            int seatPosition = 0; // Default to "All"
            if (selectedSeats == 2) {
                seatPosition = 1;
            } else if (selectedSeats == 4) {
                seatPosition = 2;
            } else if (selectedSeats == 5) {
                seatPosition = 3;
            } else if (selectedSeats == 7) {
                seatPosition = 4;
            }
            seatsAdapter.setSelectedPosition(seatPosition);
        }
    }

    private void setupButtonListeners() {
        btnReset.setOnClickListener(v -> resetFilters());

        btnApply.setOnClickListener(v -> {
            if (listener != null) {
                // Call callback to activity with selected filters
                listener.onFiltersApplied(selectedBrand, selectedVehicleType, selectedFuelType, selectedSeats);
            }
            dismiss();
        });
    }

    private void resetFilters() {
        selectedBrand = null;
        selectedVehicleType = null;
        selectedFuelType = null;
        selectedSeats = null;

        // Reset all adapters to select the first item (All)
        brandsAdapter.setSelectedPosition(0);
        vehicleTypesAdapter.setSelectedPosition(0);
        fuelTypesAdapter.setSelectedPosition(0);
        seatsAdapter.setSelectedPosition(0);
    }
}
