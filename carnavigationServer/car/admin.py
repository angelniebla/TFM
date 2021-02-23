# -*- coding: utf-8 -*-
from __future__ import unicode_literals

from django.contrib import admin
from car.models import *
from django.conf import settings
from car.forms import PollsForm


from django.forms import Widget

# Register your models here.

@admin.register(Car)
class CarAdmin(admin.ModelAdmin):
	list_display = ('id', 'uid', 'speed', 'latitude', 'longitude', 'latitude_old', 'longitude_old', 'date')
	search_fields = ('id',)
	
@admin.register(Sensor)
class SensorAdmin(admin.ModelAdmin):
	list_display = ('nid', 'latitude', 'longitude', 'wind_speed', 'fog', 'date')