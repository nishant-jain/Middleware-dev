'''
Created on 11-Mar-2014

@author: nishant
'''
import peewee
from peewee import *

mysql_db = MySQLDatabase('my_database', user='code') #add database name, usrname, password


class MySQLModel(Model):
    """A base model that will use our MySQL database"""
    class Meta:
        database = mysql_db

class Query(MySQLModel):
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


mysql_db.connect()
#Query.create_table();
#new_entry = User()
#new_entry.save() 


def queryparse(msg): #parse queries
    print msg['body']
    query= msg['body']

def fetchParameters(JSONquery):
    ''' To do:
    Parse parameters of query from the received msg and store it in DB
    returns an object of Query class containing attribute values specified in the JSON query message. 
    '''
        
    return 

def lookup(query):
    ''' To do:
    returns true if the query can be serviced by already available queries in the Query Database
    ; otherwise returns false and the query is forwarded to the DataRequester Module.
    '''
    return 

def sendAcknowledgement(): 
    ''' To do:
    sends an acknowledgement to the publisher stating that the query has been received
     and he will be notified when sufficient subscribers will provide the information.
    '''
    return 

def sendConfirmation():
    ''' To do:
    send a message to the requester informing whether the query will be serviced.
    '''
    return 
