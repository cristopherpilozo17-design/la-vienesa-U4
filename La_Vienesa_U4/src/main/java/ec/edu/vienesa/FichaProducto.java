package ec.edu.vienesa;

import jakarta.persistence.*;

@Entity
@Table(name = "ficha_producto")
public class FichaProducto {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "producto_id", nullable = false, unique = true)
    private Producto producto;

    @Column(nullable = false)
    private String conservacion;

    protected FichaProducto() {}

    FichaProducto(Producto producto, String conservacion) {
        this.producto = producto;
        this.conservacion = conservacion;
    }
}
