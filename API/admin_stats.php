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

// Handle GET request for admin statistics
if ($_SERVER['REQUEST_METHOD'] === 'GET') {
    // Initialize default values
    $stats = [
        'total_bookings' => 0,
        'total_cars' => 0,
        'today_bookings' => 0,
        'week_bookings' => 0,
        'month_bookings' => 0,
        'cancel_rate' => 0.0,
        'success_rate' => 0.0
    ];

    try {
        // Get total bookings
        $stmt = $pdo->query("SELECT COUNT(*) FROM bookings");
        $stats['total_bookings'] = (int)$stmt->fetchColumn();
    } catch (Exception $e) {
        // Keep default value if table doesn't exist
    }

    try {
        // Get total cars
        $stmt = $pdo->query("SELECT COUNT(*) FROM vehicles");
        $stats['total_cars'] = (int)$stmt->fetchColumn();
    } catch (Exception $e) {
        // Keep default value if table doesn't exist
    }

    try {
        // Get today's bookings
        $stmt = $pdo->query("SELECT COUNT(*) FROM bookings WHERE DATE(created_at) = CURDATE()");
        $stats['today_bookings'] = (int)$stmt->fetchColumn();
    } catch (Exception $e) {
        // Try alternative date field if created_at doesn't exist
        try {
            $stmt = $pdo->query("SELECT COUNT(*) FROM bookings WHERE DATE(pickup_date) = CURDATE()");
            $stats['today_bookings'] = (int)$stmt->fetchColumn();
        } catch (Exception $e2) {
            // Keep default value
        }
    }

    try {
        // Get this week's bookings
        $stmt = $pdo->query("SELECT COUNT(*) FROM bookings WHERE YEARWEEK(created_at, 1) = YEARWEEK(CURDATE(), 1)");
        $stats['week_bookings'] = (int)$stmt->fetchColumn();
    } catch (Exception $e) {
        // Try alternative date field
        try {
            $stmt = $pdo->query("SELECT COUNT(*) FROM bookings WHERE YEARWEEK(pickup_date, 1) = YEARWEEK(CURDATE(), 1)");
            $stats['week_bookings'] = (int)$stmt->fetchColumn();
        } catch (Exception $e2) {
            // Keep default value
        }
    }

    try {
        // Get this month's bookings
        $stmt = $pdo->query("SELECT COUNT(*) FROM bookings WHERE YEAR(created_at) = YEAR(CURDATE()) AND MONTH(created_at) = MONTH(CURDATE())");
        $stats['month_bookings'] = (int)$stmt->fetchColumn();
    } catch (Exception $e) {
        // Try alternative date field
        try {
            $stmt = $pdo->query("SELECT COUNT(*) FROM bookings WHERE YEAR(pickup_date) = YEAR(CURDATE()) AND MONTH(pickup_date) = MONTH(CURDATE())");
            $stats['month_bookings'] = (int)$stmt->fetchColumn();
        } catch (Exception $e2) {
            // Keep default value
        }
    }

    try {
        // Calculate success and cancel rates
        $stmt = $pdo->query("SELECT status, COUNT(*) as count FROM bookings GROUP BY status");
        $statusCounts = $stmt->fetchAll();
        
        $totalBookings = 0;
        $cancelledCount = 0;
        $completedCount = 0;
        
        foreach ($statusCounts as $status) {
            $totalBookings += $status['count'];
            $statusName = strtolower($status['status']);
            
            if (in_array($statusName, ['cancelled', 'canceled', 'cancel'])) {
                $cancelledCount += $status['count'];
            } elseif (in_array($statusName, ['completed', 'finished', 'returned', 'complete', 'done'])) {
                $completedCount += $status['count'];
            }
        }
        
        if ($totalBookings > 0) {
            $stats['cancel_rate'] = round(($cancelledCount / $totalBookings) * 100, 1);
            $stats['success_rate'] = round(($completedCount / $totalBookings) * 100, 1);
        }
    } catch (Exception $e) {
        // Keep default rates
    }

    echo json_encode(['success' => true, 'stats' => $stats]);

} else {
    http_response_code(405);
    echo json_encode(['error' => 'Method not allowed']);
}
?>
            $cancelRate = 0.0;
            $successRate = 0.0;
        }

        $stats = [
            'total_bookings' => (int)$totalBookings,
            'total_cars' => (int)$totalCars,
            'today_bookings' => (int)$todayBookings,
            'week_bookings' => (int)$weekBookings,
            'month_bookings' => (int)$monthBookings,
            'cancel_rate' => round($cancelRate, 1),
            'success_rate' => round($successRate, 1)
        ];

        echo json_encode(['success' => true, 'stats' => $stats]);

    } catch (\PDOException $e) {
        // Return default values with error info for debugging
        $stats = [
            'total_bookings' => 0,
            'total_cars' => 0,
            'today_bookings' => 0,
            'week_bookings' => 0,
            'month_bookings' => 0,
            'cancel_rate' => 0.0,
            'success_rate' => 0.0,
            'debug_error' => $e->getMessage()
        ];

        echo json_encode(['success' => true, 'stats' => $stats]);
    } catch (Exception $e) {
        // Return default values with error info for debugging
        $stats = [
            'total_bookings' => 0,
            'total_cars' => 0,
            'today_bookings' => 0,
            'week_bookings' => 0,
            'month_bookings' => 0,
            'cancel_rate' => 0.0,
            'success_rate' => 0.0,
            'debug_error' => $e->getMessage()
        ];

        echo json_encode(['success' => true, 'stats' => $stats]);
    }
} else {
    http_response_code(405);
    echo json_encode(['error' => 'Method not allowed']);
}
?>
