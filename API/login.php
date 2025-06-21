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

// Handle login POST
if ($_SERVER['REQUEST_METHOD'] === 'POST') {
    $data = json_decode(file_get_contents("php://input"), true);

    if (!isset($data['phone_number']) || !isset($data['password'])) {
        http_response_code(400);
        echo json_encode(['error' => 'Missing required fields']);
        exit;
    }

    // Lấy user theo số điện thoại
    $stmt = $pdo->prepare("SELECT * FROM accounts WHERE phone_number = ?");
    $stmt->execute([$data['phone_number']]);
    $user = $stmt->fetch();

    if ($user && password_verify($data['password'], $user['password'])) {
        // Đảm bảo không trả mật khẩu về client
        unset($user['password']);
        
        // Get user role from account_roles and roles tables
        try {
            $roleStmt = $pdo->prepare("
                SELECT r.role_name 
                FROM account_roles ar 
                JOIN roles r ON ar.role_id = r.role_id 
                WHERE ar.account_id = ?
            ");
            $roleStmt->execute([$user['account_id']]);
            $roleResult = $roleStmt->fetch();
            
            if ($roleResult) {
                $user['role'] = $roleResult['role_name'];
            } else {
                // Fallback: Check if this is an admin user by phone number
                $adminPhones = ['0123456789', '0987654321', '0999999999', 'admin', '0000000000', '0998877665'];
                $user['role'] = in_array($user['phone_number'], $adminPhones) ? 'admin' : 'user';
            }
        } catch (Exception $e) {
            // If roles tables don't exist, use phone number fallback
            $adminPhones = ['0123456789', '0987654321', '0999999999', 'admin', '0000000000', '0998877665'];
            $user['role'] = in_array($user['phone_number'], $adminPhones) ? 'admin' : 'user';
        }
        
        echo json_encode(['success' => true, 'user' => $user]);
    } else {
        echo json_encode(['success' => false, 'error' => 'Invalid credentials']);
    }
} else {
    http_response_code(405);
    echo json_encode(['error' => 'Method not allowed']);
}
