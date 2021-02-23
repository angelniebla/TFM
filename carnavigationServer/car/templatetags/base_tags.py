from car.models import Car
from car.models import Sensor
from django import template

register = template.Library()

@register.inclusion_tag('admin/show_map.html')
def show_map():
	cars = Car.objects.all()
	nodes = Sensor.objects.all()
	return {'car_list': cars, 'node_list': nodes}
