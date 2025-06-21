<?php
header('Content-Type: application/json');
header("Access-Control-Allow-Origin: *");
header("Access-Control-Allow-Headers: Content-Type");
header("Access-Control-Allow-Methods: POST, OPTIONS");

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

// Handle forgot password verification POST
if ($_SERVER['REQUEST_METHOD'] === 'POST') {
    // Get raw POST data
    $raw_data = file_get_contents("php://input");
    $data = json_decode($raw_data, true);
    
    // Debug: Log what we received
    error_log("Raw POST data: " . $raw_data);
    error_log("Decoded data: " . print_r($data, true));
    
    // Check if JSON decoding failed
    if (json_last_error() !== JSON_ERROR_NONE) {
        http_response_code(400);
        echo json_encode([
            'success' => false,
            'error' => 'Invalid JSON data: ' . json_last_error_msg(),
            'raw_data' => $raw_data
        ]);
        exit;
    }
    
    // Check if data is null or empty
    if (!$data) {
        http_response_code(400);
        echo json_encode([
            'success' => false,
            'error' => 'No data received',
            'raw_data' => $raw_data
        ]);
        exit;
    }

    // Check for the fields that are actually being sent by your Android app
    if (!isset($data['username']) || !isset($data['phone_number'])) {
        http_response_code(400);
        echo json_encode([
            'success' => false,
            'error' => 'Missing required fields: username and phone_number',
            'received_fields' => array_keys($data),
            'received_data' => $data
        ]);
        exit;
    }

    // Clean input data
    $username = trim($data['username']);
    $phone_number = trim($data['phone_number']);

    // Validate phone number format (basic validation)
    if (!preg_match('/^[+]?[0-9]{10,13}$/', $phone_number)) {
        http_response_code(400);
        echo json_encode([
            'success' => false,
            'error' => 'Invalid phone number format'
        ]);
        exit;
    }

    try {
        // Check if account exists with matching username and phone number
        // Using the correct column names from your database structure
        $stmt = $pdo->prepare("SELECT account_id, username, phone_number FROM accounts WHERE username = ? AND phone_number = ?");
        $stmt->execute([$username, $phone_number]);
        $user = $stmt->fetch();

        if ($user) {
            // Account found - verification successful
            echo json_encode([
                'success' => true, 
                'message' => 'Account verified successfully',
                'user_id' => $user['account_id'],
                'username' => $user['username'],
                'phone_number' => $user['phone_number']
            ]);
        } else {
            // Account not found
            echo json_encode([
                'success' => false, 
                'error' => 'Account name or mobile number not found'
            ]);
        }

    } catch (\PDOException $e) {
        http_response_code(500);
        echo json_encode([
            'success' => false,
            'error' => 'Database query failed: ' . $e->getMessage()
        ]);
    }

} else {
    http_response_code(405);
    echo json_encode([
        'success' => false,
        'error' => 'Method not allowed'
    ]);
}
?>