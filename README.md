## Master's Thesis - A location based attendance system for Moodle. 

Name: Brandon Rozario  
Student Number: 2955533  
Subject: Master's Thesis   
Faculty: Computing  
Title: Location Based Attendance System for Moodle.   

The project is divided into 2 parts:
Part 1 is the core of the thesis and is an attendance system for Moodle. 
Part 2 is a standalone attendance system with DynamoDB as the backend. 

## About
Moodle is a popular open-source course management system used by 63k registered sites, from 222 countries all over the world. 
This project adds location-based attendance-taking capabilities to moodle's attendance module. 
Students use the Android application (frontend of this project) to log in to their moodle account and mark themselves present for a given lecture. It only works if they do so before the lecture ends, and they are within the college campus. 

At the backend, the developed API accepts the request from the frontend, interfaces with Moodle, and marks the student present.    
