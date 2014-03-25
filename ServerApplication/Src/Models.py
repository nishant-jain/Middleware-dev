'''
Created on Mar 21, 2014

@author: Kshitiz

This file should be used to define all Models that our server application will use.
For any DB-related functionality, import this module in your module.
'''
import peewee
from peewee import *

""" Making it a in-memory SQLite database for now, before we figure out some centralized way to establish DBs across machines """
dbObject = SqliteDatabase(":memory:")

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
 
dbObject.connect()