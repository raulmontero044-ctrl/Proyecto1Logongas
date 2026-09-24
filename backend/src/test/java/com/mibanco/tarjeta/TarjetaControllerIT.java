package com.mibanco.tarjeta;

import com.mibanco.tarjeta.repository.TarjetaRepository;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasItems;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class TarjetaControllerIT {

    @LocalServerPort
    private int port;

    @Autowired
    private TarjetaRepository tarjetaRepository;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
        tarjetaRepository.deleteAll();
        tarjetaRepository.flush();
    }

    @Test
    void crearTarjetaConTipoEnMinusculasResponde201ConTipoCanonicoYEsConsultable() {
        Integer id = given()
                .contentType(ContentType.JSON)
                .body("""
                        {"nombrePropietario":"María López","tipo":"débito"}
                        """)
                .when()
                .post("/api/tarjetas")
                .then()
                .statusCode(201)
                .body("id", greaterThan(0))
                .body("nombrePropietario", equalTo("María López"))
                .body("tipo", equalTo("Débito"))
                .extract()
                .path("id");

        given()
                .when()
                .get("/api/tarjetas/{id}", id)
                .then()
                .statusCode(200)
                .body("id", equalTo(id))
                .body("nombrePropietario", equalTo("María López"))
                .body("tipo", equalTo("Débito"));
    }

    @Test
    void crearTarjetaConTipoInexistenteResponde400ConTIPO_NO_EXISTE() {
        given()
                .contentType(ContentType.JSON)
                .body("""
                        {"nombrePropietario":"Ana Pérez","tipo":"Bienvenida"}
                        """)
                .when()
                .post("/api/tarjetas")
                .then()
                .statusCode(400)
                .body("codigo", equalTo("TIPO_NO_EXISTE"))
                .body("mensaje", equalTo("El tipo de tarjeta no existe"));
    }

    @Test
    void crearTarjetaConNombreVacioResponde400ConNOMBRE_REQUERIDO() {
        given()
                .contentType(ContentType.JSON)
                .body("""
                        {"nombrePropietario":"   ","tipo":"Débito"}
                        """)
                .when()
                .post("/api/tarjetas")
                .then()
                .statusCode(400)
                .body("codigo", equalTo("NOMBRE_REQUERIDO"))
                .body("mensaje", equalTo("El nombre del propietario es obligatorio"));
    }

    @Test
    void listarTiposDeTarjetaResponde200ConDebitoYCredito() {
        given()
                .when()
                .get("/api/tipos-tarjetas")
                .then()
                .statusCode(200)
                .body("nombre", hasItems("Débito", "Crédito"));
    }

    @Test
    void consultarTarjetaInexistenteResponde404ConTARJETA_NO_ENCONTRADA() {
        given()
                .when()
                .get("/api/tarjetas/999999")
                .then()
                .statusCode(404)
                .body("codigo", equalTo("TARJETA_NO_ENCONTRADA"))
                .body("mensaje", equalTo("Tarjeta no encontrada"));
    }
}