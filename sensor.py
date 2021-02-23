import smbus
import time
import RPi.GPIO as GPIO
#import geocoder
import requests
import json
#import urllib2
from thread import start_new_thread
import math
import Adafruit_DHT

class anemometer():
    imp_per_sec = 0
    actual_windspeed_msec = 0
    events = []
    
    def __init__(self):
        GPIO.setmode(GPIO.BOARD)

        # Pin 3 (GPIO 0) auf Input setzen
        GPIO.setup(3, GPIO.IN, pull_up_down=GPIO.PUD_UP)
        
        GPIO.add_event_detect(3, GPIO.RISING, callback = self.interrupt, bouncetime = 5)
        
    def interrupt(self,val):
        self.imp_per_sec += 1

    def ws100_imp_to_mpersec(self,val):
            #y = 8E-09x5 - 2E-06x4 + 0,0002x3 - 0,0073x2 + 0,4503x + 0,11

            y = float("8e-9") * math.pow(val,5) - float("2e-6") * math.pow(val,4) + float("2e-4") * math.pow(val,3) - float("7.3e-3") * math.pow(val,2) + 0.4503 * val + 0.11
            if y < 0.2:
                    y = 0
            return y

    def threadeval(self):
        i = 0
        while i<10:
            self.actual_windspeed_msec = self.ws100_imp_to_mpersec(self.imp_per_sec)
            #print ("actual_windspeed_msec %f" % self.actual_windspeed_msec)
            self.imp_per_sec = 0
            for x in self.events:
                x.set()
            time.sleep(1)
            i+=1
        
    def readWindSpeed(self):
        return self.actual_windspeed_msec

 
def __calculate_dewPoint_diff(humidity, temp):
    return temp - (temp - ((100 - humidity) / 5))
        
def IsInternetUp():
    try:
        #urllib2.urlopen('http://192.168.0.27:8000/sensor/', timeout=1)
        return True
    except urllib2.URLError as err:
        return False

def sendNotice(wind, fog):
    g = geocoder.ip('me')
    print(g.latlng)
    url = "http://192.168.0.27:8000/sensor/"
    headers = {"content-type": "application/json"}
    body = {"latitude":str(g.lat), "longitude":str(g.lng), "wind":wind, "fog":fog}
    requests.post(url, data = json.dumps(body), headers = headers)
    #old_frozen_road = frozen_road
    #old_dangerous_curve = dangerous_curve
        
if __name__ == "__main__":
    while True:
        if(IsInternetUp()):
            #GPIO.cleanup()
            wind_sensor = anemometer()
            wind_sensor.threadeval()
            speed=wind_sensor.readWindSpeed()
            if speed > 100:
                sendNotice(True,False)
            print("actual_windspeed_msec %f" % speed)
            #start_new_thread(wind_sensor.threadeval, ())
            
            #humidity_sensor = DHT11(pin=17)
            #result = humidity_sensor.read()
            #if result.is_valid():
            humidity, temperature = Adafruit_DHT.read_retry(11, 17)
                #print("Temp: %d C" % result.temperature +' '+"Humid: %d %%" % result.humidity +' '+"Dew Point: %d C" % humidity_sensor.dewPoint(result.humidity,result.temperature))
            if humidity >= 20 and __calculate_dewPoint_diff(humidity,temperature) < 12.5:
                sendNotice(False,True)
                print("Temp: %d C" % temperature +' '+"Humid: %d %%" % humidity +' '+"Dew Point: %d C" % __calculate_dewPoint_diff(humidity,temperature))
                    
            
                    
            #time.sleep(1800)
            #GPIO.cleanup()
            time.sleep(10)
        
        else:
            time.sleep(600)