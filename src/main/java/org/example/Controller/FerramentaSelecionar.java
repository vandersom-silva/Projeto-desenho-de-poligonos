package org.example.Controller;

import javafx.scene.input.MouseEvent;
import org.example.Models.FormaGeometrica;

public class FerramentaSelecionar implements Ferramenta {

    // Forma que está sendo arrastada
    private FormaGeometrica formaSendoArrastada;

    // Última posição conhecida do mouse
    private double ultimoX;
    private double ultimoY;

    @Override
    public void aoPressionarMouse(
            MouseEvent evento,
            EditorContext contexto) {

        double x = evento.getX();
        double y = evento.getY();

        // Procura a forma clicada
        formaSendoArrastada = encontrarForma(
                x,
                y,
                contexto
        );

        // Se encontrou uma forma
        if (formaSendoArrastada != null) {

            // Limpa a seleção anterior
            contexto.limparSelecao();

            // Seleciona a forma encontrada
            contexto.adicionarSelecionado(
                    formaSendoArrastada
            );

            // Guarda a posição inicial do mouse
            ultimoX = x;
            ultimoY = y;
        }

        contexto.requestRedraw();
        contexto.requestStatsUpdate();
    }

    @Override
    public void aoArrastarMouse(
            MouseEvent evento,
            EditorContext contexto) {

        // Se não existe forma sendo arrastada,
        // não fazemos nada.
        if (formaSendoArrastada == null) {
            return;
        }

        double x = evento.getX();
        double y = evento.getY();

        // Calcula quanto o mouse se moveu
        double dx = x - ultimoX;
        double dy = y - ultimoY;

        // Move a forma
        formaSendoArrastada.transladar(dx, dy);

        // Atualiza a última posição
        ultimoX = x;
        ultimoY = y;

        contexto.requestRedraw();
        contexto.requestStatsUpdate();
    }

    @Override
    public void aoSoltarMouse(
            MouseEvent evento,
            EditorContext contexto) {

        // Finaliza o arraste
        formaSendoArrastada = null;

        contexto.requestRedraw();
        contexto.requestStatsUpdate();
    }

    @Override
    public void aoClicar(
            MouseEvent evento,
            EditorContext contexto) {

        double x = evento.getX();
        double y = evento.getY();

        // Primeiro remove qualquer seleção existente
        contexto.limparSelecao();

        // Procura uma forma no ponto clicado
        for (int i = contexto.getFormas().size() - 1;
             i >= 0;
             i--) {

            FormaGeometrica forma =
                    contexto.getFormas().get(i);

            if (forma.contemPonto(x, y)) {

                contexto.adicionarSelecionado(forma);

                break;
            }
        }

        contexto.requestRedraw();
        contexto.requestStatsUpdate();
    }
    private FormaGeometrica encontrarForma(
            double x,
            double y,
            EditorContext contexto) {

        /*
         * Percorremos de trás para frente para encontrar
         * primeiro a forma que estiver por cima.
         */
        for (int i = contexto.getFormas().size() - 1;
             i >= 0;
             i--) {

            FormaGeometrica forma =
                    contexto.getFormas().get(i);

            if (forma.contemPonto(x, y)) {
                return forma;
            }
        }

        return null;
    }
}