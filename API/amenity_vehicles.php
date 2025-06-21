<?php
header('Content-Type: application/json');
header("Access-Control-Allow-Origin: *");
header("Access-Control-Allow-Headers: Content-Type");
header("Access-Control-Allow-Methods: POST, OPTIONS");

class Vehicle {
    private $conn;
    private $table_name = "vehicles";

    public function __construct($db) {
        $this->conn = $db;
    }

    // Lấy tất cả xe với thông tin chi tiết
    public function getAllVehicles($limit = null, $offset = null, $status_filter = null) {
        $query = "SELECT 
            v.vehicle_id as id,
            v.lessor_id,
            v.name,
            v.rating,
            v.total_trips as trips,
            v.location,
            v.transmission,
            v.seats,
            v.fuel_type as fuel,
            v.base_price,
            v.vehicle_type,
            v.description,
            v.status,
            v.is_favorite,
            v.created_at,
            v.updated_at,
            a.username as lessor_name,
            vi.image_url as primary_image
        FROM " . $this->table_name . " v
        LEFT JOIN accounts a ON v.lessor_id = a.account_id
        LEFT JOIN vehicle_images vi ON v.vehicle_id = vi.vehicle_id AND vi.is_primary = 1";
        
        $params = array();
        $conditions = array();

        // Thêm điều kiện status nếu có
        if ($status_filter) {
            $conditions[] = "v.status = :status";
            $params[':status'] = $status_filter;
        }

        // Thêm WHERE clause nếu có điều kiện
        if (!empty($conditions)) {
            $query .= " WHERE " . implode(" AND ", $conditions);
        }

        $query .= " ORDER BY v.created_at DESC";
        
        if ($limit) {
            $query .= " LIMIT " . $limit;
            if ($offset) {
                $query .= " OFFSET " . $offset;
            }
        }

        $stmt = $this->conn->prepare($query);
        
        foreach ($params as $key => $value) {
            $stmt->bindValue($key, $value);
        }
        
        $stmt->execute();
        return $stmt;
    }

    // Lấy xe theo ID
    public function getVehicleById($id) {
        $query = "SELECT 
            v.vehicle_id as id,
            v.lessor_id,
            v.name,
            v.rating,
            v.total_trips as trips,
            v.location,
            v.transmission,
            v.seats,
            v.fuel_type as fuel,
            v.base_price,
            v.vehicle_type,
            v.description,
            v.status,
            v.is_favorite,
            v.created_at,
            v.updated_at,
            a.username as lessor_name
        FROM " . $this->table_name . " v
        LEFT JOIN accounts a ON v.lessor_id = a.account_id
        WHERE v.vehicle_id = :id";

        $stmt = $this->conn->prepare($query);
        $stmt->bindParam(':id', $id);
        $stmt->execute();

        return $stmt;
    }

    // Lấy hình ảnh của xe
    public function getVehicleImages($vehicle_id) {
        $query = "SELECT image_id, image_url, is_primary, display_order 
                  FROM vehicle_images 
                  WHERE vehicle_id = :vehicle_id 
                  ORDER BY is_primary DESC, display_order ASC";
        
        $stmt = $this->conn->prepare($query);
        $stmt->bindParam(':vehicle_id', $vehicle_id);
        $stmt->execute();

        return $stmt;
    }

    // Lấy tiện nghi của xe
    public function getVehicleAmenities($vehicle_id) {
        $query = "SELECT va.amenity_id, va.amenity_name, va.amenity_icon, va.description
                  FROM vehicle_amenities va
                  INNER JOIN vehicle_amenity_mapping vam ON va.amenity_id = vam.amenity_id
                  WHERE vam.vehicle_id = :vehicle_id
                  ORDER BY va.amenity_id";
        
        $stmt = $this->conn->prepare($query);
        $stmt->bindParam(':vehicle_id', $vehicle_id);
        $stmt->execute();

        return $stmt;
    }

    // Tìm kiếm xe
    public function searchVehicles($search_term, $vehicle_type = null, $location = null, $status_filter = null, $brand = null, $seats = null, $fuel_type = null) {
        $query = "SELECT
            v.vehicle_id as id,
            v.lessor_id,
            v.name,
            v.rating,
            v.total_trips as trips,
            v.location,
            v.transmission,
            v.seats,
            v.fuel_type as fuel,
            v.base_price,
            v.vehicle_type,
            v.description,
            v.status,
            v.is_favorite,
            v.created_at,
            v.updated_at,
            a.username as lessor_name,
            vi.image_url as primary_image
        FROM " . $this->table_name . " v
        LEFT JOIN accounts a ON v.lessor_id = a.account_id
        LEFT JOIN vehicle_images vi ON v.vehicle_id = vi.vehicle_id AND vi.is_primary = 1
        WHERE 1=1";

        $params = array();

        // Thêm điều kiện status
        if ($status_filter) {
            $query .= " AND v.status = :status";
            $params[':status'] = $status_filter;
        }

        if ($search_term) {
            $query .= " AND (v.name LIKE :search OR v.description LIKE :search OR v.location LIKE :search)";
            $params[':search'] = '%' . $search_term . '%';
        }

        if ($vehicle_type) {
            $query .= " AND v.vehicle_type = :vehicle_type";
            $params[':vehicle_type'] = $vehicle_type;
        }

        if ($location) {
            $query .= " AND v.location LIKE :location";
            $params[':location'] = '%' . $location . '%';
        }

        // Add brand filter (extract brand from name field)
        if ($brand) {
            $query .= " AND v.name LIKE :brand";
            $params[':brand'] = $brand . '%';
        }

        // Add seats filter
        if ($seats) {
            $query .= " AND v.seats = :seats";
            $params[':seats'] = $seats;
        }

        // Add fuel type filter
        if ($fuel_type) {
            $query .= " AND v.fuel_type = :fuel_type";
            $params[':fuel_type'] = $fuel_type;
        }

        $query .= " ORDER BY v.rating DESC, v.created_at DESC";

        $stmt = $this->conn->prepare($query);
        
        foreach ($params as $key => $value) {
            $stmt->bindValue($key, $value);
        }
        
        $stmt->execute();

        return $stmt;
    }

