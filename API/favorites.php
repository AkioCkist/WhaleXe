<?php
header("Access-Control-Allow-Origin: *");
header("Content-Type: application/json; charset=UTF-8");
header("Access-Control-Allow-Methods: GET, POST, DELETE");
header("Access-Control-Max-Age: 3600");
header("Access-Control-Allow-Headers: Content-Type, Access-Control-Allow-Headers, Authorization, X-Requested-With");

if ($_SERVER['REQUEST_METHOD'] === 'OPTIONS') {
    http_response_code(200);
    exit();
}

// Database connection
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
    echo json_encode(['status' => 'error', 'message' => 'Database connection failed']);
    exit;
}

$request_method = $_SERVER["REQUEST_METHOD"];

switch($request_method) {
    case 'GET':
        // Get user favorites
        if (isset($_GET['account_id'])) {
            getUserFavorites($pdo, $_GET['account_id']);
        } else {
            http_response_code(400);
            echo json_encode(['status' => 'error', 'message' => 'Missing account_id parameter']);
        }
        break;

    case 'POST':
        // Toggle favorite
        $data = json_decode(file_get_contents("php://input"));
        if (isset($data->account_id) && isset($data->vehicle_id)) {
            toggleFavorite($pdo, $data->account_id, $data->vehicle_id);
        } else {
            http_response_code(400);
            echo json_encode(['status' => 'error', 'message' => 'Missing account_id or vehicle_id parameter']);
        }
        break;

    case 'DELETE':
        // Remove favorite
        $data = json_decode(file_get_contents("php://input"));
        if (isset($data->account_id) && isset($data->vehicle_id)) {
            removeFavorite($pdo, $data->account_id, $data->vehicle_id);
        } else {
            http_response_code(400);
            echo json_encode(['status' => 'error', 'message' => 'Missing account_id or vehicle_id parameter']);
        }
        break;

    default:
        http_response_code(405);
        echo json_encode(['status' => 'error', 'message' => 'Method not allowed']);
        break;
}

function getUserFavorites($pdo, $account_id) {
    try {
        // Query to get all favorite vehicles for a user with details
        $query = "
            SELECT v.*,
                   f.id as favorite_id,
                   f.created_at as favorited_at,
                   (SELECT image_url FROM vehicle_images WHERE vehicle_id = v.vehicle_id AND is_primary = 1 LIMIT 1) as primary_image
            FROM favorites f
            JOIN vehicles v ON f.vehicle_id = v.vehicle_id
            WHERE f.account_id = :account_id
            ORDER BY f.created_at DESC
        ";

        $stmt = $pdo->prepare($query);
        $stmt->bindParam(':account_id', $account_id, PDO::PARAM_INT);
        $stmt->execute();

        $favorites = [];
        while ($row = $stmt->fetch()) {
            // Format price for display
            $price_display = number_format($row['base_price'] / 1000, 0) . "K/ngày";
            $price_formatted = number_format($row['base_price'], 0, ',', '.') . ' VND';

            // Format image URL for Android emulator access
            $image_url = $row['primary_image'];
            if (strpos($image_url, 'http') !== 0 && !empty($image_url)) {
                $clean_url = ltrim($image_url, '/cars');
                $image_url = 'http://10.0.2.2/myapi/cars/' . $clean_url;
            } elseif (empty($image_url)) {
                $image_url = 'http://10.0.2.2/myapi/cars/default_car.png';
            }

            // Get vehicle amenities
            $amenities_query = "
                SELECT a.amenity_id, a.amenity_name, a.amenity_icon, a.description
                FROM vehicle_amenity_mapping m
                JOIN vehicle_amenities a ON m.amenity_id = a.amenity_id
                WHERE m.vehicle_id = :vehicle_id
            ";
            $amenities_stmt = $pdo->prepare($amenities_query);
            $amenities_stmt->bindParam(':vehicle_id', $row['vehicle_id'], PDO::PARAM_INT);
            $amenities_stmt->execute();
            $amenities = $amenities_stmt->fetchAll();

            $favorites[] = [
                "id" => $row['vehicle_id'],
                "name" => $row['name'],
                "rating" => floatval($row['rating']),
                "trips" => intval($row['total_trips']),
                "location" => $row['location'],
                "transmission" => $row['transmission'],
                "seats" => intval($row['seats']),
                "fuel" => $row['fuel_type'],
                "base_price" => floatval($row['base_price']),
                "price_display" => $price_display,
                "price_formatted" => $price_formatted,
                "vehicle_type" => $row['vehicle_type'],
                "description" => $row['description'],
                "status" => $row['status'],
                "is_favorite" => true,
                "lessor_id" => $row['lessor_id'],
                "favorite_id" => $row['favorite_id'],
                "favorited_at" => $row['favorited_at'],
                "primary_image" => $image_url,
                "amenities" => $amenities
            ];
        }

        $response = [
            'status' => 'success',
            'count' => count($favorites),
            'data' => $favorites
        ];

        http_response_code(200);
        echo json_encode($response);

    } catch (PDOException $e) {
        http_response_code(500);
        echo json_encode(['status' => 'error', 'message' => 'Database error: ' . $e->getMessage()]);
    }
}

