package org.example.Controller;

import javafx.scene.input.MouseEvent;

import org.example.Models.FormaGeometrica;
import org.example.Models.Poligono;
import org.example.Models.Ponto;

public class FerramentaEditarVertice implements Ferramenta {

    private Ponto verticeSelecionado;

    private static final double TOLERANCIA = 10.0;

    @Override
    public void aoPressionarMouse(
            MouseEvent evento,
            EditorContext contexto) {

        double x = evento.getX();
        double y = evento.getY();

        verticeSelecionado = null;

        double menorDistancia = TOLERANCIA;

        // Procura vértices nos polígonos selecionados.
        for (FormaGeometrica forma :
                contexto.getSelecionados()) {

            if (!(forma instanceof Poligono)) {
                continue;
            }

            for (Ponto ponto : forma.getPontos()) {

                double distancia = Math.hypot(
                        x - ponto.getX(),
                        y - ponto.getY()
                );

                if (distancia <= menorDistancia) {

                    menorDistancia = distancia;
                    verticeSelecionado = ponto;
                }
            }
        }

        contexto.requestRedraw();
    }

    @Override
    public void aoArrastarMouse(
            MouseEvent evento,
            EditorContext contexto) {

        if (verticeSelecionado == null) {
            return;
        }

        verticeSelecionado.setX(
                contexto.aplicarSnap(evento.getX())
        );

        verticeSelecionado.setY(
                contexto.aplicarSnap(evento.getY())
        );

        contexto.requestRedraw();
        contexto.requestStatsUpdate();
    }

    @Override
    public void aoSoltarMouse(
            MouseEvent evento,
            EditorContext contexto) {

        verticeSelecionado = null;

        contexto.requestRedraw();
        contexto.requestStatsUpdate();
    }

    @Override
    public void aoClicar(
            MouseEvent evento,
            EditorContext contexto) {
    }

    public Ponto getVerticeSelecionado() {
        return verticeSelecionado;
    }
}