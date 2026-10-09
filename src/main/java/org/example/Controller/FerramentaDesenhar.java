package org.example.Controller;

import javafx.scene.input.MouseEvent;

import org.example.Models.Ponto;
import org.example.Models.Poligono;

public class FerramentaDesenhar implements Ferramenta {

    @Override
    public void aoPressionarMouse(
            MouseEvent evento,
            EditorContext contexto) {
    }

    @Override
    public void aoArrastarMouse(
            MouseEvent evento,
            EditorContext contexto) {
    }

    @Override
    public void aoSoltarMouse(
            MouseEvent evento,
            EditorContext contexto) {
    }

    @Override
    public void aoClicar(
            MouseEvent evento,
            EditorContext contexto) {

        // Aplica o Snap às coordenadas do mouse.
        double x = contexto.aplicarSnap(evento.getX());
        double y = contexto.aplicarSnap(evento.getY());

        if (contexto.getFormaEmAndamento() == null) {

            // Primeiro clique: inicia o polígono.
            Poligono poligono = new Poligono();

            poligono.adicionarPonto(
                    new Ponto(x, y)
            );

            contexto.setFormaEmAndamento(poligono);

        } else {

            Poligono poligono =
                    (Poligono) contexto.getFormaEmAndamento();

            // Duplo clique: finaliza se houver pelo menos
            // três pontos.
            if (evento.getClickCount() == 2) {

                if (poligono.getQuantidadePontos() >= 3) {
                    contexto.finalizarFormaEmAndamento();
                }

            } else {

                // Clique normal: adiciona um vértice.
                poligono.adicionarPonto(
                        new Ponto(x, y)
                );
            }
        }

        contexto.requestRedraw();
        contexto.requestStatsUpdate();
    }
}