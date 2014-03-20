'''
Created on Mar 21, 2014

@author: Kshitiz

This file should be used to define all Models that our server application will use.
For any DB-related functionality, import this module in your module.
'''
import peewee
from peewee import *

""" Making it a in-memory SQLite database for now, before we figure out some centralized way to establish DBs across machines """
sqlite_db = SqliteDatabase(":memory:")

class baseDBModel(Model):
    """A base model that will use our Backend database"""
    class Meta:
        database = sqlite_db
 
class User(baseDBModel):
    username = CharField()
    topicsSubscribedTo = TextField()
    queriedFor =  TextField()
    sensorsPresent = TextField()
    
class Query(baseDBModel):
    username = CharField()
    queryNo= FloatField()
    dataReqd= TextField()
    frequency= IntegerField()
    Activity= TextField()
    Latitude= DoubleField()
    Longitude= DoubleField()
    fromTime= TimeField()
    toTime= TimeField()
    expiryTime= TimeField()
    countMin= IntegerField()
    countMax= IntegerField()
    countReceived= IntegerField()
 
sqlite_db.connect()