    // Cập nhật trạng thái yêu thích
    public function toggleFavorite($vehicle_id, $is_favorite) {
        $query = "UPDATE " . $this->table_name . " 
                  SET is_favorite = :is_favorite, updated_at = CURRENT_TIMESTAMP
                  WHERE vehicle_id = :vehicle_id";

        $stmt = $this->conn->prepare($query);
        $stmt->bindParam(':is_favorite', $is_favorite);
        $stmt->bindParam(':vehicle_id', $vehicle_id);

        return $stmt->execute();
    }

    // Lấy xe yêu thích
    public function getFavoriteVehicles($status_filter = null) {
        $query = "SELECT 
            v.vehicle_id as id,
            v.lessor_id,
            v.name,
            v.rating,
            v.total_trips as trips,
            v.location,
            v.transmission,
            v.seats,
            v.fuel_type as fuel,
            v.base_price,
            v.vehicle_type,
            v.description,
            v.status,
            v.is_favorite,
            v.created_at,
            v.updated_at,
            a.username as lessor_name,
            vi.image_url as primary_image
        FROM " . $this->table_name . " v
        LEFT JOIN accounts a ON v.lessor_id = a.account_id
        LEFT JOIN vehicle_images vi ON v.vehicle_id = vi.vehicle_id AND vi.is_primary = 1
        WHERE v.is_favorite = 1";

        $params = array();

        // Thêm điều kiện status nếu có
        if ($status_filter) {
            $query .= " AND v.status = :status";
            $params[':status'] = $status_filter;
        }

        $query .= " ORDER BY v.updated_at DESC";

        $stmt = $this->conn->prepare($query);
        
        foreach ($params as $key => $value) {
            $stmt->bindValue($key, $value);
        }
        
        $stmt->execute();

        return $stmt;
    }

    // Cập nhật trạng thái xe
    public function updateVehicleStatus($vehicle_id, $status) {
        $query = "UPDATE " . $this->table_name . " 
                  SET status = :status, updated_at = CURRENT_TIMESTAMP
                  WHERE vehicle_id = :vehicle_id";

        $stmt = $this->conn->prepare($query);
        $stmt->bindParam(':status', $status);
        $stmt->bindParam(':vehicle_id', $vehicle_id);

        return $stmt->execute();
    }

    // Lấy thống kê xe theo trạng thái
    public function getVehicleStats() {
        $query = "SELECT 
                    status,
                    COUNT(*) as count,
                    AVG(rating) as avg_rating,
                    SUM(total_trips) as total_trips
                  FROM " . $this->table_name . "
                  GROUP BY status";

        $stmt = $this->conn->prepare($query);
        $stmt->execute();

        return $stmt;
    }

    // Lấy xe theo lessor
    public function getVehiclesByLessor($lessor_id, $status_filter = null) {
        $query = "SELECT 
            v.vehicle_id as id,
            v.lessor_id,
            v.name,
            v.rating,
            v.total_trips as trips,
            v.location,
            v.transmission,
            v.seats,
            v.fuel_type as fuel,
            v.base_price,
            v.vehicle_type,
            v.description,
            v.status,
            v.is_favorite,
            v.created_at,
            v.updated_at,
            a.username as lessor_name,
            vi.image_url as primary_image
        FROM " . $this->table_name . " v
        LEFT JOIN accounts a ON v.lessor_id = a.account_id
        LEFT JOIN vehicle_images vi ON v.vehicle_id = vi.vehicle_id AND vi.is_primary = 1
        WHERE v.lessor_id = :lessor_id";

        $params = array(':lessor_id' => $lessor_id);

        if ($status_filter) {
            $query .= " AND v.status = :status";
            $params[':status'] = $status_filter;
        }

        $query .= " ORDER BY v.created_at DESC";

        $stmt = $this->conn->prepare($query);
        
        foreach ($params as $key => $value) {
            $stmt->bindValue($key, $value);
        }
        
        $stmt->execute();

        return $stmt;
    }
}
?>