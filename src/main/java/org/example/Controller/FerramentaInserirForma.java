package org.example.Controller;

import javafx.scene.input.MouseEvent;

import org.example.Models.Circulo;
import org.example.Models.FormaGeometrica;
import org.example.Models.Hexagono;
import org.example.Models.Ponto;
import org.example.Models.Quadrado;

public class FerramentaInserirForma implements Ferramenta {

    public enum TipoForma {
        CIRCULO,
        QUADRADO,
        HEXAGONO
    }

    private final TipoForma tipoForma;

    private Ponto centroInsercao;

    public FerramentaInserirForma(
            TipoForma tipoForma) {

        this.tipoForma = tipoForma;
    }

    @Override
    public void aoPressionarMouse(
            MouseEvent evento,
            EditorContext contexto) {

        double x = contexto.aplicarSnap(evento.getX());
        double y = contexto.aplicarSnap(evento.getY());

        centroInsercao = new Ponto(x, y);

        contexto.setFormaEmAndamento(null);
    }

    @Override
    public void aoArrastarMouse(
            MouseEvent evento,
            EditorContext contexto) {

        if (centroInsercao == null) {
            return;
        }

        double x = contexto.aplicarSnap(evento.getX());
        double y = contexto.aplicarSnap(evento.getY());

        double dx = x - centroInsercao.getX();
        double dy = y - centroInsercao.getY();

        double raio = Math.hypot(dx, dy);

        if (raio <= 0) {

            contexto.setFormaEmAndamento(null);
            contexto.requestRedraw();

            return;
        }

        contexto.setFormaEmAndamento(
                criarForma(centroInsercao, raio)
        );

        contexto.requestRedraw();
        contexto.requestStatsUpdate();
    }

    @Override
    public void aoSoltarMouse(
            MouseEvent evento,
            EditorContext contexto) {

        if (centroInsercao != null) {

            // Calcula novamente o tamanho usando a posição
            // final do mouse, também com Snap.
            double x = contexto.aplicarSnap(evento.getX());
            double y = contexto.aplicarSnap(evento.getY());

            double dx = x - centroInsercao.getX();
            double dy = y - centroInsercao.getY();

            double raio = Math.hypot(dx, dy);

            if (raio > 0) {

                contexto.setFormaEmAndamento(
                        criarForma(centroInsercao, raio)
                );

                contexto.finalizarFormaEmAndamento();

            } else {

                contexto.setFormaEmAndamento(null);
            }
        }

        centroInsercao = null;

        contexto.requestRedraw();
        contexto.requestStatsUpdate();
    }

    private FormaGeometrica criarForma(
            Ponto centro,
            double raio) {

        return switch (tipoForma) {

            case CIRCULO ->
                    new Circulo(centro, raio);

            case QUADRADO ->
                    new Quadrado(centro, raio);

            case HEXAGONO ->
                    new Hexagono(centro, raio);
        };
    }

    @Override
    public void aoClicar(
            MouseEvent evento,
            EditorContext contexto) {
    }
}