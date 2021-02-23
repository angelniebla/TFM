import pandas as pd
from json import loads,dumps
import pymysql
from math import radians, cos, sin, asin, sqrt, atan2
from mysql_connect import db
import pycurl
from io import BytesIO, StringIO
import numpy as np

class car(object):

    '''def __init__(self,data):
        location = loads(data)
        db_obj = db()
        db_obj.insertTable('car',location)
        print(location)
        #df = pd.read_sql('SELECT * FROM car', con=connection)
        df = db_obj.getAll()
        car1=df[df._id==location['_id']]
        car2=df[df._id!=location['_id']]
        self.value = self.processData(car1,car2)
        print("valor: ", self.value)'''

    def __init__(self,data):
        
        self.insertDataIPFS(data)
        
        location = loads(data)

        d = self.getDataIPFS()
        df = pd.read_json(d)
        car1=df[location['_id']]
        car2=df[df.columns.difference([location['_id']])]
        self.value = self.processData(car1,car2)
        print("valor: ", self.value)
        
    def insertDataIPFS(self,data):
        b_obj = BytesIO() 
        crl = pycurl.Curl() 

        crl.setopt(crl.URL, 'https://localhost:3000/db/carnavigation2/put')
        crl.setopt(crl.SSL_VERIFYPEER, 0)
        crl.setopt(crl.WRITEFUNCTION, b_obj.write)
        crl.setopt(crl.HTTPHEADER, ['Content-Type: application/json'])
        crl.setopt(pycurl.POST, 1)
        json = loads(data)
        #body_as_json_string = dumps(loads(data))# dict to json
        #print(data["_id"])
        #print(json['_id'])
        new_json = {"key":json['_id'], "value": json}
        body_as_json_string = dumps(new_json)
        print(body_as_json_string)
        body_as_file_object = StringIO(body_as_json_string)
        crl.setopt(pycurl.READDATA, body_as_file_object) 
        crl.setopt(pycurl.POSTFIELDSIZE, len(body_as_json_string))

        crl.perform()
        crl.close()

        get_body = b_obj.getvalue()

        print('Output of GET request:\n%s' % get_body.decode('utf8'))
    
    def getDataIPFS(self):
        b_obj = BytesIO() 
        crl = pycurl.Curl() 

        crl.setopt(crl.URL, 'https://localhost:3000/db/carnavigation2/all')
        crl.setopt(crl.SSL_VERIFYPEER, 0)
        crl.setopt(crl.WRITEDATA, b_obj)
        crl.perform()
        crl.close()

        get_body = b_obj.getvalue().decode('utf8')

        return get_body

    def __str__(self):
        return str(self.value)

    def processData(self,car1,car2):
        #print (float(car1['latitude']))
        #connection = pymysql.connect(host='localhost', user='pi', password='raspberry', db='carnavigation', charset='utf8mb4', cursorclass=pymysql.cursors.DictCursor)
        #df = pd.read_sql('SELECT * FROM car', con=connection)
        #car1=df[df._id=='100']
        #car2=df[df._id!='100']
        if(len(car2) > 0 and 'latitude' in car1):
            for col in car2.columns:
                    if float(car1['speed']) < 90:
                        radius_max = 0.80 # kilometros
                    else:
                        radius_max = 0.80
                    radius_min = 0.01
                    distance = car.haversine(float(car1['latitude']), float(car1['longitude']), float(car2[col].latitude), float(car2[col].longitude))	#Distancia entre los dos coches
                    print ("distancia:",distance)
                    if distance <= radius_max and distance > radius_min:
                        if self.nearby(float(car1['latitude']), float(car1['longitude']), float(car1['latitude_old']), float(car1['longitude_old']), float(car2[col].latitude), float(car2[col].longitude), float(car2[col].latitude_old), float(car2[col].longitude_old)):
                            print ('near')
                            #carConfiguration = filter(lambda y: y._id == x._id, Configuration.objects.all())
                            direction = car.get_direction(float(car1['latitude']), float(car1['longitude']), float(car2[col].latitude), float(car2[col].longitude), float(car1['latitude_old']), float(car1['longitude_old']), float(car2[col].latitude_old), float(car2[col].longitude_old))
                            print (direction)
                            isBehind = self.behind(float(car1['latitude']), float(car1['longitude']), float(car2[col].latitude), float(car2[col].longitude), float(car1['latitude_old']), float(car1['longitude_old']), direction)
                            x =  { "direccion":str(direction), "behind":str(isBehind), "distance":str(distance), "speed":car1['speed']}
                            return x

                        else:
                            print ('far')
                            return 0
                    else:
                        print ('lejos')
                        return 0
                    return str(distance)

    def haversine(lat1, lon1, lat2, lon2):
        """
        cálculo de la distancia de círculo máximo entre dos puntos de un globo
        sabiendo su longitud y su latitud.
        """
        # decimales a radianes
        lon1, lat1, lon2, lat2 = map(radians, [lon1, lat1, lon2, lat2])

        # haversine formula
        dlon = lon2 - lon1
        dlat = lat2 - lat1
        a = sin(dlat/2)**2 + cos(lat1) * cos(lat2) * sin(dlon/2)**2
        c = 2 * atan2(sqrt(a),sqrt(1-a))
        r = 6371 # Radio de la tierra en kilometros.
        return c * r

    def nearby(self, lat1, lon1, lat1_old, lon1_old, lat2, lon2, lat2_old, lon2_old):
        #print(lat1, lon1, lat1_old, lon1_old, lat2, lon2, lat2_old, lon2_old)
        """
        Comprobacion de si el coche2 esta mas cerca del coche1
        que en un momento anterior
        """
        dis = car.haversine(lat2,lon2,lat1, lon1)
        dis_old = car.haversine(lat2_old,lon2_old,lat1_old,lon1_old)
        print(dis)
        print(dis_old)	
        if(dis < dis_old):
            return True
        else:
            return False


    def behind(self, lat1, lon1, lat2, lon2, lat1_old, lon1_old, direction):
        """
        Comprobacion de si el coche2 esta detras del coche1
        """
        if(direction == 0):
            d_lat1 = lat1-lat1_old
            d_lon1 = lon1-lon1_old
            d_lat = lat2-lat1
            d_lon = lon2-lon1

            if(d_lat1 > 0 and d_lon1 > 0): 	
                if(d_lat > 0 and d_lon > 0):
                    return True
                else:
                    return False
            elif(d_lat1 < 0 and d_lon1 < 0):
                if(d_lat < 0 and d_lon < 0):
                    return True
                else:
                    return False
            elif(d_lat1 > 0 and d_lon1 < 0):
                if(d_lat > 0 and d_lon < 0):
                    return True
                else:
                    return False
            elif(d_lat1 < 0 and d_lon1 > 0): 		
                if(d_lat < 0 and d_lon > 0):
                    return True
                else:
                    return False	
        else:
            return None


    def get_direction(lat1, lon1, lat2, lon2, lat1_old, lon1_old, lat2_old, lon2_old):
        #print(lat1, lon1, lat2, lon2, lat1_old, lon1_old, lat2_old, lon2_old)
        """
        Calculo de la direccion de un coche respecto a otro
        0 -> Misma direccion
        1 -> Direcciones opuestas
        2 -> Direcciones que se cruzan
        """
        d_lat1 = lat1-lat1_old
        d_lon1 = lon1-lon1_old
        d_lat2 = lat2-lat2_old
        d_lon2 = lon2-lon2_old

        if(d_lat1 > 0 and d_lon1 > 0):
            if(d_lat2 < 0 and d_lon2 < 0):
                return 1
            elif(d_lat2 > 0 and d_lon2 < 0):
                return 2
            elif(d_lat2 < 0 and d_lon2 > 0):
                return 2
            elif(d_lat2 > 0 and d_lon2 > 0):
                return 0
        elif(d_lat1 < 0 and d_lon1 < 0):
            if(d_lat2 > 0 and d_lon2 > 0):
                return 1
            elif(d_lat2 > 0 and d_lon2 < 0):
                return 2
            elif(d_lat2 < 0 and d_lon2 > 0):
                return 2
            elif(d_lat2 < 0 and d_lon2 < 0):
                return 0
        elif(d_lat1 > 0 and d_lon1 < 0):
            if(d_lat2 > 0 and d_lon2 > 0):
                return 2
            elif(d_lat2 < 0 and d_lon2 < 0):
                return 2
            elif(d_lat2 < 0 and d_lon2 > 0):
                return 1
            elif(d_lat2 > 0 and d_lon2 < 0):
                return 0
        elif(d_lat1 < 0 and d_lon1 > 0):
            if(d_lat2 > 0 and d_lon2 > 0):
                return 2
            elif(d_lat2 < 0 and d_lon2 < 0):
                return 2
            elif(d_lat2 > 0 and d_lon2 < 0):
                return 1
            elif(d_lat2 < 0 and d_lon2 > 0):
                return 0
	
