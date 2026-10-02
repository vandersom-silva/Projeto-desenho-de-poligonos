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

        double x = evento.getX();
        double y = evento.getY();

        // Primeiro clique: cria um novo polígono
        if (contexto.getFormaEmAndamento() == null) {

            Poligono poligono = new Poligono();

            poligono.adicionarPonto(
                    new Ponto(x, y)
            );

            contexto.setFormaEmAndamento(poligono);

        } else {

            Poligono poligono =
                    (Poligono) contexto.getFormaEmAndamento();

            // Duplo clique: tenta finalizar o polígono
            if (evento.getClickCount() == 2) {

                // O polígono precisa ter pelo menos 3 pontos
                if (poligono.getQuantidadePontos() >= 3) {

                    contexto.finalizarFormaEmAndamento();
                }

            } else {

                // Clique normal: adiciona um novo vértice
                poligono.adicionarPonto(
                        new Ponto(x, y)
                );
            }
        }

        contexto.requestRedraw();
        contexto.requestStatsUpdate();
    }
}