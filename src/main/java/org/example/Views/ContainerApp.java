package org.example.Views;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;

import org.example.Controller.EditorContext;
import org.example.Controller.FerramentaDesenhar;
import org.example.Controller.FerramentaEditarVertice;
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

    private double mouseX;
    private double mouseY;

    private boolean mouseDentroCanvas;

    public ContainerApp() {

        contexto = new EditorContext();

        contexto.setFerramentaAtual(
                new FerramentaDesenhar()
        );

        areaDeDesenho = new Canvas(1000, 600);

        areaDeDesenho.setFocusTraversable(true);
        areaDeDesenho.requestFocus();

        setCenter(areaDeDesenho);

        inicializarEventos();

        redesenharCanvas();
    }

    private void inicializarEventos() {

        areaDeDesenho.setOnMouseClicked(
                this::aoClicarMouse
        );

        areaDeDesenho.setOnMousePressed(
                this::aoPressionarMouse
        );

        areaDeDesenho.setOnMouseDragged(
                this::aoArrastarMouse
        );

        areaDeDesenho.setOnMouseReleased(
                this::aoSoltarMouse
        );

        areaDeDesenho.setOnMouseMoved(
                this::aoMoverMouse
        );

        areaDeDesenho.setOnMouseEntered(
                this::aoEntrarCanvas
        );

        areaDeDesenho.setOnMouseExited(
                this::aoSairCanvas
        );

        areaDeDesenho.setOnKeyPressed(evento -> {

            switch (evento.getCode()) {

                case D -> {

                    contexto.limparSelecao();
                    contexto.setFormaEmAndamento(null);

                    contexto.setFerramentaAtual(
                            new FerramentaDesenhar()
                    );

                    redesenharCanvas();
                }

                case S -> {

                    contexto.setFormaEmAndamento(null);

                    contexto.setFerramentaAtual(
                            new FerramentaSelecionar()
                    );

                    redesenharCanvas();
                }

                case C -> {

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

                    contexto.setFormaEmAndamento(null);

                    contexto.setFerramentaAtual(
                            new FerramentaTransformar(
                                    FerramentaTransformar.Modo.ESCALA
                            )
                    );

                    redesenharCanvas();
                }

                case R -> {

                    contexto.setFormaEmAndamento(null);

                    contexto.setFerramentaAtual(
                            new FerramentaTransformar(
                                    FerramentaTransformar.Modo.ROTACAO
                            )
                    );

                    redesenharCanvas();
                }

                case T -> {

                    contexto.setFormaEmAndamento(null);

                    contexto.setFerramentaAtual(
                            new FerramentaTransformar(
                                    FerramentaTransformar.Modo.CISALHAMENTO
                            )
                    );

                    redesenharCanvas();
                }

                case V -> {

                    contexto.setFormaEmAndamento(null);

                    contexto.setFerramentaAtual(
                            new FerramentaEditarVertice()
                    );

                    redesenharCanvas();
                }

                case G -> {

                    contexto.agruparSelecionados();

                    redesenharCanvas();
                }

                case U -> {

                    contexto.desagruparSelecionados();

                    redesenharCanvas();
                }

                case F -> {

                    contexto.trazerParaFrente();

                    redesenharCanvas();
                }

                case B -> {

                    contexto.enviarParaTras();

                    redesenharCanvas();
                }

                case N -> {

                    boolean habilitar =
                            !contexto.isGridSnappingHabilitado();

                    contexto.setGridSnappingHabilitado(
                            habilitar
                    );

                    redesenharCanvas();
                }
            }
        });
    }

    private void aoClicarMouse(MouseEvent evento) {

        if (contexto.getFerramentaAtual() != null) {

            contexto.getFerramentaAtual()
                    .aoClicar(evento, contexto);

            redesenharCanvas();
        }
    }

    private void aoPressionarMouse(MouseEvent evento) {

        mouseX = evento.getX();
        mouseY = evento.getY();

        if (contexto.getFerramentaAtual() != null) {

            contexto.getFerramentaAtual()
                    .aoPressionarMouse(evento, contexto);

            redesenharCanvas();
        }
    }

    private void aoArrastarMouse(MouseEvent evento) {

        mouseX = evento.getX();
        mouseY = evento.getY();

        if (contexto.getFerramentaAtual() != null) {

            contexto.getFerramentaAtual()
                    .aoArrastarMouse(evento, contexto);

            redesenharCanvas();
        }
    }

    private void aoSoltarMouse(MouseEvent evento) {

        mouseX = evento.getX();
        mouseY = evento.getY();

        if (contexto.getFerramentaAtual() != null) {

            contexto.getFerramentaAtual()
                    .aoSoltarMouse(evento, contexto);

            redesenharCanvas();
        }
    }

    private void aoMoverMouse(MouseEvent evento) {

        mouseX = evento.getX();
        mouseY = evento.getY();

        if (contexto.getFormaEmAndamento() != null) {

            redesenharCanvas();

        } else if (contexto.getFerramentaAtual()
                instanceof FerramentaSelecionar) {

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

        gc.clearRect(
                0,
                0,
                areaDeDesenho.getWidth(),
                areaDeDesenho.getHeight()
        );

        desenharFundo();

        // A grade fica atrás das formas.
        desenharGrade();

        for (FormaGeometrica forma : contexto.getFormas()) {

            desenharForma(
                    forma,
                    false,
                    contexto.estaSelecionada(forma)
            );
        }

        if (contexto.getFormaEmAndamento() != null) {

            desenharForma(
                    contexto.getFormaEmAndamento(),
                    true,
                    false
            );
        }

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

    private void desenharGrade() {

        if (!contexto.isGridSnappingHabilitado()) {
            return;
        }

        GraphicsContext gc =
                areaDeDesenho.getGraphicsContext2D();

        double tamanho = contexto.getTamanhoGrade();

        double largura = areaDeDesenho.getWidth();
        double altura = areaDeDesenho.getHeight();

        gc.setStroke(Color.rgb(230, 230, 230));
        gc.setLineWidth(0.5);

        for (double x = 0; x <= largura; x += tamanho) {

            gc.strokeLine(
                    x,
                    0,
                    x,
                    altura
            );
        }

        for (double y = 0; y <= altura; y += tamanho) {

            gc.strokeLine(
                    0,
                    y,
                    largura,
                    y
            );
        }
    }

    private void desenharForma(
            FormaGeometrica forma,
            boolean emAndamento,
            boolean selecionada) {

        GraphicsContext gc =
                areaDeDesenho.getGraphicsContext2D();

        if (forma instanceof Circulo circulo) {

            desenharCirculo(
                    circulo,
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

        for (int i = 0; i < pontos.size(); i++) {

            x[i] = pontos.get(i).getX();
            y[i] = pontos.get(i).getY();
        }

        try {

            gc.setStroke(
                    Color.web(forma.getCorBordaHex())
            );

        } catch (Exception e) {

            gc.setStroke(Color.BLACK);
        }

        gc.setLineWidth(forma.getEspessuraBorda());

        if (selecionada) {

            gc.setStroke(Color.BLUE);

            gc.setLineWidth(
                    forma.getEspessuraBorda() + 3
            );
        }

        boolean formaFechada =
                (!emAndamento && pontos.size() >= 3)
                        || forma instanceof Quadrado
                        || forma instanceof Hexagono;

        if (formaFechada) {

            try {

                gc.setFill(
                        Color.web(
                                forma.getCorPreenchimentoHex()
                        )
                );

            } catch (Exception e) {

                gc.setFill(Color.WHITE);
            }

            gc.fillPolygon(
                    x,
                    y,
                    pontos.size()
            );

            gc.strokePolygon(
                    x,
                    y,
                    pontos.size()
            );

        } else {

            if (pontos.size() >= 2) {

                gc.strokePolyline(
                        x,
                        y,
                        pontos.size()
                );
            }

            // Pré-visualização do próximo segmento do polígono.
            if (emAndamento
                    && mouseDentroCanvas
                    && pontos.size() >= 1) {

                Ponto ultimoPonto =
                        pontos.get(pontos.size() - 1);

                gc.setStroke(Color.GRAY);
                gc.setLineWidth(1.0);

                gc.strokeLine(
                        ultimoPonto.getX(),
                        ultimoPonto.getY(),
                        contexto.aplicarSnap(mouseX),
                        contexto.aplicarSnap(mouseY)
                );
            }
        }

        desenharVertices(pontos, gc);
    }

    private void desenharCirculo(
            Circulo circulo,
            boolean selecionada) {

        GraphicsContext gc =
                areaDeDesenho.getGraphicsContext2D();

        Ponto centro = circulo.getCentro();

        double centroX = centro.getX();
        double centroY = centro.getY();

        double raio = circulo.getRaio();

        try {

            gc.setStroke(
                    Color.web(circulo.getCorBordaHex())
            );

        } catch (Exception e) {

            gc.setStroke(Color.BLACK);
        }

        gc.setLineWidth(
                circulo.getEspessuraBorda()
        );

        if (selecionada) {

            gc.setStroke(Color.BLUE);

            gc.setLineWidth(
                    circulo.getEspessuraBorda() + 3
            );
        }

        try {

            gc.setFill(
                    Color.web(
                            circulo.getCorPreenchimentoHex()
                    )
            );

        } catch (Exception e) {

            gc.setFill(Color.WHITE);
        }

        if (circulo.isCisalhado()) {

            List<Ponto> pontos = circulo.getPontos();

            double[] x = new double[pontos.size()];
            double[] y = new double[pontos.size()];

            for (int i = 0; i < pontos.size(); i++) {

                x[i] = pontos.get(i).getX();
                y[i] = pontos.get(i).getY();
            }

            gc.fillPolygon(
                    x,
                    y,
                    pontos.size()
            );

            gc.strokePolygon(
                    x,
                    y,
                    pontos.size()
            );

        } else {

            gc.fillOval(
                    centroX - raio,
                    centroY - raio,
                    raio * 2,
                    raio * 2
            );

            gc.strokeOval(
                    centroX - raio,
                    centroY - raio,
                    raio * 2,
                    raio * 2
            );
        }

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

        if (!(contexto.getFerramentaAtual()
                instanceof FerramentaSelecionar ferramenta)) {

            return;
        }

        if (!ferramenta.isArrastandoCaixa()) {
            return;
        }

        double startX = ferramenta.getSelecaoStartX();
        double startY = ferramenta.getSelecaoStartY();

        GraphicsContext gc =
                areaDeDesenho.getGraphicsContext2D();

        gc.setStroke(Color.GRAY);
        gc.setLineWidth(1.0);
        gc.setLineDashes(5);

        gc.strokeRect(
                Math.min(startX, mouseX),
                Math.min(startY, mouseY),
                Math.abs(mouseX - startX),
                Math.abs(mouseY - startY)
        );

        // Restaura a linha contínua para os próximos desenhos.
        gc.setLineDashes();
    }

    public Canvas getAreaDeDesenho() {
        return areaDeDesenho;
    }

    public EditorContext getContexto() {
        return contexto;
    }
}