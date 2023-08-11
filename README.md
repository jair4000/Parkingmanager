# ParkingManager

### Descripción


El sistema permite registrar la entrada y salida de vehículos en un parqueadero. Para
cada vehículo que ingresa al parqueadero, se llevará un registro que contendrá la siguiente
información:

* Tipo de vehículo (automóvil, motocicleta, camión, etc.).
```
    AUTOMOVIL(1 , "automovil"),
    MOTOCICLETA (2, "motocicleta"),
    CAMION(3, "camion"),
    OTRO(4, "otro");
```
* Placa del vehículo.
* Hora de entrada.

Cuando un vehículo sale del parqueadero, se registrará la hora de salida y se calculará el
tiempo de permanencia.

### Requisitos
The following guides illustrate how to use some features concretely:

* puerto 9091 disponible
* version de java 17

### Consumo de servicios

* localhost:9091/newParking

permite el ingreso de un vehiculo al parqueadero 
```
{
"plate":"ABC123",
"vehicleType":"2"
}
```
* localhost:9091/finishParking

permite la salida de un vehiculo al parqueadero
```
{
"plate":"ABC123"
}
```

devuelve el promedio de permanencia en el parqueadero por tipo de vehiculo

* localhost:9091/getAverageStayedTime

devuelve el vehiculo que ha estado mas tiempo en el parqueadero

* localhost:9091/getLongestStayedTime


