
package com.example.brandon.attendance;
/*
TO DO
add @string resource
force vertical orientation
remember me
keep me signed in
pull down to refresh
logout + logout button
attendance report
 */

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Timer;
import java.util.TimerTask;

import cz.msebera.android.httpclient.Header;
import de.hdodenhof.circleimageview.CircleImageView;

import static com.example.brandon.attendance.Login.checkMockApps;
import static com.example.brandon.attendance.Login.url;

public class MainActivity extends AppCompatActivity {
    private TextView course1, course2, course3, textView, welcome, datetime;
    private String userid = "", sessionid = "";
    private Button button;
    private CircleImageView userProfilePic;
    private LinearLayout lectureBlock, noLectureBlock;
    private Timer refreshTimer;
    private TimerTask timerTask;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Intent intent = getIntent();

        final SwipeRefreshLayout swipeRefreshLayout = findViewById(R.id.swiperefreshlayout);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getAttendanceRecords(userid);
                swipeRefreshLayout.setRefreshing(false);
            }
        });
        lectureBlock = findViewById(R.id.lectureBlock);
        noLectureBlock = findViewById(R.id.no_lecture_block);

        final String username = intent.getStringExtra("username");
        userProfilePic = (CircleImageView) findViewById(R.id.user_profile_pic);
        textView = findViewById(R.id.textView);
        course1 = findViewById(R.id.course1);
        course2 = findViewById(R.id.course2);
        course3 = findViewById(R.id.course3);
        welcome = findViewById(R.id.welcome);
        datetime = findViewById(R.id.datetime);
        button = findViewById(R.id.button2);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getAttendanceRecords(userid);
            }
        });

        getUserInfo(username);
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                /*Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();*/
                getAttendanceRecords(userid);
            }
        });

        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mark_attendance(sessionid);
                //check(sessionid);
                //Toast.makeText(MainActivity.this, "clicked textView", Toast.LENGTH_SHORT).show();
            }
        });

       /* timerTask = new TimerTask(){

            @Override
            public void run() {
                getAttendanceRecords(userid);
                Log.i("time",String.valueOf(System.currentTimeMillis()));
            }
        };
        refreshTimer = new Timer("auto_refresh");
        refreshTimer.schedule(timerTask,0,15000);*/



    }



    // To set profile pic from server
    public void setUserProfilePic(String url) {
        try {
            Picasso.get().load(url).into(userProfilePic);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    // To set username from server
    public void setUserName(String name) {
        try {
            welcome.setText(name);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // To set last refreshed date from server
    public void setLastRefreshDate(String date) {
        try {
            datetime.setText(date);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // To show no lecture block
    public void showNoLectureBlock() {
        noLectureBlock.setVisibility(View.VISIBLE);
    }

    // To show lecture block
    public void showLectureBlock(JSONArray lectures) {
        try {
            if (lectures.length() > 0) {
                lectureBlock.setVisibility(View.VISIBLE);
                noLectureBlock.setVisibility(View.GONE);
                for (int i = 0; i < 6; i++) {
                    CardView courseCard = (CardView) lectureBlock.getChildAt(i);
                    courseCard.setVisibility(View.INVISIBLE);
                }
                for (int i = 0; i < lectures.length(); i++) {
                    if (i <= 5) {
                        CardView courseCard = (CardView) lectureBlock.getChildAt(i);
                        courseCard.setVisibility(View.VISIBLE);
                        final int temp_j = i;
                        LinearLayout courseBlock = ((LinearLayout) (courseCard.getChildAt(0)));
                        courseBlock.setBackgroundColor(getColor(R.color.milkyWhite));

                        JSONObject courseObj = lectures.getJSONObject(i);
                        final String courseshortname = courseObj.getString("courseshortname");
                        String starttime = courseObj.getString("starttime");
                        String endtime = courseObj.getString("endtime");
                        Boolean attend = courseObj.getInt("attend") == 1;
                        Boolean markable = courseObj.getInt("markable") == 1;
                        courseBlock.getChildAt(3).setVisibility(View.INVISIBLE);

                        ((TextView) courseBlock.getChildAt(0)).setText(courseshortname);
                        ((TextView) courseBlock.getChildAt(1)).setText(starttime + "-" + endtime);
                        ((TextView) courseBlock.getChildAt(2)).setText("Attendance - " + (attend ? "marked" : "Not marked"));

                        // validation if course is clickable or not
                        if (markable) {
                            courseBlock.getChildAt(3).setVisibility(View.VISIBLE);
                            courseBlock.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {


                                    // on course is clicked
                                    onCourseClicked_b(courseshortname, temp_j + 1);
                                }
                            });
                        } else {
                            courseBlock.setBackgroundColor(getColor(R.color.disabledGray));
                            courseBlock.setOnClickListener(null);
                        }
                    } else {
                        break;
                    }
                }
            } else {
                showNoLectureBlock();
                Log.i("this is test", "no lecture block test");
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getApplicationContext(), "Error while displaying course", Toast.LENGTH_SHORT);
        }

    }

    public void onCourseClicked_b(String courseName, int lectureNo) {
        Toast.makeText(getApplicationContext(), "What happens on " + courseName + " , lecture number " + lectureNo + " clicked", Toast.LENGTH_SHORT).show();
    }

    public void getUserInfo(final String username) {

        AsyncHttpClient client = new AsyncHttpClient();
        RequestParams params = new RequestParams();
        params.put("username", username);

        client.get(url + "attendance_api/get_data_from_username.php", params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject jsonObject) {
                try {
                    userid = jsonObject.getString("userid");
                    course1.setText(userid);
                    String firstname = jsonObject.getString("firstname");
                    setUserName(firstname);
                    setLastRefreshDate("last refreshed: " + jsonObject.getString("lastrefreshed"));
                    setUserProfilePic(String.format("%s/user/pix.php/%s/f1.jpg", url, userid));
                    getAttendanceRecords(userid);
                    Log.i("userid", userid);

                } catch (Exception e) {
                    Toast.makeText(MainActivity.this, "server error: invalid response", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseBody, Throwable error) {
                try {
                    Toast.makeText(MainActivity.this, statusCode + ": " + responseBody, Toast.LENGTH_SHORT).show();
                } catch (Exception e) {
                    Toast.makeText(MainActivity.this, "Error displaying onFailure message; error code:" + statusCode, Toast.LENGTH_SHORT).show();
                }

            }
        });

    }

    public void getAttendanceRecords(String userid) {

        if (checkMockApps(MainActivity.this)){
            Log.i("Spoofer ", "Detected");
            Toast.makeText(MainActivity.this, R.string.spooferdetected, Toast.LENGTH_SHORT).show();
        }
        else {
            AsyncHttpClient client = new AsyncHttpClient();
            RequestParams params = new RequestParams();
            params.put("id", userid);
            String new_url = url + "attendance_api/attendance.php";
            //Log.i("NEW URL: ", new_url);
            client.get(new_url, params, new JsonHttpResponseHandler() {

                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject jsonObject) {
                    Log.i("response", String.valueOf(jsonObject));
                    textView.setText("");
                    try {
                        setLastRefreshDate(jsonObject.getString("lastrefreshed"));
                        JSONArray course_sessions = jsonObject.getJSONArray("course_sessions");
                        Log.i("courses:", String.valueOf(course_sessions));
                        showLectureBlock(course_sessions);

                    } catch (Exception e) {
                        Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, String response, Throwable error) {
                    Toast.makeText(MainActivity.this, statusCode + ": " + response, Toast.LENGTH_LONG).show();
                    Log.i("ERROR ", String.valueOf(statusCode));
                    textView.setText("");

                    textView.append(response + "\n");
                    Log.i("error", response);
                    //textView.setText(String.valueOf(headers));
                }
            });

        }

    }


    public void mark_attendance(String sessionid) {
        Log.i("status", "marking attendance");
        AsyncHttpClient client = new AsyncHttpClient();
        RequestParams params = new RequestParams();
        params.put("sessionid", sessionid);
        params.put("studentid", userid);

        client.post(url + "attendance_api/mark_attendance.php", params, new JsonHttpResponseHandler() {

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject jsonObject) {
                try {
                    Toast.makeText(MainActivity.this, jsonObject.getString("response"), Toast.LENGTH_SHORT).show();
                    getAttendanceRecords(userid);
                } catch (Exception e) {
                    Toast.makeText(MainActivity.this, "server error: invalid response ", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String response, Throwable error) {
                Log.e("failed", String.valueOf(statusCode) + " : " + response);
            }
        });
    }
}
