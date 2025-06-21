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

    // Check for required fields - accept both 'password' and 'new_password'
    if (!isset($data['phone_number']) || (!isset($data['password']) && !isset($data['new_password']))) {
        http_response_code(400);
        echo json_encode(['success' => false, 'error' => 'Missing required fields: phone_number and password']);
        exit;
    }

    try {
        // Clean input data
        $phone_number = trim($data['phone_number']);
        // Accept both 'password' and 'new_password' field names
        $new_password = isset($data['new_password']) ? trim($data['new_password']) : trim($data['password']);

        // Validate inputs
        if (empty($new_password)) {
            http_response_code(400);
            echo json_encode(['success' => false, 'error' => 'New password cannot be empty']);
            exit;
        }

        if (strlen($new_password) < 6) {
            http_response_code(400);
            echo json_encode(['success' => false, 'error' => 'Password must be at least 6 characters long']);
            exit;
        }

        // Validate phone number format
        if (!preg_match('/^[+]?\d{10,15}$/', $phone_number)) {
            http_response_code(400);
            echo json_encode(['success' => false, 'error' => 'Invalid phone number format']);
            exit;
        }

        // Hash the new password using the same method as profile_update.php
        $hashedPassword = password_hash($new_password, PASSWORD_BCRYPT);

        // Update the password using phone_number as identifier
        $sql = "UPDATE accounts SET password = :password WHERE phone_number = :phone_number";
        $stmt = $pdo->prepare($sql);
        $result = $stmt->execute([
            'password' => $hashedPassword,
            'phone_number' => $phone_number
        ]);

        if ($result && $stmt->rowCount() > 0) {
            // Fetch updated user data
            $stmt = $pdo->prepare("SELECT account_id, username, phone_number FROM accounts WHERE phone_number = ?");
            $stmt->execute([$phone_number]);
            $user = $stmt->fetch();

            echo json_encode([
                'success' => true,
                'message' => 'Password updated successfully',
                'user' => $user
            ]);
        } else {
            echo json_encode([
                'success' => false,
                'error' => 'Failed to update password. Phone number not found or password unchanged.'
            ]);
        }

    } catch (\Exception $e) {
        http_response_code(500);
        echo json_encode(['success' => false, 'error' => 'Error updating password: ' . $e->getMessage()]);
    }
} else {
    http_response_code(405);
    echo json_encode(['success' => false, 'error' => 'Method not allowed']);
}
?>