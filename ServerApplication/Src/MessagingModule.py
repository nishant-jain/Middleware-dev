'''
Created on 11-Mar-2014

@author: nishant
'''
import logging
import RegistrationModule
import QueryHandlerModule
from sleekxmpp import ClientXMPP


class MessageHandler(ClientXMPP):
    

    def __init__(self, jid, password):
        ClientXMPP.__init__(self, jid, password)

        self.add_event_handler("session_start", self.session_start)
        self.add_event_handler("message", self.message)
        

        # If you wanted more functionality, here's how to register plugins:
        # self.register_plugin('xep_0030') # Service Discovery
        # self.register_plugin('xep_0199') # XMPP Ping

        # Here's how to access plugins once you've registered them:
        # self['xep_0030'].add_feature('echo_demo')

        # If you are working with an OpenFire server, you will
        # need to use a different SSL version:
        # import ssl
        # self.ssl_version = ssl.PROTOCOL_SSLv3

    def session_start(self, event):
        self.send_presence()
        self.get_roster()
        
        # Most get_*/set_* methods from plugins use Iq stanzas, which
        # can generate IqError and IqTimeout exceptions
        #
        # try:
        #     self.get_roster()
        # except IqError as err:
        #     logging.error('There was an error getting the roster')
        #     logging.error(err.iq['error']['condition'])
        #     self.disconnect()
        # except IqTimeout:
        #     logging.error('Server is taking too long to respond')
        #     self.disconnect()

    def message(self, msg):
        print "Got Message!"
        msg['from'] = str(msg['from']).split("/")[0]
        if msg['type'] in ('chat', 'normal'):
            #print "Message received",msg
            if(str(msg['type'])=='chat'):  #using msg type "chat" to symbolise queries
                QueryHandlerModule.queryparse(self, msg)
                return
            else:
                RegistrationModule.processRegistrationMessage(self, msg)  #using msg type "normal" to symbolise registration for capabilities
            #msg.reply("Thanks for sending,\n%(body)s" % msg).send()  # can be converted to ack, although need to see if those are necessary
            


if __name__ == '__main__':
    # Ideally use optparse or argparse to get JID,
    # password, and log level.

    logging.basicConfig(level=logging.DEBUG,
                        format='%(levelname)-8s %(message)s')
    #Models.connect()
    print "hello"    
    xmpp = MessageHandler('server@103.25.231.23', 'server') #Should keep a centralized username and password. 
    xmpp.connect()
    
    #print "hi"
    #xmpp.send_message(mto="new_nishant@localhost", mbody="hello1234",mtype="chat")
    xmpp.process(block=False)    
