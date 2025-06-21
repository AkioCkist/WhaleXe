<?php
$pdo = new PDO("mysql:host=localhost;dbname=whalexe", "root", "");
$users = $pdo->query("SELECT account_id, password FROM accounts")->fetchAll();

$count = 0;

foreach ($users as $user) {
    $current = $user['password'];

    // ⚠️ Bỏ kiểm tra password_get_info — ép hash lại toàn bộ
    $hashed = password_hash($current, PASSWORD_BCRYPT);

    $stmt = $pdo->prepare("UPDATE accounts SET password = ? WHERE account_id = ?");
    $stmt->execute([$hashed, $user['account_id']]);
    $count++;
}

echo "Đã mã hóa $count mật khẩu.";