function toggleFavorite($pdo, $account_id, $vehicle_id) {
    try {
        // First check if favorite already exists
        $check_query = "SELECT id FROM favorites WHERE account_id = :account_id AND vehicle_id = :vehicle_id";
        $check_stmt = $pdo->prepare($check_query);
        $check_stmt->bindParam(':account_id', $account_id, PDO::PARAM_INT);
        $check_stmt->bindParam(':vehicle_id', $vehicle_id, PDO::PARAM_INT);
        $check_stmt->execute();

        if ($check_stmt->rowCount() > 0) {
            // Favorite exists, remove it
            $query = "DELETE FROM favorites WHERE account_id = :account_id AND vehicle_id = :vehicle_id";
            $stmt = $pdo->prepare($query);
            $stmt->bindParam(':account_id', $account_id, PDO::PARAM_INT);
            $stmt->bindParam(':vehicle_id', $vehicle_id, PDO::PARAM_INT);
            $stmt->execute();

            http_response_code(200);
            echo json_encode([
                'status' => 'success',
                'message' => 'Removed from favorites',
                'is_favorite' => false
            ]);
        } else {
            // Favorite doesn't exist, add it
            $query = "INSERT INTO favorites (account_id, vehicle_id) VALUES (:account_id, :vehicle_id)";
            $stmt = $pdo->prepare($query);
            $stmt->bindParam(':account_id', $account_id, PDO::PARAM_INT);
            $stmt->bindParam(':vehicle_id', $vehicle_id, PDO::PARAM_INT);
            $stmt->execute();

            http_response_code(201);
            echo json_encode([
                'status' => 'success',
                'message' => 'Added to favorites',
                'is_favorite' => true,
                'favorite_id' => $pdo->lastInsertId()
            ]);
        }
    } catch (PDOException $e) {
        http_response_code(500);
        echo json_encode(['status' => 'error', 'message' => 'Database error: ' . $e->getMessage()]);
    }
}

function removeFavorite($pdo, $account_id, $vehicle_id) {
    try {
        $query = "DELETE FROM favorites WHERE account_id = :account_id AND vehicle_id = :vehicle_id";
        $stmt = $pdo->prepare($query);
        $stmt->bindParam(':account_id', $account_id, PDO::PARAM_INT);
        $stmt->bindParam(':vehicle_id', $vehicle_id, PDO::PARAM_INT);
        $stmt->execute();

        if ($stmt->rowCount() > 0) {
            http_response_code(200);
            echo json_encode([
                'status' => 'success',
                'message' => 'Favorite removed successfully'
            ]);
        } else {
            http_response_code(404);
            echo json_encode([
                'status' => 'error',
                'message' => 'Favorite not found'
            ]);
        }
    } catch (PDOException $e) {
        http_response_code(500);
        echo json_encode(['status' => 'error', 'message' => 'Database error: ' . $e->getMessage()]);
    }
}
