package org.example.Controller;

import javafx.scene.input.MouseEvent;

import org.example.Models.FormaGeometrica;
import org.example.Models.Ponto;

import java.util.List;

public class FerramentaTransformar implements Ferramenta {

    public enum Modo {
        ESCALA,
        ROTACAO,
        CISALHAMENTO
    }

    private final Modo modo;

    private double ultimoX;
    private double ultimoY;

    public FerramentaTransformar(Modo modo) {
        this.modo = modo;
    }

    public Modo getModo() {
        return modo;
    }

    @Override
    public void aoPressionarMouse(
            MouseEvent evento,
            EditorContext contexto) {

        ultimoX = evento.getX();
        ultimoY = evento.getY();
    }

    @Override
    public void aoArrastarMouse(
            MouseEvent evento,
            EditorContext contexto) {

        double x = evento.getX();
        double y = evento.getY();

        double dx = x - ultimoX;
        double dy = y - ultimoY;

        List<FormaGeometrica> alvos =
                contexto.getFormasAfetadasPorSelecao();

        if (alvos.isEmpty()) {
            ultimoX = x;
            ultimoY = y;
            return;
        }

        switch (modo) {

            case ESCALA -> aplicarEscala(alvos, dx);

            case ROTACAO -> aplicarRotacao(alvos, dx);

            case CISALHAMENTO -> aplicarCisalhamento(
                    alvos,
                    dx,
                    dy
            );
        }

        ultimoX = x;
        ultimoY = y;

        contexto.requestRedraw();
        contexto.requestStatsUpdate();
    }

    private void aplicarEscala(
            List<FormaGeometrica> formas,
            double dx) {

        double fator = Math.max(
                0.01,
                1.0 + dx / 200.0
        );

        Ponto centroGrupo =
                calcularCentroConjunto(formas);

        for (FormaGeometrica forma : formas) {

            Ponto centroForma =
                    forma.getCentroide();

            // Posiciona o centro da forma em relação
            // ao centro do conjunto.
            double novoX =
                    centroGrupo.getX()
                            + (centroForma.getX()
                            - centroGrupo.getX()) * fator;

            double novoY =
                    centroGrupo.getY()
                            + (centroForma.getY()
                            - centroGrupo.getY()) * fator;

            forma.transladar(
                    novoX - centroForma.getX(),
                    novoY - centroForma.getY()
            );

            // Escala a geometria individual
            forma.escalar(fator);
        }
    }

    private void aplicarRotacao(
            List<FormaGeometrica> formas,
            double dx) {

        double angulo = dx * 0.01;

        Ponto centroGrupo =
                calcularCentroConjunto(formas);

        double cos = Math.cos(angulo);
        double sin = Math.sin(angulo);

        for (FormaGeometrica forma : formas) {

            Ponto centroForma =
                    forma.getCentroide();

            double dxCentro =
                    centroForma.getX()
                            - centroGrupo.getX();

            double dyCentro =
                    centroForma.getY()
                            - centroGrupo.getY();

            double novoX =
                    centroGrupo.getX()
                            + dxCentro * cos
                            - dyCentro * sin;

            double novoY =
                    centroGrupo.getY()
                            + dxCentro * sin
                            + dyCentro * cos;

            // Move o centro da forma ao redor
            // do centro do conjunto
            forma.transladar(
                    novoX - centroForma.getX(),
                    novoY - centroForma.getY()
            );

            // Rotaciona a própria forma
            forma.rotacionar(angulo);
        }
    }

    private void aplicarCisalhamento(
            List<FormaGeometrica> formas,
            double dx,
            double dy) {

        double shx = dx * 0.005;
        double shy = dy * 0.005;

        for (FormaGeometrica forma : formas) {
            forma.cisalhar(shx, shy);
        }
    }

    private Ponto calcularCentroConjunto(
            List<FormaGeometrica> formas) {

        double somaX = 0;
        double somaY = 0;

        for (FormaGeometrica forma : formas) {

            Ponto centro = forma.getCentroide();

            somaX += centro.getX();
            somaY += centro.getY();
        }

        return new Ponto(
                somaX / formas.size(),
                somaY / formas.size()
        );
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
    }
}