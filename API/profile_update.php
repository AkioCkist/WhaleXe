<?php
header("Access-Control-Allow-Origin: *");
header("Access-Control-Allow-Headers: Content-Type, Authorization");
header("Access-Control-Allow-Methods: POST, OPTIONS");
header('Content-Type: application/json');

// ✅ Handle preflight requests early
if ($_SERVER['REQUEST_METHOD'] === 'OPTIONS') {
    http_response_code(200);
    exit;
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
    echo json_encode(['success' => false, 'error' => 'Database connection failed: ' . $e->getMessage()]);
    exit;
}

if ($_SERVER['REQUEST_METHOD'] === 'POST') {
    $data = json_decode(file_get_contents("php://input"), true);

    if (!isset($data['account_id']) || !isset($data['username']) || !isset($data['phone_number'])) {
        http_response_code(400);
        echo json_encode(['success' => false, 'error' => 'Missing required fields']);
        exit;
    }

    try {
        // Prepare update fields
        $updateFields = [
            'username' => $data['username'],
            'phone_number' => $data['phone_number']
        ];

        // Add password if provided
        if (isset($data['new_password']) && !empty($data['new_password'])) {
            $updateFields['password'] = password_hash($data['new_password'], PASSWORD_BCRYPT);
        }

        // Build SQL query dynamically
        $setPart = implode(", ", array_map(function ($key) {
            return "$key = :$key";
        }, array_keys($updateFields)));

        $updateFields['account_id'] = $data['account_id']; // Add to bind params

        $sql = "UPDATE accounts SET $setPart WHERE account_id = :account_id";
        $stmt = $pdo->prepare($sql);
        $stmt->execute($updateFields);

        // Fetch updated user
        $stmt = $pdo->prepare("SELECT account_id, username, phone_number FROM accounts WHERE account_id = ?");
        $stmt->execute([$data['account_id']]);
        $user = $stmt->fetch();

        echo json_encode([
            'success' => true,
            'message' => 'Profile updated successfully',
            'user' => $user
        ]);
    } catch (\Exception $e) {
        http_response_code(500);
        echo json_encode(['success' => false, 'error' => 'Error updating profile: ' . $e->getMessage()]);
    }
} else {
    http_response_code(405);
    echo json_encode(['success' => false, 'error' => 'Method not allowed']);
}
