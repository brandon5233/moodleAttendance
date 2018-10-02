<?php
require_once(__DIR__ . '/../config.php');

//recieve course end time as well
function mark_attendance($sessionid, $studentid){
    global $DB;

    $statusid = 9;
    $statusset = "9,11,12,10";

    $record = new stdclass();
    $record->id = NULL;
    $record->sessionid = $sessionid;
    $record->studentid = $studentid;
    $record->statusid = $statusid;
    $record->statusset = $statusset;
    $record->takenby = $studentid;
    $current_time = (new DateTime())->format('U')
    $record->timetaken = $current_time;
    $record->remarks = "Self-recorded";
    $record->ipaddress = "192.168.1.4";
    $i = array($record);
    //check course end time before inserting the record 
    echo $DB->insert_records('attendance_log', $i);

    #Update attendance_session table 
    $update = new stdclass();
    $update->id = $sessionid;
    $update->lasttaken = $current_time;
    $update->lasttakenby = $studentid;
    $update->description = "Regular class session";

    #$arr_update = array($update);
    $DB->update_record('attendance_sessions', $update);
    

}

$sessionid = $_POST['sessionid'];
$studentid = $_POST['studentid'];
mark_attendance($sessionid, $studentid);
echo json_encode(array("response"=>"attendance successfully marked!"));