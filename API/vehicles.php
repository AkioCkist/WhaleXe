<?php
header("Access-Control-Allow-Origin: *");
header("Content-Type: application/json; charset=UTF-8");
header("Access-Control-Allow-Methods: GET, POST, PUT, DELETE");
header("Access-Control-Max-Age: 3600");
header("Access-Control-Allow-Headers: Content-Type, Access-Control-Allow-Headers, Authorization, X-Requested-With");

include_once 'amenity_vehicles.php';

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
    echo json_encode(['error' => 'Database connection failed']);
    exit;
}

// Initialize Vehicle class
$vehicle = new Vehicle($pdo);

$request_method = $_SERVER["REQUEST_METHOD"];

switch($request_method) {
    case 'GET':
        if (!empty($_GET["id"])) {
            $id = intval($_GET["id"]);
            get_vehicle_detail($vehicle, $id);
        } else {
            get_vehicles($vehicle);
        }
        break;
    case 'POST':
        if (isset($_POST['action']) && $_POST['action'] == 'update_status') {
            updateVehicleStatus($_POST);
        } elseif (isset($_POST['action']) && $_POST['action'] == 'toggle_favorite') {
            toggle_favorite($vehicle);
        }
        break;
    default:
        header("HTTP/1.0 405 Method Not Allowed");
        break;
}

function get_vehicles($vehicle) {
    // Xử lý tham số query
    $status_filter = isset($_GET['status']) ? $_GET['status'] : null;
    $limit = isset($_GET['limit']) ? intval($_GET['limit']) : null;
    $offset = isset($_GET['offset']) ? intval($_GET['offset']) : null;  
    $search = isset($_GET['search']) ? $_GET['search'] : null;
    $type = isset($_GET['type']) ? $_GET['type'] : null;
    $location = isset($_GET['location']) ? $_GET['location'] : null;
    $favorites_only = isset($_GET['favorites']) && $_GET['favorites'] == 'true';

    // New filter parameters
    $brand = isset($_GET['brand']) ? $_GET['brand'] : null;
    $seats = isset($_GET['seats']) ? intval($_GET['seats']) : null;
    $fuel_type = isset($_GET['fuel_type']) ? $_GET['fuel_type'] : null;

    try {
        if ($favorites_only) {
            $stmt = $vehicle->getFavoriteVehicles($status_filter);
        } elseif ($search || $type || $location || $brand || $seats || $fuel_type) {
            // Update searchVehicles to include new filters
            $stmt = $vehicle->searchVehicles($search, $type, $location, $status_filter, $brand, $seats, $fuel_type);
        } else {
            // Khi không có search, type, location thì lấy tất cả xe
            $stmt = $vehicle->getAllVehicles($limit, $offset, $status_filter);
        }

        $num = $stmt->rowCount();

        if ($num > 0) {
            $vehicles_arr = array();
            $vehicles_arr["records"] = array();
            $vehicles_arr["total"] = $num;

            while ($row = $stmt->fetch(PDO::FETCH_ASSOC)) {
                extract($row);

                // Lấy hình ảnh của xe
                $images_stmt = $vehicle->getVehicleImages($id);
                $images = array();
                while ($img_row = $images_stmt->fetch(PDO::FETCH_ASSOC)) {
                    // Fix URL path - remove duplicate /cars
                    $img_url = $img_row["image_url"];
                    if (strpos($img_url, 'http') !== 0) {
                        // Remove leading /cars if exists, then add the full path
                        $clean_url = ltrim($img_url, '/cars');
                        $img_url = 'http://10.0.2.2/myapi/cars/' . $clean_url; // 10.0.2.2 cho emulator
                    }
                    
                    $images[] = array(
                        "id" => $img_row["image_id"],
                        "url" => $img_url,
                        "is_primary" => $img_row["is_primary"] == 1,
                        "display_order" => $img_row["display_order"]
                    );
                }

                // Lấy tiện nghi của xe
                $amenities_stmt = $vehicle->getVehicleAmenities($id);
                $amenities = array();
                while ($amenity_row = $amenities_stmt->fetch(PDO::FETCH_ASSOC)) {
                    $amenities[] = array(
                        "id" => $amenity_row["amenity_id"],
                        "name" => $amenity_row["amenity_name"],
                        "icon" => $amenity_row["amenity_icon"],
                        "description" => $amenity_row["description"]
                    );
                }

                // Format giá tiền
                $price_display = number_format($base_price / 1000, 0) . "K/ngày";
                $price_formatted = number_format($base_price, 0, ',', '.') . ' VND';
                
                // Tạo old price giả (bạn có thể điều chỉnh logic này)
                $old_price = null;
                $price_discount = null;
                if ($rating >= 4.5) {
                    $old_price = $base_price * 1.2;
                    $price_discount = "Giảm 20%";
                }

                // Xử lý URL hình ảnh primary để đảm bảo đường dẫn đúng
                $primary_image = 'http://localhost/myapi/cars/default_car.png';
                if (!empty($images)) {
                    $primary_image = $images[0]["url"]; // Already processed above
                }

                $vehicle_item = array(
                    "id" => $id,
                    "name" => $name,
                    "rating" => floatval($rating),
                    "trips" => intval($trips),
                    "location" => $location,
                    "transmission" => $transmission,
                    "seats" => intval($seats),
                    "fuel" => $fuel,
                    "base_price" => floatval($base_price),
                    "priceDisplay" => $price_display,
                    "price_formatted" => $price_formatted,
                    "oldPrice" => $old_price,
                    "priceDiscount" => $price_discount,
                    "pricePer" => "ngày",
                    "vehicle_type" => $vehicle_type,
                    "description" => $description,
                    "status" => $status,
                    "is_favorite" => $is_favorite == 1,
                    "is_favorite_for_user" => $is_favorite == 1,
                    "lessor_id" => $lessor_id,
                    "lessor_name" => $lessor_name,
                    "primary_image" => $primary_image,
                    "images" => $images,
                    "amenities" => $amenities,
                    "created_at" => $created_at,
                    "updated_at" => $updated_at
                );

                array_push($vehicles_arr["records"], $vehicle_item);
            }

            http_response_code(200);
            echo json_encode($vehicles_arr);
        } else {
            http_response_code(200);
            echo json_encode(array(
                "message" => "Không tìm thấy xe nào.",
                "records" => array(),
                "total" => 0
            ));
        }
    } catch (Exception $e) {
        http_response_code(500);
        echo json_encode(array("message" => "Lỗi server: " . $e->getMessage()));
    }
}

