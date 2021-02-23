#!/usr/bin/python3

from bluetooth_server import BluetoothServer
from bluetooth_client import BluetoothClient


class MainServer(BluetoothServer):

    def __init__(self):

        BluetoothServer.__init__(self)

    def handleMessage(self, message):
        print(message)

        self.send(message)
    

if __name__ == '__main__':

    server = MainServer()

    server.start()
