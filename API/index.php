<?php
header('Content-Type: application/json');

// DB connection
$host = 'localhost';
$db   = 'whalexe';
$user = 'root';
$pass = '';
$charset = 'utf8mb4';

// PDO setup
$dsn = "mysql:host=$host;dbname=$db;charset=$charset";
$options = [
    PDO::ATTR_ERRMODE            => PDO::ERRMODE_EXCEPTION,
    PDO::ATTR_DEFAULT_FETCH_MODE => PDO::FETCH_ASSOC,
];

try {
    $pdo = new PDO($dsn, $user, $pass, $options);
} catch (\PDOException $e) {
    http_response_code(500);
    echo json_encode(['error' => 'Database connection failed']);
    exit;
}

// Handle GET
if ($_SERVER['REQUEST_METHOD'] === 'GET') {
    $stmt = $pdo->query("SELECT * FROM accounts");
    $rows = $stmt->fetchAll();
    echo json_encode($rows);
    exit;
}

// Handle POST (expects JSON data)
if ($_SERVER['REQUEST_METHOD'] === 'POST') {
    $data = json_decode(file_get_contents("php://input"), true);

    if (!isset($data['column1']) || !isset($data['password'])) {
        http_response_code(400);
        echo json_encode(['error' => 'Missing required fields']);
        exit;
    }

    $stmt = $pdo->prepare("INSERT INTO accounts (phone_number, password) VALUES (?, ?)");
    $stmt->execute([$data['column1'], $data['password']]);

    echo json_encode(['success' => true]);
    exit;
}

// Handle PUT (update user info)
if ($_SERVER['REQUEST_METHOD'] === 'PUT') {
    $data = json_decode(file_get_contents("php://input"), true);

    if (!isset($data['account_id'])) {
        http_response_code(400);
        echo json_encode(['error' => 'Missing account_id']);
        exit;
    }

    $fields = [];
    $params = [];

    if (isset($data['username'])) {
        $fields[] = "username = ?";
        $params[] = $data['username'];
    }
    if (isset($data['phone_number'])) {
        $fields[] = "phone_number = ?";
        $params[] = $data['phone_number'];
    }
    if (isset($data['password']) && $data['password']) {
        $fields[] = "password = ?";
        $params[] = $data['password'];
    }

    if (empty($fields)) {
        http_response_code(400);
        echo json_encode(['error' => 'No fields to update']);
        exit;
    }

    $params[] = $data['account_id'];
    $sql = "UPDATE accounts SET " . implode(', ', $fields) . " WHERE account_id = ?";
    $stmt = $pdo->prepare($sql);
    $stmt->execute($params);

    echo json_encode(['success' => true]);
    exit;
}

http_response_code(405); // Method not allowed
echo json_encode(['error' => 'Method not allowed']);
