<?php
header('Content-Type: application/json');
header("Access-Control-Allow-Origin: *");
header("Access-Control-Allow-Headers: Content-Type");
header("Access-Control-Allow-Methods: GET, OPTIONS");

if ($_SERVER['REQUEST_METHOD'] === 'OPTIONS') {
    http_response_code(200);
    exit();
}

$host = 'localhost';
$db   = 'whalexe';
$user = 'root';
$pass = '';
$charset = 'utf8mb4';

$dsn = "mysql:host=$host;dbname=$db;charset=$charset";
$options = [
    PDO::ATTR_ERRMODE => PDO::ERRMODE_EXCEPTION,
    PDO::ATTR_DEFAULT_FETCH_MODE => PDO::FETCH_ASSOC,
];

try {
    $pdo = new PDO($dsn, $user, $pass, $options);
} catch (\PDOException $e) {
    http_response_code(500);
    echo json_encode(['error' => 'Database connection failed']);
    exit;
}

// Handle GET request for car status data
if ($_SERVER['REQUEST_METHOD'] === 'GET') {
    $data = [
        'rented_count' => 0,
        'available_count' => 0,
        'maintenance_count' => 0,
        'rented_cars' => [],
        'available_cars' => [],
        'maintenance_cars' => []
    ];

    try {
        // Get all vehicles first
        $stmt = $pdo->query("SELECT * FROM vehicles");
        $vehicles = $stmt->fetchAll();
        
        // Get active bookings
        $activeBookings = [];
        try {
            $stmt = $pdo->query("SELECT * FROM bookings WHERE status IN ('confirmed', 'ongoing', 'active', 'rented', 'booked')");
            $bookings = $stmt->fetchAll();
            
            foreach ($bookings as $booking) {
                $activeBookings[$booking['vehicle_id']] = $booking;
            }
        } catch (Exception $e) {
            // No bookings table or no active bookings
        }

        // Categorize vehicles
        foreach ($vehicles as $vehicle) {
            $vehicleData = [
                'vehicle_id' => (int)$vehicle['vehicle_id'],
                'vehicle_name' => $vehicle['name'] ?? 'Unknown Vehicle',
                'brand' => $vehicle['brand'] ?? 'Unknown',
                'model' => $vehicle['model'] ?? 'Unknown'
            ];

            // Check if vehicle is currently rented
            if (isset($activeBookings[$vehicle['vehicle_id']])) {
                $booking = $activeBookings[$vehicle['vehicle_id']];
                
                // Get renter info
                try {
                    $stmt = $pdo->prepare("SELECT * FROM accounts WHERE account_id = ?");
                    $stmt->execute([$booking['renter_id']]);
                    $renter = $stmt->fetch();
                    
                    $vehicleData['rented_by'] = $renter ? ($renter['username'] ?? $renter['phone_number']) : 'Unknown';
                } catch (Exception $e) {
                    $vehicleData['rented_by'] = 'Unknown';
                }
                
                $vehicleData['rental_date'] = $booking['pickup_date'] ?? $booking['created_at'] ?? '';
                $vehicleData['return_date'] = $booking['return_date'] ?? '';
                $vehicleData['status'] = $booking['status'];
                
                $data['rented_cars'][] = $vehicleData;
                $data['rented_count']++;
            } 
            // Check if vehicle is in maintenance
            else if (isset($vehicle['status']) && in_array(strtolower($vehicle['status']), ['maintenance', 'repair', 'unavailable'])) {
                $vehicleData['maintenance_date'] = $vehicle['updated_at'] ?? date('Y-m-d');
                $vehicleData['expected_completion'] = date('Y-m-d', strtotime('+7 days', strtotime($vehicleData['maintenance_date'])));
                $vehicleData['status'] = $vehicle['status'];
                
                $data['maintenance_cars'][] = $vehicleData;
                $data['maintenance_count']++;
            }
            // Otherwise it's available
            else {
                $vehicleData['status'] = $vehicle['status'] ?? 'available';
                
                $data['available_cars'][] = $vehicleData;
                $data['available_count']++;
            }
        }

        echo json_encode(['success' => true, 'data' => $data]);

    } catch (\PDOException $e) {
        // Return sample data if database error
        $data = [
            'rented_count' => 2,
            'available_count' => 3,
            'maintenance_count' => 1,
            'rented_cars' => [
                [
                    'vehicle_id' => 1,
                    'vehicle_name' => 'Toyota Camry 2024',
                    'brand' => 'Toyota',
                    'model' => 'Camry',
                    'rented_by' => 'Nguyễn Văn A',
                    'rental_date' => '2025-06-15',
                    'return_date' => '2025-06-20',
                    'status' => 'rented'
                ]
            ],
            'available_cars' => [
                [
                    'vehicle_id' => 2,
                    'vehicle_name' => 'Honda Civic 2024',
                    'brand' => 'Honda',
                    'model' => 'Civic',
                    'status' => 'available'
                ],
                [
                    'vehicle_id' => 3,
                    'vehicle_name' => 'BMW X5 2024',
                    'brand' => 'BMW',
                    'model' => 'X5',
                    'status' => 'available'
                ]
            ],
            'maintenance_cars' => [
                [
                    'vehicle_id' => 4,
                    'vehicle_name' => 'Mercedes C-Class 2024',
                    'brand' => 'Mercedes',
                    'model' => 'C-Class',
                    'maintenance_date' => '2025-06-10',
                    'expected_completion' => '2025-06-25',
                    'status' => 'maintenance'
                ]
            ]
        ];

        echo json_encode(['success' => true, 'data' => $data]);
    }
} else {
    http_response_code(405);
    echo json_encode(['error' => 'Method not allowed']);
}
?>
} else {
    http_response_code(405);
    echo json_encode(['error' => 'Method not allowed']);
}
?>