function get_vehicle_detail($vehicle, $id) {
    try {
        $stmt = $vehicle->getVehicleById($id);
        $num = $stmt->rowCount();

        if ($num == 1) {
            $row = $stmt->fetch(PDO::FETCH_ASSOC);
            extract($row);

            // Lấy tất cả hình ảnh
            $images_stmt = $vehicle->getVehicleImages($id);
            $images = array();
            while ($img_row = $images_stmt->fetch(PDO::FETCH_ASSOC)) {
                // Fix URL path - remove duplicate /cars
                $img_url = $img_row["image_url"];
                if (strpos($img_url, 'http') !== 0) {
                    // Remove leading /cars if exists, then add the full path
                    $clean_url = ltrim($img_url, '/cars');
                    $img_url = 'http://10.0.2.2/myapi/cars/' . $clean_url; // 10.0.2.2 cho emulator
                }
                
                $images[] = array(
                    "id" => $img_row["image_id"],
                    "url" => $img_url,
                    "is_primary" => $img_row["is_primary"] == 1,
                    "display_order" => $img_row["display_order"]
                );
            }

            // Lấy tất cả tiện nghi
            $amenities_stmt = $vehicle->getVehicleAmenities($id);
            $amenities = array();
            while ($amenity_row = $amenities_stmt->fetch(PDO::FETCH_ASSOC)) {
                $amenities[] = array(
                    "id" => $amenity_row["amenity_id"],
                    "name" => $amenity_row["amenity_name"],
                    "icon" => $amenity_row["amenity_icon"],
                    "description" => $amenity_row["description"]
                );
            }

            $price_display = number_format($base_price / 1000, 0) . "K/ngày";
            $price_formatted = number_format($base_price, 0, ',', '.') . ' VND';
            
            // Tính toán giá bảo hiểm và tổng giá (tương tự get_car_details.php)
            $insurance_price = $base_price * 0.1; // 10% của giá cơ bản
            $insurance_price_formatted = number_format($insurance_price, 0, ',', '.') . ' VND';
            $total_price = $base_price + $insurance_price;
            $total_price_formatted = number_format($total_price, 0, ',', '.') . ' VND';
            
            // Tạo old price giả
            $old_price = null;
            $price_discount = null;
            if ($rating >= 4.5) {
                $old_price = $base_price * 1.2;
                $price_discount = "Giảm 20%";
            }
            
            $vehicle_item = array(
                "id" => $id,
                "name" => $name,
                "rating" => floatval($rating),
                "trips" => intval($trips),
                "location" => $location,
                "transmission" => $transmission,
                "seats" => intval($seats),
                "fuel" => $fuel,
                "base_price" => floatval($base_price),
                "priceDisplay" => $price_display,
                "base_price_formatted" => $price_formatted,
                "insurance_price" => $insurance_price,
                "insurance_price_formatted" => $insurance_price_formatted,
                "total_price" => $total_price,
                "total_price_formatted" => $total_price_formatted,
                "oldPrice" => $old_price,
                "priceDiscount" => $price_discount,
                "pricePer" => "ngày",
                "vehicle_type" => $vehicle_type,
                "description" => $description,
                "status" => $status,
                "is_favorite" => $is_favorite == 1,
                "is_favorite_for_user" => $is_favorite == 1,
                "lessor_id" => $lessor_id,
                "lessor_name" => $lessor_name,
                "images" => $images,
                "amenities" => $amenities,
                "created_at" => $created_at,
                "updated_at" => $updated_at
            );

            http_response_code(200);
            echo json_encode($vehicle_item);
        } else {
            http_response_code(404);
            echo json_encode(array("message" => "Không tìm thấy xe."));
        }
    } catch (Exception $e) {
        http_response_code(500);
        echo json_encode(array("message" => "Lỗi server: " . $e->getMessage()));
    }
}

