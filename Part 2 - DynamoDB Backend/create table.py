from __future__ import print_function # Python 2/3 compatibility
import boto3

dynamodb = boto3.resource('dynamodb', region_name='region', endpoint_url="http://localhost:8000")


table = dynamodb.create_table(
    TableName='student_',
    KeySchema=[
        {
            'AttributeName': 'course',
            'KeyType': 'HASH'  #Partition key
        },
        {
            'AttributeName': 'studentNumber',
            'KeyType': 'RANGE'  #Sort key
        }
    ],
    AttributeDefinitions=[
        {
            'AttributeName': 'course',
            'AttributeType': 'S'
        },
        {
            'AttributeName': 'studentNumber',
            'AttributeType': 'N'
        },

    ],
    ProvisionedThroughput={
        'ReadCapacityUnits': 10,
        'WriteCapacityUnits': 10
        }
)

table = dynamodb.Table('student_')
print(table.table_status)
