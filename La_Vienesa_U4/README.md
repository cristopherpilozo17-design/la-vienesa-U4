# La Vienesa · Unidad 4

Proyecto de Persistencia de Datos basado en la Unidad 3.

## Cómo ejecutarlo

Usa Java 17 o posterior y Maven. Abre la carpeta que contiene el POM en NetBeans y ejecuta el proyecto, o abre una terminal en esa carpeta y escribe:

```text
mvn test
mvn compile exec:java
```

La demostración usa H2 en memoria. Maven descarga las dependencias si todavía no están guardadas localmente.

## Qué muestra

La clase DemoUnidad4 abre dos transacciones que leen el mismo producto con 10 unidades. La primera guarda su descuento. Hibernate detecta que la segunda leyó una versión anterior de Producto gracias a la anotación @Version. La segunda se revierte y se reintenta en una transacción nueva. El stock final es 8.
