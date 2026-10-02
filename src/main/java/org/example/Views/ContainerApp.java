package org.example.Views;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;

import org.example.Controller.EditorContext;
import org.example.Controller.FerramentaDesenhar;
import org.example.Controller.FerramentaSelecionar;
import org.example.Models.FormaGeometrica;
import org.example.Models.Ponto;

import java.util.List;

public class ContainerApp extends BorderPane {

    private final Canvas areaDeDesenho;
    private final EditorContext contexto;

    // Posição atual do mouse no Canvas
    private double mouseX;
    private double mouseY;

    // Indica se o mouse está dentro do Canvas
    private boolean mouseDentroCanvas;

    public ContainerApp() {

        // Cria o contexto da aplicação
        contexto = new EditorContext();

        // Define a ferramenta inicial como desenho
        contexto.setFerramentaAtual(
                new FerramentaDesenhar()
        );

        // Cria o Canvas
        areaDeDesenho = new Canvas(1000, 600);

        // Permite que o Canvas receba eventos de teclado
        areaDeDesenho.setFocusTraversable(true);
        areaDeDesenho.requestFocus();

        // Coloca o Canvas no centro
        setCenter(areaDeDesenho);

        // Configura os eventos
        inicializarEventos();

        // Primeira renderização
        redesenharCanvas();
    }

    private void inicializarEventos() {

        // Clique do mouse
        areaDeDesenho.setOnMouseClicked(
                this::aoClicarMouse
        );

        // Pressionamento do mouse
        areaDeDesenho.setOnMousePressed(
                this::aoPressionarMouse
        );

        // Movimento durante o arraste
        areaDeDesenho.setOnMouseDragged(
                this::aoArrastarMouse
        );

        // Soltura do mouse
        areaDeDesenho.setOnMouseReleased(
                this::aoSoltarMouse
        );

        // Movimento normal do mouse
        areaDeDesenho.setOnMouseMoved(
                this::aoMoverMouse
        );

        // Entrada no Canvas
        areaDeDesenho.setOnMouseEntered(
                this::aoEntrarCanvas
        );

        // Saída do Canvas
        areaDeDesenho.setOnMouseExited(
                this::aoSairCanvas
        );

        // Teclas para trocar de ferramenta
        areaDeDesenho.setOnKeyPressed(evento -> {

            switch (evento.getCode()) {

                case D -> {
                    // Ao mudar para a ferramenta de desenho,
                    // remove qualquer seleção existente.
                    contexto.limparSelecao();

                    contexto.setFerramentaAtual(
                            new FerramentaDesenhar()
                    );

                    redesenharCanvas();
                }

                case S -> {
                    contexto.setFerramentaAtual(
                            new FerramentaSelecionar()
                    );

                    redesenharCanvas();
                }
            }
        });
    }

    private void aoClicarMouse(MouseEvent evento) {

        if (contexto.getFerramentaAtual() != null) {

            contexto.getFerramentaAtual()
                    .aoClicar(
                            evento,
                            contexto
                    );

            redesenharCanvas();
        }
    }

    private void aoPressionarMouse(MouseEvent evento) {

        if (contexto.getFerramentaAtual() != null) {

            contexto.getFerramentaAtual()
                    .aoPressionarMouse(
                            evento,
                            contexto
                    );

            redesenharCanvas();
        }
    }

    private void aoArrastarMouse(MouseEvent evento) {

        if (contexto.getFerramentaAtual() != null) {

            contexto.getFerramentaAtual()
                    .aoArrastarMouse(
                            evento,
                            contexto
                    );

            redesenharCanvas();
        }
    }

    private void aoSoltarMouse(MouseEvent evento) {

        if (contexto.getFerramentaAtual() != null) {

            contexto.getFerramentaAtual()
                    .aoSoltarMouse(
                            evento,
                            contexto
                    );

            redesenharCanvas();
        }
    }

    private void aoMoverMouse(MouseEvent evento) {

        mouseX = evento.getX();
        mouseY = evento.getY();

        if (contexto.getFormaEmAndamento() != null) {

            redesenharCanvas();
        }
    }

    private void aoEntrarCanvas(MouseEvent evento) {

        mouseDentroCanvas = true;

        mouseX = evento.getX();
        mouseY = evento.getY();

        redesenharCanvas();
    }

