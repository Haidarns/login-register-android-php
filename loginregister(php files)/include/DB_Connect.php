<?php
class DB_Connect {
    private $conn;
 
    // Connecting to database
    public function connect() {
        require_once 'Config.php';
         
        // Connecting to mysql database
        $this->conn = new mysqli("localhost", "root", "", "android_api");
         
        // return database handler
        return $this->conn;
    }
}
 
?>