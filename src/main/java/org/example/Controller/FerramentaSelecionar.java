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

    // Evita registrar vários estados durante o mesmo arraste
    private boolean estadoRegistradoDuranteArraste;

    @Override
    public void aoPressionarMouse(
            MouseEvent evento,
            EditorContext contexto) {

        double x = evento.getX();
        double y = evento.getY();

        // Reinicia o controle do histórico para este gesto
        estadoRegistradoDuranteArraste = false;

        // Procura a forma clicada
        FormaGeometrica formaClicada =
                encontrarForma(x, y, contexto);

        if (formaClicada != null) {

            /*
             * Se a forma ainda não estiver selecionada,
             * seleciona a forma ou todo o grupo.
             */
            if (!contexto.estaSelecionada(formaClicada)) {

                contexto.selecionarFormaOuGrupo(
                        formaClicada
                );

            } else {

                // Mantém a seleção múltipla e registra
                // qual forma foi clicada por último.
                contexto.setFormaPrincipalSelecionada(
                        formaClicada
                );
            }

            // Posição inicial usada no movimento.
            ultimoX = contexto.aplicarSnap(x);
            ultimoY = contexto.aplicarSnap(y);

            arrastandoCaixa = false;

        } else {

            // Clique fora das formas: desmarca tudo.
            contexto.limparSelecao();

            // Começa uma possível caixa de seleção.
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

        // Arraste para mover formas selecionadas
        if (!arrastandoCaixa
                && !contexto.getSelecionados().isEmpty()) {

            // Aplica Snap to Grid, se estiver habilitado.
            double posicaoX = contexto.aplicarSnap(x);
            double posicaoY = contexto.aplicarSnap(y);

            double dx = posicaoX - ultimoX;
            double dy = posicaoY - ultimoY;

            if (dx != 0 || dy != 0) {

                /*
                 * Salva o estado anterior somente uma vez
                 * antes da primeira movimentação.
                 */
                if (!estadoRegistradoDuranteArraste) {

                    contexto.registrarEstado();

                    estadoRegistradoDuranteArraste = true;
                }

                /*
                 * Move todas as formas selecionadas,
                 * incluindo os membros dos grupos.
                 *
                 * Todas recebem o mesmo deslocamento.
                 */
                for (FormaGeometrica forma :
                        contexto.getFormasAfetadasPorSelecao()) {

                    forma.transladar(dx, dy);
                }
            }

            // Atualiza a posição anterior do mouse.
            ultimoX = posicaoX;
            ultimoY = posicaoY;
        }

        // A caixa de seleção é desenhada pelo ContainerApp.

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

            // Calcula os limites da caixa,
            // independentemente da direção do arraste.
            double minX = Math.min(selecaoStartX, x);
            double maxX = Math.max(selecaoStartX, x);

            double minY = Math.min(selecaoStartY, y);
            double maxY = Math.max(selecaoStartY, y);

            Set<String> idsDosGrupos = new HashSet<>();

            /*
             * Seleciona as formas cujos vértices
             * estiverem completamente dentro da caixa.
             */
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

                    if (groupId != null
                            && !groupId.isBlank()) {

                        idsDosGrupos.add(groupId);
                    }
                }
            }

            /*
             * Se um membro de um grupo for selecionado
             * pela caixa, inclui os demais membros.
             */
            for (FormaGeometrica forma :
                    contexto.getFormas()) {

                String groupId = forma.getGroupId();

                if (groupId != null
                        && idsDosGrupos.contains(groupId)) {

                    contexto.adicionarSelecionado(forma);
                }
            }
        }

        // Finaliza os estados do gesto atual.
        arrastandoCaixa = false;
        estadoRegistradoDuranteArraste = false;

        contexto.requestRedraw();
        contexto.requestStatsUpdate();
    }

    @Override
    public void aoClicar(
            MouseEvent evento,
            EditorContext contexto) {

        // A seleção é tratada no pressionamento do mouse.
    }

    /**
     * Encontra a forma clicada.
     * Percorre a lista de trás para frente para
     * priorizar a forma que está por cima.
     */
    private FormaGeometrica encontrarForma(
            double x,
            double y,
            EditorContext contexto) {

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

    /**
     * Verifica se todos os vértices da forma estão
     * dentro dos limites da caixa de seleção.
     */
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

    // Métodos usados pelo ContainerApp
    // para desenhar a caixa de seleção.

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