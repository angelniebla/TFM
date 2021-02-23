# -*- coding: utf-8 -*-
from __future__ import unicode_literals

from django.db import models


# Create your models here.

class Car(models.Model):
	uid = models.CharField(max_length=100)
	speed = models.CharField(max_length=50)
	latitude = models.CharField(max_length=50)
	longitude = models.CharField(max_length=50)
	date = models.DateTimeField(auto_now=True)
	latitude_old = models.CharField(max_length=50)
	longitude_old = models.CharField(max_length=50)

class Sensor(models.Model):
	nid = models.CharField(max_length=100)
	latitude = models.CharField(max_length=50)
	longitude = models.CharField(max_length=50)
	wind_speed = models.BooleanField()
	fog = models.BooleanField()
	date = models.DateTimeField(auto_now=True)