from __future__ import print_function # Python 2/3 compatibility
from flask import Flask, request
import boto3
import json
import decimal
import datetime

dynamodb = boto3.resource('dynamodb', region_name='region', endpoint_url="http://localhost:8000")

def createLectureTable(lecture):
    table = dynamodb.create_table(
        TableName=lecture,
        KeySchema=[
            {
                'AttributeName': 'student_number',
                'KeyType': 'HASH'  #Partition key
            },
        ],
        AttributeDefinitions=[
            {
                'AttributeName': 'student_number',
                'AttributeType': 'S'
            },
        ],
        ProvisionedThroughput={
            'ReadCapacityUnits': 10,
            'WriteCapacityUnits': 10
            }
    )

    table = dynamodb.Table(lecture)
    print(table.table_status)



def addNewStudent(lecture, student_number):
    table = dynamodb.Table(lecture)
    result = table.put_item(
    Item={
        'student_number':student_number,
        'daysattended': []
        }
    )
    print(result)

def markAttendance(lecture, student_number):
    table = dynamodb.Table(lecture)
    response = table.get_item(
        Key={
            'student_number':student_number
            }
        )

    ## ADD (if response.get('Item') is None) incase student is
    ## not added to the lecture.
    
    daysattended = response.get('Item').get('daysattended')
    currentdate = datetime.datetime.strftime(datetime.datetime.now().date(), "%Y-%m-%d")
    if currentdate not in daysattended:
        daysattended.append(currentdate)
    else:
        print('Date already exists')

    result = table.put_item(
    Item={
        'student_number':student_number,
        'daysattended': daysattended
        }
    )
    print(daysattended)
    
def main():
    createLectureTable("NET_")
    addNewStudent("NET_", "2955533")
    #markAttendance("TNS_", "2955533")

main()
