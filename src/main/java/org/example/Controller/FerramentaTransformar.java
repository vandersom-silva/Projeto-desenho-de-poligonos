package org.example.Controller;

import javafx.scene.input.MouseEvent;
import org.example.Models.FormaGeometrica;

public class FerramentaTransformar implements Ferramenta {

    public enum Modo {

        ESCALA,
        ROTACAO,
        CISALHAMENTO
    }

    private Modo modo;

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

        /*
         * Se não houver formas selecionadas,
         * não há nada para transformar.
         */
        if (contexto.getSelecionados().isEmpty()) {

            ultimoX = x;
            ultimoY = y;

            return;
        }

        switch (modo) {

            case ESCALA -> {

                /*
                 * Movimento horizontal controla a escala.
                 *
                 * dx > 0  → aumenta
                 * dx < 0  → diminui
                 */
                double fator = 1.0 + (dx / 200.0);

                /*
                 * Evita fator zero ou negativo.
                 */
                fator = Math.max(
                        0.01,
                        fator
                );

                for (FormaGeometrica forma :
                        contexto.getSelecionados()) {

                    forma.escalar(fator);
                }
            }

            case ROTACAO -> {

                /*
                 * Movimento horizontal controla
                 * o ângulo da rotação.
                 */
                double angulo =
                        dx * 0.01;

                for (FormaGeometrica forma :
                        contexto.getSelecionados()) {

                    forma.rotacionar(angulo);
                }
            }

            case CISALHAMENTO -> {

                /*
                 * Movimento horizontal:
                 * cisalhamento horizontal.
                 *
                 * Movimento vertical:
                 * cisalhamento vertical.
                 */
                double shx =
                        dx * 0.005;

                double shy =
                        dy * 0.005;

                for (FormaGeometrica forma :
                        contexto.getSelecionados()) {

                    forma.cisalhar(
                            shx,
                            shy
                    );
                }
            }
        }

        ultimoX = x;
        ultimoY = y;

        contexto.requestRedraw();
        contexto.requestStatsUpdate();
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