'''
Created on 11-Mar-2014

@author: nishant
'''
import json
import threading
from Models import Sensor, User, SensorUserRel
import datetime

class RegistrationProcessor(threading.Thread):
    
    def __init__(self, msgHandler, rMessage):
        super(RegistrationProcessor, self).__init__()
        self.rMessage = rMessage
        self.msgHandler = msgHandler
        
        
        '''This is the method called when this thread is started.'''
    def run(self):
        ''' Check if the message is registration or deregistration '''
        self.msgObject = json.loads(self.rMessage['body'])
        print "Running Registration Processor!"
        print str(self.rMessage['subject']) + " ; is the message subject"
        if str(self.rMessage['subject'])  in ('Sensor Capabilities'): #preliminary Check, might change later
            print "Matched Register!"
            self.registerUser(self.msgObject, str(self.rMessage['from']).split("@")[0])
        elif str(self.rMessage['subject']) in ('Delete Account'):
            print "Matched DeRegister!"
            self.deRegisterUser(self.msgObject, str(self.rMessage['from']).split("@")[0])
        return
    
    def registerUser(self, msgObject, userName): #(long userID,long password, string[] sensorsPresent): #msg contains userid+capabilities 
        ''' To do:
            Parse the incoming message and store the capabilities of a particular user in the DB
        '''
        print "Inside RegisterUser"
        foundFlag = False
        try:
            u = User.get(User.username==userName)
            foundFlag = True
        except User.DoesNotExist:
            u = User(username=str(userName), RegistrationDate=datetime.datetime.now())
            u.save()
        print "User Saved"
        
        if foundFlag:
            #User already exists. Should handle it differently.
            #Means it could be a updation message. Delete all his previous Sensor Relation entries and fill new ones for now.
            print "User already existed! Deleting previous relation entries!"
            surtoD = SensorUserRel.select().where(SensorUserRel.user==u)
            for s in surtoD:
                s.delete_instance()
                    
        
        sensorQueue = []
        #numSensor = eval(msgObject['noSensors'])
        for i in msgObject.keys():
            if i not in ('noSensors'):
                try:
                    s = Sensor.get(Sensor.SensorType==str(i), Sensor.maxRange==eval(str(msgObject[i][0])), Sensor.minDelay == eval(str(msgObject[i][1])), Sensor.power == eval(str(msgObject[i][2])), Sensor.resolution==eval(str(msgObject[i][3])))
                except Sensor.DoesNotExist:
                    s = Sensor(name="temp", SensorType=str(i), maxRange=eval(str(msgObject[i][0])), minDelay = eval(str(msgObject[i][1])), power = eval(str(msgObject[i][2])), resolution=eval(str(msgObject[i][3])))
                    s.save()
                sensorQueue += [s]
                
            
        for s in sensorQueue:
            sur = SensorUserRel(user=u, sensor=s)
            sur.save()     
                
        print "Registered Successfully!"
        
        self.msgHandler.send_message(mto=str(self.rMessage['from']).split("/")[0], msubject="Registration Successful!", mbody="Thank you! Registration/Updation Successful!")
        
        return 
         
    def deRegisterUser(self, msgObject, userName): #(long userID):    
        
        ''' To do:
                Remove the entries for the userID from the DB
                Remember to deregister from the jabber server as well apart from this. 
            '''
        try:
            u = User.get(User.username==userName, User.RegistrationDate==datetime.datetime.now())
            sensorObjects = SensorUserRel.select().where(SensorUserRel.user==u)
            for i in sensorObjects:
                i.delete_instance()
            u.delete_instance()
        except:
            print "Something went wrong."
            return
        
        self.msgHandler.send_message(mto=str(self.rMessage['from']).split("/")[0], msubject="De-Registration Successful!", mbody="Thank you!")

        return
    
def processRegistrationMessage(msgHandler, rMessage):
    processor = RegistrationProcessor(msgHandler, rMessage)
    processor.start()
