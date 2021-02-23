import os

import bluetooth as bt
import time
from carnavigation import car

class BluetoothServer(object):

    def __init__(self):

        print("INIT SERVER")
        
        # Arbitrary service UUID to advertise
        self.uuid = "7be1fcb3-5776-42fb-91fd-2ee7b5bbb86d"

        self.client_sock = None
        
        self.data={"94:87:E0:AD:E9:5C":"111"}

    def start(self):
        
        data={"94:87:E0:AD:E9:5C":"111"}

        # Make device visible
        os.system("hciconfig hci0 piscan")

        # Create a new server socket using RFCOMM protocol
        server_sock = bt.BluetoothSocket(bt.RFCOMM)

        # Bind to any port
        server_sock.bind(("", bt.PORT_ANY))

        # Start listening
        server_sock.listen(1)
        
        #server_sock.settimeout(100.0)

        # Get the port the server socket is listening
        port = server_sock.getsockname()[1]

        # Start advertising the service
        bt.advertise_service(server_sock, "RaspiBtSrv",
                           service_id=self.uuid,
                           service_classes=[self.uuid, bt.SERIAL_PORT_CLASS],
                           profiles=[bt.SERIAL_PORT_PROFILE])

        # Outer loop: listen for connections from client
        
        #timeout = time.time() + 20   # [seconds]

        
        while True:

            print("Waiting for connection on RFCOMM channel %d" % port)

            try:

                # This will block until we get a new connection
                self.client_sock, client_info = server_sock.accept()
                print("Accepted connection from " +  str(client_info))

                # Track strings delimited by '.'
                s = ''

                while True:

                    c = self.client_sock.recv(1).decode('utf-8')
                    if c == '$' and len(s) > 0:
                        print(s)
                        mycar = car(s)
                        self.handleMessage(mycar.__str__())
                        
                        s = ''
                        if self.client_sock is not None:
                            self.client_sock.close()
                    else:
                        s += c


            except IOError:
                pass
            

            except KeyboardInterrupt:

                if self.client_sock is not None:
                    self.client_sock.close()

                server_sock.close()

                print("Server going down")
                break

    def send(self, message):
        print(message)
        self.client_sock.send((message+'.').encode('utf-8'))
        
