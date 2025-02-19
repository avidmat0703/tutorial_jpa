package org.iesvdm.tutorial;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.iesvdm.tutorial.domain.Heroe;
import org.iesvdm.tutorial.domain.HeroeHasPoder;
import org.iesvdm.tutorial.domain.Mision;
import org.iesvdm.tutorial.domain.Poder;
import org.iesvdm.tutorial.repository.HasPoderRepository;
import org.iesvdm.tutorial.repository.HeroeRepository;
import org.iesvdm.tutorial.repository.MisionRepository;
import org.iesvdm.tutorial.repository.PoderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.annotation.Order;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@SpringBootTest
public class PoderesHeroesTests {

    @Autowired
    HeroeRepository heroeRepository;

    @Autowired
    MisionRepository misionsRepository;

    @Autowired
    HasPoderRepository hasPoderRepository;

    @Autowired
    PoderRepository poderRepository;

    @PersistenceContext
    EntityManager entityManager;
    @Autowired
    private PlatformTransactionManager transactionManager;
    private TransactionTemplate transactionTemplate;

    @BeforeEach
    public void setUp() {
        transactionTemplate = new TransactionTemplate(transactionManager);
    }

    @Test
    @Order(1)
    void crearHeroeMisionPoderTest() {

        // Crear misión
        Mision mision = Mision.builder()
                .descripcion("Aprender angular")
                .villano("Ya sabemos el villano")
                .build();

        // Crear heroe
        Heroe heroe = Heroe.builder()
                .nombre("Maximilian")
                .build();
        this.heroeRepository.save(heroe);

        // Crear poder
        Poder poder = Poder.builder()
                .nombre("Tutoriales free")
                .build();

        this.poderRepository.save(poder);

        // Relación heroe y poder
        HeroeHasPoder heroeHasPoder = HeroeHasPoder.builder()
                .heroe(heroe)
                .poder(poder)
                .build();
        this.hasPoderRepository.save(heroeHasPoder);

        // Asignar poder heroe
        heroe.getPoderes().add(heroeHasPoder);

        // Asignar misión heroe
        heroe.setMision(mision);

        // Asignar heroe a misión
        mision.getHeroes().add(heroe);

        // Guardar misión
        mision = this.misionsRepository.save(mision);

        // Guardar heroe
        this.heroeRepository.save(heroe);
    }

    @Test
    @Order(2)
    void listarTodoDesdeMision() {
        transactionTemplate.executeWithoutResult(transactionStatus -> {
            misionsRepository.findById(1L).ifPresentOrElse(mision -> {
                System.out.println("Misión: " + mision.toString());
            }, () -> {
                System.out.println("La misión con ID 1 no existe.");
            });
        });
    }

    @Test
    @Order(3)
    void desasociarHeroeDeMision() {
        transactionTemplate.executeWithoutResult(transactionStatus -> {

            // Buscar el heroe
            Heroe heroe = heroeRepository.findById(1L).orElse(null);

            if (heroe != null) {

                // Desasociar el heroe de la misión
                heroe.setMision(null);

                // Guardar el heroe
                heroeRepository.save(heroe);
            }
        });
    }

    @Test
    @Order(4)
    void asociarHeroeAMision() {
        transactionTemplate.executeWithoutResult(transactionStatus -> {

            // Buscar el heroe
            Heroe heroe = heroeRepository.findById(1L).orElse(null);

            if (heroe != null) {

                //  misión
                Mision mision = Mision.builder()
                        .descripcion("Ejecutar un servicio web")
                        .villano("VS Code")
                        .build();

                if (mision != null) {

                    // Asociar el heroe a la misión
                    heroe.setMision(mision);

                    // Asignar el heroe a la misión
                    mision.getHeroes().add(heroe);

                    // Guardar la misión
                    mision = this.misionsRepository.save(mision);

                    // Guardar el heroe
                    heroeRepository.save(heroe);
                }
            }
        });
    }

    @Test
    @Order(5)
    void eliminarHeroe() {
        transactionTemplate.executeWithoutResult(transactionStatus -> {

            // Buscar el heroe
            Heroe heroe = heroeRepository.findById(2L).orElse(null);
            if (heroe != null) {

                // Desasociar la misión del héroe
                if (heroe.getMision() != null) {
                    heroe.setMision(null);
                    heroeRepository.save(heroe);
                }

                // Desasociar los poderes del héroe
                for (HeroeHasPoder heroeHasPoder : heroe.getPoderes()) {
                    hasPoderRepository.delete(heroeHasPoder);
                }

                // Eliminar el héroe
                heroeRepository.delete(heroe);
            }
        });
    }
}