function toggle_favorite($vehicle) {
    $data = json_decode(file_get_contents("php://input"));

    if (!empty($data->vehicle_id)) {
        $vehicle_id = $data->vehicle_id;
        $is_favorite = isset($data->is_favorite) ? $data->is_favorite : true;

        if ($vehicle->toggleFavorite($vehicle_id, $is_favorite)) {
            http_response_code(200);
            echo json_encode(array("message" => "Đã cập nhật trạng thái yêu thích."));
        } else {
            http_response_code(503);
            echo json_encode(array("message" => "Không thể cập nhật trạng thái yêu thích."));
        }
    } else {
        http_response_code(400);
        echo json_encode(array("message" => "Dữ liệu không hợp lệ."));
    }
}

function updateVehicleStatus($data) {
    global $pdo;
    
    $vehicleId = $data['vehicle_id'] ?? null;
    $status = $data['status'] ?? 'rented';

    if (!$vehicleId) {
        http_response_code(400);
        echo json_encode(['success' => false, 'error' => 'Vehicle ID is required']);
        return;
    }

    try {
        $stmt = $pdo->prepare("UPDATE vehicles SET status = ?, updated_at = CURRENT_TIMESTAMP WHERE vehicle_id = ?");
        $stmt->execute([$status, $vehicleId]);
        
        if ($stmt->rowCount() > 0) {
            echo json_encode(['success' => true, 'message' => 'Vehicle status updated successfully']);
        } else {
            echo json_encode(['success' => false, 'error' => 'Vehicle not found or status unchanged']);
        }
    } catch (PDOException $e) {
        http_response_code(500);
        echo json_encode(['success' => false, 'error' => $e->getMessage()]);
    }
}

?>