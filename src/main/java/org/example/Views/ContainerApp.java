package org.example.Views;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;

import org.example.Controller.EditorContext;
import org.example.Controller.FerramentaDesenhar;
import org.example.Controller.FerramentaInserirForma;
import org.example.Controller.FerramentaSelecionar;
import org.example.Controller.FerramentaTransformar;

import org.example.Models.Circulo;
import org.example.Models.FormaGeometrica;
import org.example.Models.Hexagono;
import org.example.Models.Ponto;
import org.example.Models.Quadrado;

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

        // Ferramenta inicial: desenho de polígonos
        contexto.setFerramentaAtual(
                new FerramentaDesenhar()
        );

        // Cria o Canvas
        areaDeDesenho = new Canvas(1000, 600);

        // Permite receber eventos do teclado
        areaDeDesenho.setFocusTraversable(true);
        areaDeDesenho.requestFocus();

        // Coloca o Canvas no centro da tela
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

        // Arraste do mouse
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

        // Entrada do mouse no Canvas
        areaDeDesenho.setOnMouseEntered(
                this::aoEntrarCanvas
        );

        // Saída do mouse do Canvas
        areaDeDesenho.setOnMouseExited(
                this::aoSairCanvas
        );

        // Troca de ferramenta pelo teclado
        areaDeDesenho.setOnKeyPressed(evento -> {

            switch (evento.getCode()) {

                case D -> {

                    // Vai para desenho
                    // e desmarca todas as formas
                    contexto.limparSelecao();

                    // Cancela forma que estivesse em andamento
                    contexto.setFormaEmAndamento(null);

                    contexto.setFerramentaAtual(
                            new FerramentaDesenhar()
                    );

                    redesenharCanvas();
                }

                case S -> {

                    // Vai para seleção
                    contexto.setFormaEmAndamento(null);

                    contexto.setFerramentaAtual(
                            new FerramentaSelecionar()
                    );

                    redesenharCanvas();
                }

                case C -> {

                    // Vai para criação de círculo
                    contexto.limparSelecao();
                    contexto.setFormaEmAndamento(null);

                    contexto.setFerramentaAtual(
                            new FerramentaInserirForma(
                                    FerramentaInserirForma.TipoForma.CIRCULO
                            )
                    );

                    redesenharCanvas();
                }

                case Q -> {

                    // Vai para criação de quadrado
                    contexto.limparSelecao();
                    contexto.setFormaEmAndamento(null);

                    contexto.setFerramentaAtual(
                            new FerramentaInserirForma(
                                    FerramentaInserirForma.TipoForma.QUADRADO
                            )
                    );

                    redesenharCanvas();
                }

                case H -> {

                    // Vai para criação de hexágono
                    contexto.limparSelecao();
                    contexto.setFormaEmAndamento(null);

                    contexto.setFerramentaAtual(
                            new FerramentaInserirForma(
                                    FerramentaInserirForma.TipoForma.HEXAGONO
                            )
                    );

                    redesenharCanvas();
                }

                case E -> {

                    // Escala
                    contexto.setFormaEmAndamento(null);

                    contexto.setFerramentaAtual(
                            new FerramentaTransformar(
                                    FerramentaTransformar.Modo.ESCALA
                            )
                    );

                    redesenharCanvas();
                }

                case R -> {

                    // Rotação
                    contexto.setFormaEmAndamento(null);

                    contexto.setFerramentaAtual(
                            new FerramentaTransformar(
                                    FerramentaTransformar.Modo.ROTACAO
                            )
                    );

                    redesenharCanvas();
                }

                case T -> {

                    // Cisalhamento
                    contexto.setFormaEmAndamento(null);

                    contexto.setFerramentaAtual(
                            new FerramentaTransformar(
                                    FerramentaTransformar.Modo.CISALHAMENTO
                            )
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

        mouseX = evento.getX();
        mouseY = evento.getY();

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

        mouseX = evento.getX();
        mouseY = evento.getY();

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

        mouseX = evento.getX();
        mouseY = evento.getY();

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

        // Atualiza a pré-visualização do polígono
        if (contexto.getFormaEmAndamento() != null) {

            redesenharCanvas();

        } else if (contexto.getFerramentaAtual()
                instanceof FerramentaSelecionar) {

            // Atualiza a caixa de seleção
            FerramentaSelecionar ferramenta =
                    (FerramentaSelecionar)
                            contexto.getFerramentaAtual();

            if (ferramenta.isArrastandoCaixa()) {

                redesenharCanvas();
            }
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

        // Desenha o fundo
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

        // Desenha a forma em andamento
        if (contexto.getFormaEmAndamento() != null) {

            desenharForma(
                    contexto.getFormaEmAndamento(),
                    true,
                    false
            );
        }

        // Desenha a caixa de seleção
        desenharCaixaSelecao();
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

        /*
         * CÍRCULO
         */
        if (forma instanceof Circulo) {

            desenharCirculo(
                    (Circulo) forma,
                    selecionada
            );

            return;
        }

        List<Ponto> pontos = forma.getPontos();

        if (pontos == null || pontos.isEmpty()) {
            return;
        }

        double[] x = new double[pontos.size()];
        double[] y = new double[pontos.size()];

        // Converte os pontos para os arrays do Canvas
        for (int i = 0; i < pontos.size(); i++) {

            x[i] = pontos.get(i).getX();
            y[i] = pontos.get(i).getY();
        }

        // Configuração da cor da borda
        try {

            gc.setStroke(
                    Color.web(
                            forma.getCorBordaHex()
                    )
            );

        } catch (Exception e) {

            gc.setStroke(Color.BLACK);
        }

        // Espessura da borda
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

        /*
         * POLÍGONO, QUADRADO E HEXÁGONO
         *
         * Quadrado e hexágono ficam fechados
         * mesmo durante o arraste.
         */
        if ((!emAndamento && pontos.size() >= 3)
                || forma instanceof Quadrado
                || forma instanceof Hexagono) {

            // Cor do preenchimento
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

            // Borda fechada
            gc.strokePolygon(
                    x,
                    y,
                    pontos.size()
            );

        } else {

            /*
             * Enquanto o polígono está sendo criado,
             * desenha as linhas já existentes.
             */
            if (pontos.size() >= 2) {

                gc.strokePolyline(
                        x,
                        y,
                        pontos.size()
                );
            }

            /*
             * Pré-visualização do próximo segmento
             *
             * Somente para o polígono comum.
             */
            if (emAndamento
                    && !(forma instanceof Quadrado)
                    && !(forma instanceof Hexagono)
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

    private void desenharCirculo(
            Circulo circulo,
            boolean selecionada) {

        GraphicsContext gc =
                areaDeDesenho.getGraphicsContext2D();

        double centroX =
                circulo.getCentro().getX();

        double centroY =
                circulo.getCentro().getY();

        double raio =
                circulo.getRaio();

        // Cor da borda
        try {

            gc.setStroke(
                    Color.web(
                            circulo.getCorBordaHex()
                    )
            );

        } catch (Exception e) {

            gc.setStroke(Color.BLACK);
        }

        // Espessura da borda
        gc.setLineWidth(
                circulo.getEspessuraBorda()
        );

        // Destaque da seleção
        if (selecionada) {

            gc.setStroke(Color.BLUE);

            gc.setLineWidth(
                    circulo.getEspessuraBorda() + 3
            );
        }

        // Cor de preenchimento
        try {

            gc.setFill(
                    Color.web(
                            circulo.getCorPreenchimentoHex()
                    )
            );

        } catch (Exception e) {

            gc.setFill(Color.WHITE);
        }

        // Preenchimento do círculo
        gc.fillOval(
                centroX - raio,
                centroY - raio,
                raio * 2,
                raio * 2
        );

        // Borda do círculo
        gc.strokeOval(
                centroX - raio,
                centroY - raio,
                raio * 2,
                raio * 2
        );

        // Marca o centro
        gc.setFill(Color.BLACK);

        gc.fillOval(
                centroX - 3,
                centroY - 3,
                6,
                6
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

    private void desenharCaixaSelecao() {

        // Só desenha a caixa na ferramenta de seleção
        if (!(contexto.getFerramentaAtual()
                instanceof FerramentaSelecionar)) {

            return;
        }

        FerramentaSelecionar ferramenta =
                (FerramentaSelecionar)
                        contexto.getFerramentaAtual();

        // Não há caixa para desenhar
        if (!ferramenta.isArrastandoCaixa()) {

            return;
        }

        double startX =
                ferramenta.getSelecaoStartX();

        double startY =
                ferramenta.getSelecaoStartY();

        double largura =
                mouseX - startX;

        double altura =
                mouseY - startY;

        GraphicsContext gc =
                areaDeDesenho.getGraphicsContext2D();

        gc.setStroke(Color.GRAY);

        gc.setLineWidth(1.0);

        // Caixa tracejada
        gc.setLineDashes(5);

        gc.strokeRect(
                Math.min(startX, mouseX),
                Math.min(startY, mouseY),
                Math.abs(largura),
                Math.abs(altura)
        );

        // Volta para linha contínua
        gc.setLineDashes(null);
    }

    public Canvas getAreaDeDesenho() {
        return areaDeDesenho;
    }

    public EditorContext getContexto() {
        return contexto;
    }
}