'''
Created on Mar 21, 2014

@author: Kshitiz

This file should be used to define all Models that our server application will use.
For any DB-related functionality, import this module in your module.
'''
import peewee
from peewee import *

''' A MySQLDatabase object for our transactions. Currently threadLocals = True, seems to be needed!'''
dbObject = MySQLDatabase("collabmid", user='collabmiduser', passwd='collabmidpassword', threadlocals=True)

class baseDBModel(Model):
    """A base model that will use our Backend database"""
    class Meta:
        database = dbObject
 

class Sensor(baseDBModel):
    name = TextField()
    SensorType = CharField(index=True)
    maxRange = DoubleField(index=True)
    minDelay = DoubleField(index=True)
    power = DoubleField(index=True)
    resolution = DoubleField(index=True)
    
    class Meta:
        indexes = (
                (('SensorType', 'maxRange', 'minDelay', 'power', 'resolution'), True),
            )
     
class User(baseDBModel):
    username = CharField(unique=True)
    RegistrationDate = DateTimeField()
    ActivityRecognition = BooleanField(index=True)
    DownloadAllowed = BooleanField(index=True)
    
class SensorUserRel(baseDBModel):
    user = ForeignKeyField(User, related_name="sensors")
    sensor = ForeignKeyField(Sensor, related_name="users")
    class Meta:
        indexes = (
                (('user','sensor'), True),
            )       
    
class Query(baseDBModel):
    username = ForeignKeyField(User, related_name="queries")
    queryNo= TextField(unique=True)
    dataReqd= CharField()
    frequency = IntegerField() #in Hertz
    Activity= TextField()
    Location = TextField()
    Latitude= DoubleField()
    Longitude= DoubleField()
    fromTime= DateTimeField(index=True)
    toTime= DateTimeField(index=True)
    expiryTime= DateTimeField(index=True)
    countMin= IntegerField()
    countMax= IntegerField()
    countReceived = IntegerField()
    
    class Meta:
        indexes = (
                (('fromTime', 'toTime', 'expiryTime'), False),
            )    

 
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
