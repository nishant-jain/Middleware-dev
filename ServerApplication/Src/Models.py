'''
Created on Mar 21, 2014

@author: Kshitiz

This file should be used to define all Models that our server application will use.
For any DB-related functionality, import this module in your module.
'''
import peewee
from peewee import *

"""" Made it a file to avoid cross-thread issues with in-memory SQLite!"""
dbObject = SqliteDatabase("firstDB.db", threadlocals=True)

class baseDBModel(Model):
    """A base model that will use our Backend database"""
    class Meta:
        database = dbObject
 

class Sensor(baseDBModel):
    name = TextField()
    SensorType = CharField()
    maxRange = DoubleField()
    minDelay = DoubleField()
    power = DoubleField()
    resolution = DoubleField()
     
class User(baseDBModel):
    username = CharField()
    RegistrationDate = DateTimeField()
    ActivityRecognition = BooleanField()
    DownloadAllowed = BooleanField()
    
class SensorUserRel(baseDBModel):
    user = ForeignKeyField(User, related_name="sensors")
    sensor = ForeignKeyField(Sensor, related_name="users")   
    
class Query(baseDBModel):
    username = ForeignKeyField(User, related_name="queries")
    queryNo= BigIntegerField()
    dataReqd= CharField()
    frequency = IntegerField() #in Hertz
    Activity= TextField()
    Location = TextField()
    Latitude= DoubleField()
    Longitude= DoubleField()
    fromTime= DateTimeField()
    toTime= DateTimeField()
    expiryTime= DateTimeField()
    countMin= IntegerField()
    countMax= IntegerField()
    countReceived = IntegerField()

 
def connect(): 
    print "Connecting!"
    dbObject.connect()
    Sensor.create_table()
    User.create_table()
    SensorUserRel.create_table()
    Query.create_table()
    print "Created Tables!"
    
dbObject.connect()
#connect()