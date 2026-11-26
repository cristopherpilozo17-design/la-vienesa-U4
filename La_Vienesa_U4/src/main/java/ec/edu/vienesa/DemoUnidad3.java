package ec.edu.vienesa;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.IntFunction;

public final class DemoUnidad3 {
    private static final int TAM = 5;
    private static final LocalDateTime DESDE = LocalDateTime.of(2026, 11, 1, 0, 0);

    private DemoUnidad3() {}

    public static void main(String[] args) {
        Map<String, Object> config = Map.of(
            "jakarta.persistence.jdbc.driver", "org.h2.Driver",
            "jakarta.persistence.jdbc.url", "jdbc:h2:mem:vienesa_u3;MODE=PostgreSQL",
            "jakarta.persistence.jdbc.user", "sa",
            "jakarta.persistence.jdbc.password", "",
            "hibernate.hbm2ddl.auto", "create-drop",
            "hibernate.show_sql", "false");
        try (EntityManagerFactory emf = Persistence.createEntityManagerFactory("vienesaPU", config);
             EntityManager em = emf.createEntityManager()) {
            em.getTransaction().begin();
            try {
                sembrar(em);
                em.getTransaction().commit();
            } catch (RuntimeException error) {
                if (em.getTransaction().isActive()) em.getTransaction().rollback();
                throw error;
            }
            em.clear();

            System.out.println("LA VIENESA | Unidad 3 | Hibernate / H2");
            System.out.println("20 productos de prueba | 5 filas por página");
            mostrar("1. Catálogo activo de Embutidos",
                pagina -> ConsultasU3.catalogo(em, "Embutidos", pagina, TAM),
                "%s | USD %s | %s");
            mostrar("2. Líneas de venta desde 01/11/2026",
                pagina -> ConsultasU3.ventas(em, DESDE, pagina, TAM),
                "Factura %02d | %s | %s | %d ud.");
            mostrar("3. Demanda de Embutidos",
                pagina -> ConsultasU3.demanda(em, "Embutidos", pagina, TAM),
                "%s | %d ud.");
        }
    }

    private static void mostrar(String titulo, IntFunction<List<Object[]>> consultar, String formato) {
        System.out.println();
        System.out.println(titulo);
        for (int pagina = 0; pagina < 2; pagina++) {
            List<Object[]> filas = consultar.apply(pagina);
            System.out.printf("Página %d | desplazamiento %d | %d filas%n", pagina + 1, pagina * TAM, filas.size());
            for (Object[] fila : filas) {
                System.out.printf(Locale.ROOT, "  " + formato + "%n", fila);
            }
        }
    }

    static void sembrar(EntityManager em) {

        Categoria embutidos = new Categoria("Embutidos");
        Categoria ahumados = new Categoria("Ahumados");
        Cliente ana = new Cliente("Ana Demo", "ana@example.com", "0990000001");
        Cliente luis = new Cliente("Luis Demo", "luis@example.com", "0990000002");
        for (Object entidad : List.of(embutidos, ahumados, ana, luis)) em.persist(entidad);
        String[] nombres = {"Chorizo criollo", "Chorizo parrillero", "Jamon cocido",
            "Jamon de pavo", "Longaniza", "Mortadela", "Salami", "Salchicha de pollo",
            "Salchicha frankfurt", "Salchicha Vienesa", "Salchichon", "Tocino curado",
            "Chorizo ahumado", "Costilla ahumada", "Jamon ahumado", "Lomo ahumado",
            "Panceta ahumada", "Pavo ahumado", "Pollo ahumado", "Tocino ahumado"};
        for (int i = 0; i < nombres.length; i++) {
            Producto producto = new Producto(nombres[i], "paquete", BigDecimal.valueOf(250 + 25L*i, 2), 30);
            (i < 12 ? embutidos : ahumados).agregarProducto(producto);
            em.persist(producto);
            em.persist(new FichaProducto(producto, "Refrigerado"));
            if (i < 12) {
                Factura factura = new Factura();
                factura.setFecha(DESDE.plusDays(i));
                (i % 2 == 0 ? ana : luis).agregarFactura(factura);
                factura.agregarDetalle(new DetalleFactura(producto, 12 - i));
                em.persist(factura);
            }
        }


    }
}

