<?php
global $CFG;
require_once(__DIR__ . '\..\config.php');
require_once($CFG->libdir . '/gradelib.php');
use block_online_users\fetcher;
include_once($CFG->dirroot . '/course/lib.php');
include_once($CFG->libdir . '/coursecatlib.php');
header('Content-type: application/json');
#to get params from url via get_data() or get_submitted_data()
require_once($CFG->libdir.'/formslib.php');

/*
takes in in id of the users
returns array of courses the user is signed up for & att.id from the attendance table
*/ 
function attendance_get_user_courses_attendances($userid) {
    global $DB, $USER;
    
    $usercourses = enrol_get_users_courses($userid);
    list($usql, $uparams) = $DB->get_in_or_equal(array_keys($usercourses), SQL_PARAMS_NAMED, 'cid0');

    $sql = "SELECT att.id as attid, att.course as courseid, course.fullname as coursefullname, 
                   course.shortname as courseshortname, course.startdate as coursestartdate, 
                   att.name as attname, att.grade as attgrade
            FROM {attendance} att
            JOIN {course} course
                ON att.course = course.id
            WHERE att.course $usql
        ORDER BY coursefullname ASC, attname ASC";

    $params = array_merge($uparams, array('uid' => $userid));

    return $DB->get_records_sql($sql, $params);

}

function get_user_courses_by_id($user_id){
    
    #echo "found user id : ". $user_id;
    $function_call = attendance_get_user_courses_attendances($user_id);
    #echo "<br>attendance_get_user_courses_attendances() of user $user_id <br>";
    #echo var_dump($function_call);
    return $function_call;
}

function get_sessions($attid){
    global $DB;
    $start_of_day = (new DateTime())->setTime(0,0);
    $par1 = $start_of_day->format('U') ;
    $end_of_day = (new DateTime())->setTime(23,59,59);
    $par2 = $end_of_day->format('U');
    $sql = "SELECT * 
            FROM attendance_sessions
            WHERE attendanceid = $attid AND sessdate BETWEEN $par1 AND $par2";
    return $DB->get_records_sql($sql);
}

function check_attendance_log($sessid, $studentid){
    global $DB;
    $sql = "SELECT * 
            FROM attendance_log
            WHERE sessionid = $sessid AND studentid = $studentid";
    return $DB->get_records_sql($sql);
}




$user_id = $_GET['id'];
$list_of_user_courses = get_user_courses_by_id($user_id);

$course_session_array = array();
foreach($list_of_user_courses as $c){
    $sessions_for_today = get_sessions($c->attid);
    #echo var_dump($sessions_for_today);
    #echo "<br>";
    if ($sessions_for_today == NULL){
       #echo "<p style='color:red'>no $c->coursefullname today</p>";
    }
    else{
        #echo "<p style='color:green'>FOR COURSE $c->coursefullname with id $c->courseid</p>";
        foreach($sessions_for_today as $s){
            $sess_array = array();
            $sess_array['courseshortname'] = $c->courseshortname;
            $dateobj = new DateTIme();
            $dateobj->setTimestamp($s->sessdate);
            #$sess_start_time = (new DateTime())->setTimestamp->$s->sessdate;
            $sess_start_time = ((new DateTime())->setTimestamp($s->sessdate))->format("H:i:s");
            $sess_end_time = ((new DateTime())->setTimestamp(($s->sessdate)+($s->duration)))->format("H:i:s");
            $sess_array['starttime'] = $sess_start_time;
            $sess_array['endtime'] = $sess_end_time;
            $current_time = (new DateTime())->format('H:i:s');
            
            $session_id = $s->id;
            $sess_array['sessionid'] = $session_id;
            $att_for_session = check_attendance_log($session_id, $user_id);
            if($att_for_session == NULL){
                #echo "session $session_id attendance not marked<br>";
                $sess_array['attend'] = 0;
            }
            else{
                #echo "session $session_id dattendance marked<br>";
                $sess_array['attend'] = 1;
            }

            if ((($current_time >= $sess_start_time) & ($current_time <= $sess_end_time)) & ($sess_array['attend']==0) ){
                $sess_array['markable'] = 1;
            }
            else{
                $sess_array['markable'] = 0;
            }
            array_push($course_session_array, $sess_array);
        }
        
    }
          
}
    #print_r($course_session_array);
    $json_response = json_encode(array("lastrefreshed"=>((new DateTime)->format("H:i l jS F Y")), "course_sessions"=>$course_session_array), JSON_PRETTY_PRINT);
    echo $json_response;     





        /*
convert array of objects to array of arrays to json_encode an return as output
*/

/*
foreach($sessions_for_today as $a){
    $new_arr = array(a);
    $sessions_for_today[$a] = $new_arr;
}
echo json_encode($sessions_for_today, JSON_PRETTY_PRINT);
*/
?>