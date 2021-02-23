from rest_framework import serializers
from .models import Car
from .models import Credential
from .models import Configuration
from .models import Sensor

class CarSerializer(serializers.Serializer):
	pk = serializers.IntegerField(read_only=True)
	uid = serializers.CharField()
	speed = serializers.CharField()
	latitude = serializers.CharField()
	longitude = serializers.CharField()
	latitude_old = serializers.CharField(default='0')
	longitude_old = serializers.CharField(default='0')
	
	def create(self, validated_data):
		return Car.objects.create(**validated_data)
		
	def update(self, instance, validated_data):
		instance.uid = validated_data.get('uid', instance.uid)
		instance.tokenId = validated_data.get('tokenId', instance.uid)
		instance.speed = validated_data.get('speed', instance.speed)
		instance.latitude = validated_data.get('latitude', instance.latitude)
		instance.longitude = validated_data.get('longitude', instance.longitude)
		instance.latitude_old = validated_data.get('latitude_old', instance.latitude_old)
		instance.longitude_old = validated_data.get('longitude_old', instance.longitude_old)
		instance.save()
		return instance
		
	class Meta:
		model = Car
		fields = ('id', 'uid', 'tokenId', 'speed', 'latitude', 'longitude','latitude_old', 'longitude_old')

		
class SensorSerializer(serializers.Serializer):
	nid = serializers.CharField()
	latitude = serializers.CharField()
	longitude = serializers.CharField()
	wind_speed = serializers.BooleanField()
	fog = serializers.BooleanField()

	
	def create(self, validated_data):
		return Sensor.objects.create(**validated_data)
		
	def update(self, instance, validated_data):
		instance.nid = validated_data.get('nid', instance.nid)
		instance.latitude = validated_data.get('latitude', instance.latitude)
		instance.longitude = validated_data.get('longitude', instance.longitude)
		instance.wind_speed = validated_data.get('wind_speed', instance.wind_speed)
		instance.fog = validated_data.get('fog', instance.fog)
		instance.save()
		return instance
		
	class Meta:
		model = Sensor
		fields = ('nid', 'latitude', 'longitude', 'wind_speed', 'fog')

