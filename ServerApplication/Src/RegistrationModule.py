'''
Created on 11-Mar-2014

@author: nishant
'''
def registerUser(msg): #(long userID,long password, string[] sensorsPresent): #msg contains userid+capabilities 
    ''' To do:
        Parse the incoming message and store the capabilities of a particular user in the DB
    '''
    
    return 
     
def deRegisterUser(userID): #(long userID):    
    
    ''' To do:
            Remove the entries for the userID from the DB
            Remember to deregister from the jabber server as well apart from this. 
        '''
    return


import peewee
from peewee import *

mysql_db = MySQLDatabase('my_database', user='code')


class MySQLModel(Model):
    """A base model that will use our MySQL database"""
    class Meta:
        database = mysql_db

class User(MySQLModel):
    username = CharField()
    topicsSubscribedTo = TextField()
    queriedFor =  TextField()
    sensorsPresent = TextField()

mysql_db.connect() 


#mysql_db.create_table();
#new_entry = MySQLModel()
#new_entry.save() 