 package org.example.Controller;

import javafx.scene.input.MouseEvent;

import org.example.Models.FormaGeometrica;
import org.example.Models.Ponto;

import java.util.HashSet;
import java.util.Set;

public class FerramentaSelecionar implements Ferramenta {

    // Última posição do mouse durante o arraste
    private double ultimoX;
    private double ultimoY;

    // Controle da caixa de seleção
    private boolean arrastandoCaixa;
    private double selecaoStartX;
    private double selecaoStartY;

    @Override
    public void aoPressionarMouse(
            MouseEvent evento,
            EditorContext contexto) {

        double x = evento.getX();
        double y = evento.getY();

        // Procura a forma clicada usando as coordenadas
        // reais do mouse para não prejudicar a seleção.
        FormaGeometrica formaClicada =
                encontrarForma(x, y, contexto);

        if (formaClicada != null) {

            // Seleciona a forma ou seu grupo, caso
            // ainda não esteja selecionada.
            if (!contexto.estaSelecionada(formaClicada)) {

                contexto.selecionarFormaOuGrupo(
                        formaClicada
                );

            } else {

                // Registra qual forma foi clicada por último.
                contexto.setFormaPrincipalSelecionada(
                        formaClicada
                );
            }

            /*
             * Guarda a posição inicial do mouse.
             * Se o Snap estiver ligado, utiliza a
             * posição ajustada à grade.
             */
            ultimoX = contexto.aplicarSnap(x);
            ultimoY = contexto.aplicarSnap(y);

            arrastandoCaixa = false;

        } else {

            // Clique fora das formas: desmarca todas.
            contexto.limparSelecao();

            // Inicia uma possível caixa de seleção.
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

        if (!arrastandoCaixa
                && !contexto.getSelecionados().isEmpty()) {

            /*
             * Converte a posição atual para a grade
             * quando o Snap estiver ativado.
             */
            double posicaoX =
                    contexto.aplicarSnap(x);

            double posicaoY =
                    contexto.aplicarSnap(y);

            // Calcula o deslocamento.
            double dx = posicaoX - ultimoX;
            double dy = posicaoY - ultimoY;

            /*
             * Move todas as formas selecionadas,
             * incluindo os membros dos grupos.
             * Todas recebem exatamente o mesmo deslocamento.
             */
            if (dx != 0 || dy != 0) {

                for (FormaGeometrica forma :
                        contexto.getFormasAfetadasPorSelecao()) {

                    forma.transladar(dx, dy);
                }
            }

            // Atualiza a última posição já ajustada à grade.
            ultimoX = posicaoX;
            ultimoY = posicaoY;
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

        if (arrastandoCaixa) {

            double minX = Math.min(selecaoStartX, x);
            double maxX = Math.max(selecaoStartX, x);

            double minY = Math.min(selecaoStartY, y);
            double maxY = Math.max(selecaoStartY, y);

            Set<String> idsDosGrupos = new HashSet<>();

            // Seleciona formas totalmente dentro da caixa.
            for (FormaGeometrica forma :
                    contexto.getFormas()) {

                if (formaDentroDaCaixa(
                        forma,
                        minX,
                        minY,
                        maxX,
                        maxY)) {

                    contexto.adicionarSelecionado(forma);

                    String groupId = forma.getGroupId();

                    if (groupId != null && !groupId.isBlank()) {
                        idsDosGrupos.add(groupId);
                    }
                }
            }

            // Inclui os demais membros dos grupos encontrados.
            for (FormaGeometrica forma :
                    contexto.getFormas()) {

                String groupId = forma.getGroupId();

                if (groupId != null
                        && idsDosGrupos.contains(groupId)) {

                    contexto.adicionarSelecionado(forma);
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

        // A seleção é tratada no pressionamento do mouse.
    }

    private FormaGeometrica encontrarForma(
            double x,
            double y,
            EditorContext contexto) {

        // Prioriza a forma desenhada por cima.
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

    private boolean formaDentroDaCaixa(
            FormaGeometrica forma,
            double minX,
            double minY,
            double maxX,
            double maxY) {

        if (forma.getPontos() == null
                || forma.getPontos().isEmpty()) {

            return false;
        }

        for (Ponto ponto : forma.getPontos()) {

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