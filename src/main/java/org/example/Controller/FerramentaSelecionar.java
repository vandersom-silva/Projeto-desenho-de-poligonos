package org.example.Controller;

import javafx.scene.input.MouseEvent;
import org.example.Models.FormaGeometrica;
import org.example.Models.Ponto;

public class FerramentaSelecionar implements Ferramenta {

    // Última posição do mouse durante o arraste
    private double ultimoX;
    private double ultimoY;

    // Dados da caixa de seleção
    private boolean arrastandoCaixa;
    private double selecaoStartX;
    private double selecaoStartY;

    @Override
    public void aoPressionarMouse(
            MouseEvent evento,
            EditorContext contexto) {

        double x = evento.getX();
        double y = evento.getY();

        // Procura a forma que foi clicada
        FormaGeometrica formaClicada =
                encontrarForma(x, y, contexto);

        if (formaClicada != null) {

            /*
             * Se clicou em uma forma que já está selecionada,
             * mantemos todas as formas selecionadas.
             *
             * Isso permite mover várias formas juntas.
             */
            if (!contexto.estaSelecionada(formaClicada)) {

                // Clicou em uma forma não selecionada:
                // limpa a seleção anterior e seleciona somente ela.
                contexto.limparSelecao();

                contexto.adicionarSelecionado(
                        formaClicada
                );
            }

            // Guarda a posição inicial do mouse
            ultimoX = x;
            ultimoY = y;

            // Não é uma caixa de seleção
            arrastandoCaixa = false;

        } else {

            /*
             * Clicou em uma área vazia.
             * Remove a seleção atual e começa
             * uma possível caixa de seleção.
             */
            contexto.limparSelecao();

            arrastandoCaixa = true;

            selecaoStartX = x;
            selecaoStartY = y;
        }

        contexto.requestRedraw();
        contexto.requestStatsUpdate();
    }

    @Override
    public void aoArrastarMouse(
            MouseEvent evento,
            EditorContext contexto) {

        double x = evento.getX();
        double y = evento.getY();

        /*
         * Se existe uma ou mais formas selecionadas,
         * todas são movimentadas juntas.
         */
        if (!arrastandoCaixa
                && !contexto.getSelecionados().isEmpty()) {

            // Calcula o deslocamento do mouse
            double dx = x - ultimoX;
            double dy = y - ultimoY;

            /*
             * Aplica exatamente o mesmo deslocamento
             * a todas as formas selecionadas.
             */
            for (FormaGeometrica forma :
                    contexto.getSelecionados()) {

                forma.transladar(dx, dy);
            }

            // Atualiza a posição anterior do mouse
            ultimoX = x;
            ultimoY = y;
        }

        /*
         * Se estiver criando uma caixa de seleção,
         * não move as formas.
         */
        else if (arrastandoCaixa) {

            // A caixa será desenhada pelo ContainerApp.
        }

        contexto.requestRedraw();
        contexto.requestStatsUpdate();
    }

    @Override
    public void aoSoltarMouse(
            MouseEvent evento,
            EditorContext contexto) {

        double x = evento.getX();
        double y = evento.getY();

        // Finaliza a caixa de seleção
        if (arrastandoCaixa) {

            double minX = Math.min(
                    selecaoStartX,
                    x
            );

            double maxX = Math.max(
                    selecaoStartX,
                    x
            );

            double minY = Math.min(
                    selecaoStartY,
                    y
            );

            double maxY = Math.max(
                    selecaoStartY,
                    y
            );

            /*
             * Seleciona todas as formas que estiverem
             * completamente dentro da caixa.
             */
            for (FormaGeometrica forma :
                    contexto.getFormas()) {

                if (formaDentroDaCaixa(
                        forma,
                        minX,
                        minY,
                        maxX,
                        maxY)) {

                    contexto.adicionarSelecionado(
                            forma
                    );
                }
            }
        }

        arrastandoCaixa = false;

        contexto.requestRedraw();
        contexto.requestStatsUpdate();
    }

    @Override
    public void aoClicar(
            MouseEvent evento,
            EditorContext contexto) {

        /*
         * A seleção é feita no pressionamento
         * do botão do mouse.
         */
    }

    private FormaGeometrica encontrarForma(
            double x,
            double y,
            EditorContext contexto) {

        /*
         * Percorre de trás para frente para encontrar
         * primeiro a forma que estiver por cima.
         */
        for (int i =
             contexto.getFormas().size() - 1;
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

    private boolean formaDentroDaCaixa(
            FormaGeometrica forma,
            double minX,
            double minY,
            double maxX,
            double maxY) {

        for (Ponto ponto :
                forma.getPontos()) {

            if (ponto.getX() < minX
                    || ponto.getX() > maxX
                    || ponto.getY() < minY
                    || ponto.getY() > maxY) {

                return false;
            }
        }

        return true;
    }

    public boolean isArrastandoCaixa() {
        return arrastandoCaixa;
    }

    public double getSelecaoStartX() {
        return selecaoStartX;
    }

    public double getSelecaoStartY() {
        return selecaoStartY;
    }
}