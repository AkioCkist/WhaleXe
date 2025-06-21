<?php
include 'connect.php';

header("Access-Control-Allow-Origin: *");
header("Access-Control-Allow-Headers: *");

$data = json_decode(file_get_contents("php://input"));

$phone_number = $data->phone_number;
$password = $data->password;

$sql = "SELECT a.id, a.full_name, a.phone_number, a.password, r.id AS role_id, r.name AS role_name
        FROM accounts a
        JOIN roles r ON a.role_id = r.id
        WHERE a.phone_number = ?";

$stmt = $conn->prepare($sql);
$stmt->bind_param("s", $phone_number);
$stmt->execute();

$result = $stmt->get_result();

if ($result->num_rows === 1) {
    $row = $result->fetch_assoc();
    if (password_verify($password, $row['password'])) {
        echo json_encode([
            "status" => "success",
            "id" => $row['id'],
            "full_name" => $row['full_name'],
            "phone_number" => $row['phone_number'],
            "role_id" => $row['role_id'],
            "role_name" => $row['role_name']
        ]);
    } else {
        echo json_encode(["status" => "error", "message" => "Invalid password"]);
    }
} else {
    echo json_encode(["status" => "error", "message" => "User not found"]);
}

$stmt->close();
$conn->close();
?>
