from __future__ import print_function
from flask import Flask, request
import boto3
import json
import decimal
import datetime

dynamodb = boto3.resource('dynamodb', region_name='region', endpoint_url="http://localhost:8000")

##to do: use json in request instead of forms

app = Flask(__name__)
@app.route('/')
def index():
    return 'moodle.griffith.ie_ Log in to the site.html'

@app.route('/login', methods=['POST'])
def login():
    
    course = request.form.get('course')
    password = request.form.get('password')
    student_number = request.form.get('student_number')
    print(course)
    print(password)

    table = dynamodb.Table('users_')
    result = table.get_item(
        Key={
            'course':str(course),
            'student_number':str(student_number)
            }
        )
    item = result.get('Item')
    
    if item is None:
        return json.dumps({'result':'No User Found'}, indent=4)
    else:
        if item.get('password') == password:
            return json.dumps({'result':'success',
                               'name':item.get('name'),
                               'ttid':item.get('ttid')}, indent=4)
        else:
            return json.dumps({'result':'password is incorrect'}, indent=4)

@app.route('/getCourses', methods=['POST'])
def getCourses():
    ttid = request.form.get('ttid')
    print("ttid = ", ttid)
    student_number = request.form.get('student_number')

    table = dynamodb.Table('timetable_')
    response = table.get_item(
        Key={
            'ttid':str(ttid)
            }
        )
    item = response.get('Item')
    currentday = datetime.datetime.strftime(datetime.datetime.now(), "%A")
    ## incase no lectures are scheduled for the day and item == None
    print(item)
    if item is None:
        return json.dumps({'result':'Something went wrong, please contact the administrator'})
    else:
        if item.get(currentday) == []:
            return json.dumps({'result':'success', 'timetable':'You have no lectures scheduled for today'})
        else:
            #return json.dumps({'result':'success', 'timetable':item.get(currentday)}, indent=4)
            for _ in item.get(currentday):
                lecture = str(_.get('lecture'))+"_"
                table = dynamodb.Table(lecture)
                print("lecture = ", lecture)
                response = table.get_item(
                    Key = {
                        'student_number':student_number
                        }
                    )
                daysattended = response.get('Item').get('daysattended')
                currentdate = datetime.datetime.strftime(datetime.datetime.now().date(), "%Y-%m-%d")
                if currentdate not in daysattended:
                    _['attend'] = 'no'
                else:
                    _['attend'] = 'yes'
            return json.dumps({'result':'success', 'timetable':item.get(currentday)}, indent=4)

@app.route('/markattendance', methods=['POST'])
def markattendance():
    lecture  = request.form.get('lecture')
    student_number = request.form.get('student_number')
    table = dynamodb.Table(lecture)
    currentdate = datetime.datetime.strftime(datetime.datetime.now().date(), "%Y-%m-%d")
    response = table.get_item(
        Key = {
            'student_number':student_number
            }
        )
    daysattended = response.get('Item').get('daysattended')
    daysattended.append(currentdate)
    response = table.put_item(
        Item = {
            'student_number':student_number,
            'daysattended':daysattended
            }
        )
    return json.dumps({'result':'success'})


@app.route('/moodle', methods=['GET'])
def moodle():
    return "I SEE YOU"
                
@app.route('/signup', methods=['POST'])
def signup():
    table = dynamodb.Table('temp_user')
    email = request.form.get('email')
    password = request.form.get('password')
    student_number = request.form.get('student_number')

    response = table.get_item(
        Key={
            'email':email
        }
    )

    item = response.get('Item')
    print("item = ", item)

    #if item is None:
    if True:
        response = table.put_item(
            Item = {
                'email':email,
                'password':password,
                'student_number':student_number,
                'device_id':'0000'
                })
        print(response)
        return json.dumps({"message":"success"}, indent=4)

    else:
        #Have to check if PID already exists !!
        return json.dumps({"message":"user already exists"}, indent=4)
        
        
@app.route('/signin', methods=['POST'])
def signin():
    table = dynamodb.Table('temp_user')
    email = request.form.get('email')
    password = request.form.get('password')

    response = table.get_item(
        Key={
            'email':email
            }
        )
    item = response.get('Item')
    print(item['password'])
    if item['password']== password:
        return json.dumps({'response':'true'}, indent=4)
    else:
        return json.dumps({'response':'false'}, indent=4)


@app.route('/getdata', methods=['POST'])
def getdata():
    if request.method == 'POST':
        table = dynamodb.Table('temp_user')

        email = request.form.get('email')
        response = table.get_item(
        Key={
            'email':email
        }
    )

        item = response.get('Item')
        print("GetItem succeeded:")
        #return json.dumps(item, indent=4)
        print(item)
        return json.dumps(item, indent=4, cls=DecimalEncoder)
        
    if request.method == 'GET':
        return json.dumps({'message':'hi from /getdata()'})

@app.route('/listallusers', methods=['POST'])
def listallusers():
    table = dynamodb.Table('temp_user')
    users = table.scan()
##    print('Items = ', users['Items'])
##    print('Count = ', users['Count'])
##    print('Scanned Count', users['ScannedCount'])
##    print('ResponseMetadata', users['ResponseMetadata'] )
    usernames = []
    for x in users['Items']:
        usernames.append(x.get('email'))
    num_users = str(users['ScannedCount'])+" users found"
    return json.dumps({'response':num_users, 'users':usernames}, indent=4)
        

# Helper class to convert a DynamoDB item to JSON.
class DecimalEncoder(json.JSONEncoder):
    def default(self, o):
        if isinstance(o, decimal.Decimal):
            if abs(o) % 1 > 0:
                return float(o)
            else:
                return int(o)
        return super(DecimalEncoder, self).default(o)

############################# MOODLE FUNCTIONS #########################

@app.route('/moodle_login', methods=['POST'])
def moodle_login():

    received_data = request.get_data().decode('utf8').replace("'", '"')
    json_response = json.loads(received_data)
    course = json_response.get('course')
    password = json_response.get('password')
    student_number = json_response.get('student_number')
    print(course)
    print(password)
    

    table = dynamodb.Table('users_')
    result = table.get_item(
        Key={
            'course':str(course),
            'student_number':str(student_number)
            }
        )
    item = result.get('Item')
    
    if item is None:
        return json.dumps({'result':'No User Found'}, indent=4)
    else:
        if item.get('password') == password:
            return json.dumps({'result':'success',
                               'name':item.get('name'),
                               'ttid':item.get('ttid')}, indent=4)
        else:
            return json.dumps({'result':'password is incorrect'}, indent=4)

@app.route('/moodle_getpercentage')
def moodle_getpercentage():
    received_data = request.get_data().decode('utf8').replace("'", '"')
    json_response = json.loads(received_data)
    
    
    