    private void aoSairCanvas(MouseEvent evento) {

        mouseDentroCanvas = false;

        redesenharCanvas();
    }

    private void redesenharCanvas() {

        GraphicsContext gc =
                areaDeDesenho.getGraphicsContext2D();

        // Limpa o Canvas
        gc.clearRect(
                0,
                0,
                areaDeDesenho.getWidth(),
                areaDeDesenho.getHeight()
        );

        // Fundo branco
        desenharFundo();

        // Desenha todas as formas finalizadas
        for (FormaGeometrica forma :
                contexto.getFormas()) {

            desenharForma(
                    forma,
                    false,
                    contexto.estaSelecionada(forma)
            );
        }

        // Desenha a forma que está sendo criada
        if (contexto.getFormaEmAndamento() != null) {

            desenharForma(
                    contexto.getFormaEmAndamento(),
                    true,
                    false
            );
        }
    }

    private void desenharFundo() {

        GraphicsContext gc =
                areaDeDesenho.getGraphicsContext2D();

        gc.setFill(Color.WHITE);

        gc.fillRect(
                0,
                0,
                areaDeDesenho.getWidth(),
                areaDeDesenho.getHeight()
        );
    }

    private void desenharForma(
            FormaGeometrica forma,
            boolean emAndamento,
            boolean selecionada) {

        GraphicsContext gc =
                areaDeDesenho.getGraphicsContext2D();

        List<Ponto> pontos = forma.getPontos();

        if (pontos == null || pontos.isEmpty()) {
            return;
        }

        double[] x = new double[pontos.size()];
        double[] y = new double[pontos.size()];

        for (int i = 0; i < pontos.size(); i++) {

            x[i] = pontos.get(i).getX();
            y[i] = pontos.get(i).getY();
        }

        // Configuração da borda
        try {

            gc.setStroke(
                    Color.web(
                            forma.getCorBordaHex()
                    )
            );

        } catch (Exception e) {

            gc.setStroke(Color.BLACK);
        }

        gc.setLineWidth(
                forma.getEspessuraBorda()
        );

        // Destaca a forma selecionada
        if (selecionada) {

            gc.setStroke(Color.BLUE);

            gc.setLineWidth(
                    forma.getEspessuraBorda() + 3
            );
        }

        // Forma finalizada
        if (!emAndamento && pontos.size() >= 3) {

            // Cor de preenchimento
            try {

                gc.setFill(
                        Color.web(
                                forma.getCorPreenchimentoHex()
                        )
                );

            } catch (Exception e) {

                gc.setFill(Color.WHITE);
            }

            // Preenchimento
            gc.fillPolygon(
                    x,
                    y,
                    pontos.size()
            );

            // Borda
            gc.strokePolygon(
                    x,
                    y,
                    pontos.size()
            );

        } else {

            // Desenha as linhas enquanto está em construção
            if (pontos.size() >= 2) {

                gc.strokePolyline(
                        x,
                        y,
                        pontos.size()
                );
            }

            /*
             * Pré-visualização:
             * desenha uma linha do último ponto
             * até o cursor do mouse.
             */
            if (emAndamento
                    && mouseDentroCanvas
                    && pontos.size() >= 1) {

                Ponto ultimoPonto =
                        pontos.get(
                                pontos.size() - 1
                        );

                gc.setStroke(Color.GRAY);
                gc.setLineWidth(1.0);

                gc.strokeLine(
                        ultimoPonto.getX(),
                        ultimoPonto.getY(),
                        mouseX,
                        mouseY
                );
            }
        }

        // Desenha os vértices
        desenharVertices(
                pontos,
                gc
        );
    }

    private void desenharVertices(
            List<Ponto> pontos,
            GraphicsContext gc) {

        gc.setFill(Color.BLACK);

        double tamanho = 6;

        for (Ponto ponto : pontos) {

            gc.fillOval(
                    ponto.getX() - tamanho / 2,
                    ponto.getY() - tamanho / 2,
                    tamanho,
                    tamanho
            );
        }
    }

    public Canvas getAreaDeDesenho() {
        return areaDeDesenho;
    }

    public EditorContext getContexto() {
        return contexto;
    }
}