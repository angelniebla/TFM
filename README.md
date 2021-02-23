# TFM

Vídeos de las pruebas realizadas https://drive.google.com/drive/folders/1Z_zpLJbL3SlwHXcAg2gRSNmECzR1p6SZ?usp=sharing

## Código del TFM con título Sistema de prevención de colisiones basado en la comunicación con infraestructuras del alumno Ángel Niebla Montero

El contenido del repositorio es el siguiente:
- CarNavigation (Aplicación Android)
  - Activities/: actividades de la aplicación.
  - Adapters/: archivos necesarios para el RecyclerView, como son el adapter, viewHolder y un helper.
  - CameraUtils/: archivos necesarios para el procesado de las imágenes en tiempo real con la camara.
  - Communicators/: clases que ejecutan tareas en segundo plano para la comunicación TCP y Bluetooth.
  - Fragments/: fragmentos de la aplicación.
  - GlobalVars/: fichero que almacena las variables globales que se usan en la aplicación.
  - Listeners/: listeners encargados de interceptar los eventos correspondientes a las peticiones al servidor.
  - LocaleManager/: fichero necesario para el cambio de idioma en la aplicación.
  - Model/: clases de los objetos necesarios en la aplicación.
  - SharedPreference/: fichero que almacena los conjuntos clave-valor que se usan en la aplicación.
  - Volley/: archivos necesarios para el uso de la librería Volley en las conexiones HTTP con el servidor.
  
- CarNavigationServer (Servidor cloud)}
	- car
		- admin.py: contiene los objetos habilitados para ser gestionados por el administrador.
		- models.py: objetos creados que necesitan ser almacenados en base de datos.
		- serializers.py: serializers para cada objeto que necesite serializar y deserializar las instancias de fragmentos en representaciones json.
		- views.py: se realiza el procesado de la información recibida.
		- templates/: plantillas HTML modificadas para la interfaz del administrador.
		- templatetags/: contiene el tag para la plantilla que muestra el mapa para el administrador.
	- carnavigation/
		- settings.py: configuración de la aplicación Django.
		- urls.py: configuración de las URL raíz del proyecto.
- CarNavigationBluetoothServer(Servidor del nodo)
  - main_server: contiene el controlador del servidor
  - bluetooth_server: contiene el código para el manejo de cada conexión bluetooth.
  - carnavigation: contiene el código que procesa la información recibida por la conexión bluetooth abierta.

- Sensor.py : archivo con el código del nodo que recoge la información de los sensores y la envía.
