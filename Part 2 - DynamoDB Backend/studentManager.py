from __future__ import print_function # Python 2/3 compatibility
from flask import Flask, request
import boto3
import json
import decimal
import datetime

dynamodb = boto3.resource('dynamodb', region_name='region', endpoint_url="http://localhost:8000")

def createUserTable():
    table = dynamodb.create_table(
        TableName="users_",
        KeySchema=[
            {
                'AttributeName': 'course',
                'KeyType': 'HASH'  #Partition key
            },
            {
                'AttributeName': 'student_number',
                'KeyType': 'RANGE' #Sort key
            }
        ],
        AttributeDefinitions=[
            {
                'AttributeName': 'course',
                'AttributeType': 'S'
            },
            {
                'AttributeName': 'student_number',
                'AttributeType': 'S'
            }
        ],
        ProvisionedThroughput={
            'ReadCapacityUnits': 10,
            'WriteCapacityUnits': 10
            }
    )

    table = dynamodb.Table("users_")
    print(table.table_status)

def addNewStudent(course, student_number, name, password, ttid):
    #add deviceid and deviceid functionality here
    table = dynamodb.Table("users_")
    result = table.put_item(
    Item={
        'course':course,
        'student_number':student_number,
        'name':name,
        'password':password,
        'ttid':ttid
        }
    )
    print(result)

def displayStudentData(course, student_number):
    table = dynamodb.Table("users_")
    response = table.get_item(
        Key={
            'course':course,
            'student_number':student_number
            }
        )
    item = response.get('Item')
    print(item)
    
def main():
    #createUserTable()
    #addNewStudent("MSCC", "2955533", "Brandon", "password", "MSCC")
    displayStudentData("MSCC", "2955533")

main()
