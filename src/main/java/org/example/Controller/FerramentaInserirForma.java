package org.example.Controller;

import javafx.scene.input.MouseEvent;
import org.example.Models.Circulo;
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

        centroInsercao =
                new Ponto(
                        evento.getX(),
                        evento.getY()
                );

        contexto.setFormaEmAndamento(null);
    }

    @Override
    public void aoArrastarMouse(
            MouseEvent evento,
            EditorContext contexto) {

        if (centroInsercao == null) {
            return;
        }

        double dx =
                evento.getX()
                        - centroInsercao.getX();

        double dy =
                evento.getY()
                        - centroInsercao.getY();

        double raio =
                Math.sqrt(
                        dx * dx + dy * dy
                );

        if (raio <= 0) {
            return;
        }

        switch (tipoForma) {

            case CIRCULO -> {

                Circulo circulo =
                        new Circulo(
                                centroInsercao,
                                raio
                        );

                contexto.setFormaEmAndamento(
                        circulo
                );
            }

            case QUADRADO -> {

                Quadrado quadrado =
                        new Quadrado(
                                centroInsercao,
                                raio
                        );

                contexto.setFormaEmAndamento(
                        quadrado
                );
            }

            case HEXAGONO -> {

                Hexagono hexagono =
                        new Hexagono(
                                centroInsercao,
                                raio
                        );

                contexto.setFormaEmAndamento(
                        hexagono
                );
            }
        }

        contexto.requestRedraw();
        contexto.requestStatsUpdate();
    }

    @Override
    public void aoSoltarMouse(
            MouseEvent evento,
            EditorContext contexto) {

        if (contexto.getFormaEmAndamento()
                != null) {

            contexto.finalizarFormaEmAndamento();
        }

        centroInsercao = null;

        contexto.requestRedraw();
        contexto.requestStatsUpdate();
    }

    @Override
    public void aoClicar(
            MouseEvent evento,
            EditorContext contexto) {
    }
}