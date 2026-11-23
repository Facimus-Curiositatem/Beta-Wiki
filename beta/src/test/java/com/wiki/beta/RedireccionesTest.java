package com.wiki.beta;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Con Tomcat real: detras de un proxy HTTPS (Tailscale Funnel) una redireccion absoluta saldria con http y el
 * puerto equivocado, asi que las redirecciones deben ser relativas y sin jsessionid en la URL.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class RedireccionesTest {

    private final HttpClient cliente = HttpClient.newBuilder().followRedirects(HttpClient.Redirect.NEVER).build();

    @Value("${local.server.port}")
    private int puerto;

    @Test
    void documentoInexistente_redirigeConUbicacionRelativa() throws Exception {
        HttpResponse<Void> respuesta = cliente.send(
                HttpRequest.newBuilder(URI.create("http://localhost:" + puerto + "/documentos/no-existe")).build(),
                HttpResponse.BodyHandlers.discarding());

        assertThat(respuesta.statusCode()).isEqualTo(302);
        assertThat(respuesta.headers().firstValue("Location")).hasValue("/documentos");
    }

    @Test
    void formularioDeContacto_redirigeSinJsessionidEnLaUrl() throws Exception {
        String formulario = "nombre=Ana&email=ana%40example.com&telefono=3000000000&asunto=Consulta&mensaje=Hola";
        HttpResponse<Void> respuesta = cliente.send(
                HttpRequest.newBuilder(URI.create("http://localhost:" + puerto + "/contactenos"))
                        .header("Content-Type", "application/x-www-form-urlencoded")
                        .POST(HttpRequest.BodyPublishers.ofString(formulario))
                        .build(),
                HttpResponse.BodyHandlers.discarding());

        assertThat(respuesta.statusCode()).isEqualTo(302);
        assertThat(respuesta.headers().firstValue("Location")).hasValue("/contactenos");
    }
}
