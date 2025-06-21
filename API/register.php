<?php
header('Content-Type: application/json');
header("Access-Control-Allow-Origin: *");
header("Access-Control-Allow-Headers: Content-Type");
header("Access-Control-Allow-Methods: POST, OPTIONS");

// ... (PDO connection setup remains the same) ...
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
    echo json_encode(['success' => false, 'error' => 'Database connection failed: ' . $e->getMessage()]);
    exit;
}


if ($_SERVER['REQUEST_METHOD'] === 'POST') {
    $data = json_decode(file_get_contents("php://input"), true);

    if (!isset($data['phone_number']) || !isset($data['password']) || !isset($data['username'])) {
        http_response_code(400);
        echo json_encode(['success' => false, 'error' => 'Missing required fields: phone_number, password, or username']);
        exit;
    }

    // 1. Corrected: Check if account already exists using 'account_id'
    $checkStmt = $pdo->prepare("SELECT account_id FROM accounts WHERE phone_number = ?");
    $checkStmt->execute([$data['phone_number']]);
    if ($checkStmt->fetch()) {
        echo json_encode(['success' => false, 'error' => 'Phone number already exists']);
        exit;
    }

    $hashedPassword = password_hash($data['password'], PASSWORD_BCRYPT);

    $insertStmt = $pdo->prepare("INSERT INTO accounts (phone_number, username, password) VALUES (?, ?, ?)");
    
    try {
        $success = $insertStmt->execute([
            $data['phone_number'],
            $data['username'], 
            $hashedPassword
        ]);

        if ($success) {
            $lastId = $pdo->lastInsertId(); // This should return the generated value for 'account_id' if it's AUTO_INCREMENT
            
            // 2. Corrected: Fetch the newly registered user using 'account_id'
            // This is the query around line 46 that was causing the error.
            // Assuming the primary key column is 'account_id'.
            // The selected 'account_id' will be directly used.
            // 'name' is aliased to 'username' to match what route.js expects from data.user.username.
            $userStmt = $pdo->prepare("SELECT account_id, username, phone_number FROM accounts WHERE account_id = ?");
            $userStmt->execute([$lastId]);
            $newUser = $userStmt->fetch(PDO::FETCH_ASSOC);

            if ($newUser) {
                // Ensure the keys in $newUser ('account_id', 'username', 'phone_number')
                // match what your route.js authorize function expects for data.user.
                echo json_encode(['success' => true, 'user' => $newUser]);
            } else {
                http_response_code(500);
                echo json_encode(['success' => false, 'error' => 'Failed to retrieve user after registration. lastId was: ' . $lastId]);
            }
        } else {
            http_response_code(500);
            echo json_encode(['success' => false, 'error' => 'Registration failed at database insertion']);
        }
    } catch (PDOException $e) {
        http_response_code(500);
        echo json_encode(['success' => false, 'error' => 'Database error during registration: ' . $e->getMessage()]);
    }
} else {
    http_response_code(405);
    echo json_encode(['error' => 'Method not allowed']);
}

?>