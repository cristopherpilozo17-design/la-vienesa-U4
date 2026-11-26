package ec.edu.vienesa;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import java.time.LocalDateTime;
import java.util.List;

public final class ConsultasU3 {
    static final String CATALOGO = """
        SELECT p.nombre, p.precio, fp.conservacion
        FROM FichaProducto fp JOIN fp.producto p JOIN p.categoria c
        WHERE c.nombre = :categoria AND p.activo = true
        ORDER BY p.nombre, p.id
        """;
    static final String VENTAS = """
        SELECT f.id, cl.nombre, p.nombre, d.cantidad
        FROM DetalleFactura d JOIN d.factura f JOIN f.cliente cl
        JOIN d.producto p WHERE f.fecha >= :desde
        ORDER BY f.fecha DESC, f.id DESC, d.id
        """;
    static final String DEMANDA = """
        SELECT p.nombre, SUM(d.cantidad) AS unidades
        FROM DetalleFactura d JOIN d.producto p JOIN p.categoria c
        WHERE c.nombre = :categoria
        GROUP BY p.id, p.nombre ORDER BY unidades DESC, p.id
        """;

    private ConsultasU3() {}

    public static List<Object[]> catalogo(EntityManager em, String categoria, int pagina, int tamano) {
        return paginar(em.createQuery(CATALOGO, Object[].class)
            .setParameter("categoria", categoria), pagina, tamano);
    }

    public static List<Object[]> ventas(EntityManager em, LocalDateTime desde, int pagina, int tamano) {
        return paginar(em.createQuery(VENTAS, Object[].class)
            .setParameter("desde", desde), pagina, tamano);
    }

    public static List<Object[]> demanda(EntityManager em, String categoria, int pagina, int tamano) {
        return paginar(em.createQuery(DEMANDA, Object[].class)
            .setParameter("categoria", categoria), pagina, tamano);
    }

    private static List<Object[]> paginar(TypedQuery<Object[]> consulta, int pagina, int tamano) {
        if (pagina < 0 || tamano < 1) {
            throw new IllegalArgumentException("La página empieza en cero y el tamaño debe ser positivo.");
        }
        return consulta.setFirstResult(Math.multiplyExact(pagina, tamano))
            .setMaxResults(tamano)
            .getResultList();
    }
}
