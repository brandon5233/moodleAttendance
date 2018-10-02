<?php
require_once(__DIR__ . '/../config.php');
global $DB;
$username = $_GET['username'];
$sql = "SELECT id, firstname
       FROM user
       WHERE username = '$username' ";
header('Content-type: application/json');
//echo json_encode(array_keys($DB->get_records_sql($sql))[0]);
$student_records = $DB->get_records_sql($sql);
$key = array_keys($student_records)[0];
$value = $student_records[$key];
$userid = $value->id;
$firstname = $value->firstname;

echo json_encode(array("userid"=>$userid, "firstname"=>$firstname, "lastrefreshed"=>((new DateTime)->format("H:i l jS F Y"))));
?>