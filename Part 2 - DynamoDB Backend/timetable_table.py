from __future__ import print_function # Python 2/3 compatibility
from flask import Flask, request
import boto3
import json
import decimal
import datetime

dynamodb = boto3.resource('dynamodb', region_name='region', endpoint_url="http://localhost:8000")

def createTable():
    table = dynamodb.create_table(
        TableName='timetable_',
        KeySchema=[
            {
                'AttributeName': 'ttid',
                'KeyType': 'HASH'  #Partition key
            },
        ],
        AttributeDefinitions=[
            {
                'AttributeName': 'ttid',
                'AttributeType': 'S'
            },
        ],
        ProvisionedThroughput={
            'ReadCapacityUnits': 10,
            'WriteCapacityUnits': 10
            }
    )

    table = dynamodb.Table('timetable_')
    print(table.table_status)



def loadTable():
    table = dynamodb.Table('timetable_')
    with open("Thursday.json") as json_file:
        tt = json.load(json_file, parse_float = decimal.Decimal)
    with open("MSCC_course_list.json") as json_file:
        course_list = json.load(json_file, parse_float = decimal.Decimal)
    
    result = table.put_item(
        Item={
            'ttid':'MSCC',
            'Monday':[],
            'Tuesday':[],
            'Wednesday':[],
            'Thursday':tt,
            'Friday':tt,
            'Saturday':tt,
            'Sunday':[],
            'course_list': course_list
            }
        )
    print(result)

def printTodaysTimeTable():
    table = dynamodb.Table('timetable_')
    response = table.get_item(
        Key={
            'ttid':'MSCC'
            }
        )
    item = response.get('Item')
    print(item)
    currentday = datetime.datetime.strftime(datetime.datetime.now(), "%A")
    print(item.get(currentday))
    if item.get(currentday) == []:
        print("today is free")
        return json.dumps({'response':'You have no lectures scheduled for today'})
    else:
        for _ in item.get(currentday):
            print(_.get("lecture"))
def main():
    #createTable()
    loadTable()
    printTodaysTimeTable()

main()
