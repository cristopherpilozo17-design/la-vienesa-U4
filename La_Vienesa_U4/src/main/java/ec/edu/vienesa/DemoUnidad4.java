package ec.edu.vienesa;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.OptimisticLockException;
import jakarta.persistence.Persistence;
import java.math.BigDecimal;
import java.util.Map;

public final class DemoUnidad4 {
    private DemoUnidad4() {}

    public static void main(String[] args) {
        Map<String, Object> config = Map.of(
            "jakarta.persistence.jdbc.driver", "org.h2.Driver",
            "jakarta.persistence.jdbc.url", "jdbc:h2:mem:vienesa_u4;MODE=PostgreSQL",
            "jakarta.persistence.jdbc.user", "sa",
            "jakarta.persistence.jdbc.password", "",
            "hibernate.hbm2ddl.auto", "create-drop",
            "hibernate.show_sql", "false"
        );
        try (EntityManagerFactory emf = Persistence.createEntityManagerFactory("vienesaPU", config)) {
            ProductoRepository productos = new ProductoRepository(emf);
            long id = productos.crear("Pan de la casa", "Unidad", new BigDecimal("1.00"), 10).getId();
            simularDosVentas(emf, id);
            int stock = productos.listar().get(0).getStock();
            if (stock != 8) {
                throw new IllegalStateException("Se esperaban 8 unidades, se encontraron " + stock);
            }
            System.out.println("Stock final: " + stock);
        }
    }

    private static void simularDosVentas(EntityManagerFactory emf, long id) {
        try (EntityManager primera = emf.createEntityManager();
             EntityManager segunda = emf.createEntityManager()) {
            primera.getTransaction().begin();
            segunda.getTransaction().begin();
            try {
                Producto productoA = primera.find(Producto.class, id);
                Producto productoB = segunda.find(Producto.class, id);
                System.out.printf("Las dos ventas leen el stock: %d y %d%n",
                    productoA.getStock(), productoB.getStock());

                productoA.setStock(productoA.getStock() - 1);
                primera.getTransaction().commit();
                System.out.println("Venta 1 guardada. Stock: 9");

                productoB.setStock(productoB.getStock() - 1);
                try {
                    segunda.flush();
                    throw new IllegalStateException("Se esperaba un conflicto de versión.");
                } catch (OptimisticLockException conflicto) {
                    System.out.println("Venta 2 detectó que el producto había cambiado.");
                }
            } finally {
                revertirSiActiva(segunda);
                revertirSiActiva(primera);
            }
        }

        // El reintento abre una transacción nueva y vuelve a leer el stock.
        try (EntityManager reintento = emf.createEntityManager()) {
            reintento.getTransaction().begin();
            try {
                Producto actualizado = reintento.find(Producto.class, id);
                actualizado.setStock(actualizado.getStock() - 1);
                reintento.getTransaction().commit();
                System.out.println("Venta 2 guardada después de consultar el stock actualizado.");
            } finally {
                revertirSiActiva(reintento);
            }
        }
    }

    private static void revertirSiActiva(EntityManager em) {
        if (em.getTransaction().isActive()) {
            em.getTransaction().rollback();
        }
    }
}

