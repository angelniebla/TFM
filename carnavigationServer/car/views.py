# -*- coding: utf-8 -*-
from __future__ import unicode_literals
from math import radians, cos, sin, asin, sqrt
from django.http import HttpResponse
from django.views.decorators.csrf import csrf_exempt
from rest_framework.renderers import JSONRenderer
from rest_framework.parsers import JSONParser
from car.models import Car
from car.models import Credential
from car.models import Configuration
from car.models import Sensor
from car.models import Alert
from car.serializers import CarSerializer
from car.serializers import CredentialSerializer
from car.serializers import ConfigurationSerializer
from car.serializers import SensorSerializer
import requests
import json
from django.utils import timezone
import time

class JSONResponse(HttpResponse):
    """
    An HttpResponse that renders its content into JSON.
    """
    def __init__(self, data, **kwargs):
        content = JSONRenderer().render(data)
        kwargs['content_type'] = 'application/json'
        super(JSONResponse, self).__init__(content, **kwargs)

@csrf_exempt
def car_list(request):	
	if request.method == 'POST':
		data = JSONParser().parse(request)
		data_old = None
		print data['uid']
		for x in Car.objects.all():
			if x.uid == data['uid']:
				if data['latitude'] == x.latitude and data['longitude'] == x.longitude:
					data['latitude_old'] = x.latitude_old
					data['longitude_old'] = x.longitude_old
				else:
					data['latitude_old'] = x.latitude
					data['longitude_old'] = x.longitude
				
				x.delete()
		serializer = CarSerializer(data=data)
		if serializer.is_valid():
			if authorize(data['uid']):
				serializer.save()
				return JSONResponse(serializer.data, status=201)
			return JSONResponse({'status': False, 'message': "Usuario no registrado"}, status=401)
		return JSONResponse(serializer.errors, status=400)
		

def authorize(uid):
	l = filter(lambda y: y.uid == uid, Credential.objects.all())
	return len(l) != 0
		

@csrf_exempt
def sensor(request):
	if request.method == 'POST':
		start = time.time()
		data = JSONParser().parse(request)
		for x in Sensor.objects.all():
			if x.nid == data['nid']:
				x.delete()
		serializer = SensorSerializer(data=data)
		if serializer.is_valid():
			serializer.save()
			finish = time.time() - start
			print 'finish: ' + str(finish)
			return JSONResponse(serializer.data, status=201)
		return JSONResponse(serializer.errors, status=400)