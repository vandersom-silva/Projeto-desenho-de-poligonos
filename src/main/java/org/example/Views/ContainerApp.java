package org.example.Views;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
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
    private final BarraLateralView barraLateral;
    private final PainelStatusView painelStatus;

    // Coordenadas reais do cursor dentro do Canvas.
    private double mouseX;
    private double mouseY;

    private boolean mouseDentroCanvas;

    public ContainerApp() {

        contexto = new EditorContext();

        // Ferramenta inicial: desenhar polígonos.
        contexto.setFerramentaAtual(
                new FerramentaDesenhar()
        );

        areaDeDesenho = new Canvas(1000, 600);
        areaDeDesenho.setFocusTraversable(true);

        // Barra lateral existente no projeto.
        barraLateral = new BarraLateralView(
                contexto,
                this::redesenharCanvas
        );

        // Painel inferior com coordenadas e estatísticas.
        painelStatus = new PainelStatusView(contexto);

        setLeft(barraLateral);
        setCenter(areaDeDesenho);
        setBottom(painelStatus);

        inicializarEventos();

        redesenharCanvas();
        painelStatus.atualizar();
    }

    // ==========================================
    // INICIALIZAÇÃO DOS EVENTOS
    // ==========================================

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

        // Permite usar atalhos do teclado.
        addEventFilter(
                KeyEvent.KEY_PRESSED,
                this::aoPressionarTecla
        );
    }

    // ==========================================
    // ATALHOS DO TECLADO
    // ==========================================

    private void aoPressionarTecla(KeyEvent evento) {

        // Ctrl + Z: desfazer.
        if (evento.isControlDown()
                && evento.getCode() == KeyCode.Z) {

            contexto.desfazer();

            atualizarInterface();

            evento.consume();
            return;
        }

        // Ctrl + Y: refazer.
        if (evento.isControlDown()
                && evento.getCode() == KeyCode.Y) {

            contexto.refazer();

            atualizarInterface();

            evento.consume();
            return;
        }

        // Ctrl + D: duplicar formas selecionadas.
        if (evento.isControlDown()
                && evento.getCode() == KeyCode.D) {

            contexto.duplicarSelecionados();

            atualizarInterface();

            evento.consume();
            return;
        }

        // Demais atalhos.
        switch (evento.getCode()) {

            // D - Desenhar polígono.
            case D -> {

                contexto.limparSelecao();
                contexto.setFormaEmAndamento(null);

                contexto.setFerramentaAtual(
                        new FerramentaDesenhar()
                );
            }

            // S - Selecionar e mover.
            case S -> {

                contexto.setFormaEmAndamento(null);

                contexto.setFerramentaAtual(
                        new FerramentaSelecionar()
                );
            }

            // C - Criar círculo.
            case C -> {

                contexto.limparSelecao();
                contexto.setFormaEmAndamento(null);

                contexto.setFerramentaAtual(
                        new FerramentaInserirForma(
                                FerramentaInserirForma.TipoForma.CIRCULO
                        )
                );
            }

            // Q - Criar quadrado.
            case Q -> {

                contexto.limparSelecao();
                contexto.setFormaEmAndamento(null);

                contexto.setFerramentaAtual(
                        new FerramentaInserirForma(
                                FerramentaInserirForma.TipoForma.QUADRADO
                        )
                );
            }

            // H - Criar hexágono.
            case H -> {

                contexto.limparSelecao();
                contexto.setFormaEmAndamento(null);

                contexto.setFerramentaAtual(
                        new FerramentaInserirForma(
                                FerramentaInserirForma.TipoForma.HEXAGONO
                        )
                );
            }

            // E - Escala.
            case E -> {

                contexto.setFormaEmAndamento(null);

                contexto.setFerramentaAtual(
                        new FerramentaTransformar(
                                FerramentaTransformar.Modo.ESCALA
                        )
                );
            }

            // R - Rotação.
            case R -> {

                contexto.setFormaEmAndamento(null);

                contexto.setFerramentaAtual(
                        new FerramentaTransformar(
                                FerramentaTransformar.Modo.ROTACAO
                        )
                );
            }

            // T - Cisalhamento.
            case T -> {

                contexto.setFormaEmAndamento(null);

                contexto.setFerramentaAtual(
                        new FerramentaTransformar(
                                FerramentaTransformar.Modo.CISALHAMENTO
                        )
                );
            }

            // V - Editar vértices.
            case V -> {

                contexto.setFormaEmAndamento(null);

                contexto.setFerramentaAtual(
                        new FerramentaEditarVertice()
                );
            }

            // G - Agrupar formas selecionadas.
            case G -> contexto.agruparSelecionados();

            // U - Desagrupar formas selecionadas.
            case U -> contexto.desagruparSelecionados();

            // F - Trazer para frente.
            case F -> contexto.trazerParaFrente();

            // B - Enviar para trás.
            case B -> contexto.enviarParaTras();

            // N - Ativar/desativar Snap to Grid.
            case N -> contexto.setGridSnappingHabilitado(
                    !contexto.isGridSnappingHabilitado()
            );

            default -> {
                return;
            }
        }

        atualizarInterface();
        evento.consume();
    }

    // ==========================================
    // EVENTOS DO MOUSE
    // ==========================================

    private void aoClicarMouse(MouseEvent evento) {

        atualizarPosicaoCursor(evento);

        if (contexto.getFerramentaAtual() != null) {

            contexto.getFerramentaAtual()
                    .aoClicar(evento, contexto);
        }

        atualizarInterface();
    }

    private void aoPressionarMouse(MouseEvent evento) {

        mouseDentroCanvas = true;

        atualizarPosicaoCursor(evento);

        // Devolve o foco ao Canvas para permitir
        // a utilização dos atalhos do teclado.
        areaDeDesenho.requestFocus();

        if (contexto.getFerramentaAtual() != null) {

            contexto.getFerramentaAtual()
                    .aoPressionarMouse(evento, contexto);
        }

        atualizarInterface();
    }

    private void aoArrastarMouse(MouseEvent evento) {

        mouseDentroCanvas = true;

        atualizarPosicaoCursor(evento);

        if (contexto.getFerramentaAtual() != null) {

            contexto.getFerramentaAtual()
                    .aoArrastarMouse(evento, contexto);
        }

        redesenharCanvas();
    }

    private void aoSoltarMouse(MouseEvent evento) {

        mouseDentroCanvas = true;

        atualizarPosicaoCursor(evento);

        if (contexto.getFerramentaAtual() != null) {

            contexto.getFerramentaAtual()
                    .aoSoltarMouse(evento, contexto);
        }

        atualizarInterface();
    }

    private void aoMoverMouse(MouseEvent evento) {

        mouseDentroCanvas = true;

        atualizarPosicaoCursor(evento);

        // Atualiza o desenho enquanto um polígono
        // está sendo criado.
        if (contexto.getFormaEmAndamento() != null) {

            redesenharCanvas();

        } else if (contexto.getFerramentaAtual()
                instanceof FerramentaSelecionar ferramenta) {

            // Atualiza a caixa de seleção enquanto ela é arrastada.
            if (ferramenta.isArrastandoCaixa()) {

                redesenharCanvas();

            } else {

                painelStatus.atualizar();
            }

        } else {

            painelStatus.atualizar();
        }
    }

    private void aoEntrarCanvas(MouseEvent evento) {

        mouseDentroCanvas = true;

        atualizarPosicaoCursor(evento);

        redesenharCanvas();
    }

    private void aoSairCanvas(MouseEvent evento) {

        mouseDentroCanvas = false;

        painelStatus.marcarCursorFora();

        redesenharCanvas();

        // Mantém a indicação de que o cursor está fora do Canvas.
        painelStatus.marcarCursorFora();
    }

    // ==========================================
    // COORDENADAS E STATUS
    // ==========================================

    private void atualizarPosicaoCursor(MouseEvent evento) {

        mouseX = evento.getX();
        mouseY = evento.getY();

        double xStatus = mouseX;
        double yStatus = mouseY;

        // Quando a grade está ativa, mostra no status
        // as coordenadas ajustadas à grade.
        if (contexto.isGridSnappingHabilitado()) {

            xStatus = contexto.aplicarSnap(mouseX);
            yStatus = contexto.aplicarSnap(mouseY);
        }

        painelStatus.definirCoordenadas(
                xStatus,
                yStatus
        );
    }

    // ==========================================
    // ATUALIZAÇÃO DA INTERFACE
    // ==========================================

    private void atualizarInterface() {

        redesenharCanvas();

        barraLateral.atualizarCoresInterface();
        barraLateral.atualizarEstadoGrade();

        painelStatus.atualizar();
    }

    // ==========================================
    // REDESENHO DO CANVAS
    // ==========================================

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
        desenharGrade();

        // Desenha as formas finalizadas.
        for (FormaGeometrica forma : contexto.getFormas()) {

            desenharForma(
                    forma,
                    false,
                    contexto.estaSelecionada(forma)
            );
        }

        // Desenha a forma que está sendo criada.
        if (contexto.getFormaEmAndamento() != null) {

            desenharForma(
                    contexto.getFormaEmAndamento(),
                    true,
                    false
            );
        }

        desenharCaixaSelecao();

        // Atualiza o total de formas, a seleção,
        // a área e o perímetro no painel de status.
        painelStatus.atualizar();
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

    // ==========================================
    // SNAP TO GRID
    // ==========================================

    private void desenharGrade() {

        if (!contexto.isGridSnappingHabilitado()) {
            return;
        }

        GraphicsContext gc =
                areaDeDesenho.getGraphicsContext2D();

        double tamanho = contexto.getTamanhoGrade();

        if (tamanho <= 0) {
            return;
        }

        double largura = areaDeDesenho.getWidth();
        double altura = areaDeDesenho.getHeight();

        gc.save();

        gc.setStroke(Color.rgb(230, 230, 230));
        gc.setLineWidth(0.5);

        // Linhas verticais.
        for (double x = 0; x <= largura; x += tamanho) {

            gc.strokeLine(
                    x,
                    0,
                    x,
                    altura
            );
        }

        // Linhas horizontais.
        for (double y = 0; y <= altura; y += tamanho) {

            gc.strokeLine(
                    0,
                    y,
                    largura,
                    y
            );
        }

        gc.restore();
    }

    // ==========================================
    // DESENHAR FORMAS
    // ==========================================

    private void desenharForma(
            FormaGeometrica forma,
            boolean emAndamento,
            boolean selecionada) {

        if (forma == null) {
            return;
        }

        GraphicsContext gc =
                areaDeDesenho.getGraphicsContext2D();

        // O círculo possui desenho próprio para preservar
        // seu centro, raio e representação após cisalhamento.
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

        gc.save();

        // Cor da borda.
        try {

            gc.setStroke(
                    Color.web(forma.getCorBordaHex())
            );

        } catch (Exception e) {

            gc.setStroke(Color.BLACK);
        }

        gc.setLineWidth(
                Math.max(0.5, forma.getEspessuraBorda())
        );

        // Destaca as formas selecionadas.
        if (selecionada) {

            gc.setStroke(Color.BLUE);

            gc.setLineWidth(
                    Math.max(0.5, forma.getEspessuraBorda()) + 3
            );
        }

        // Quadrados e hexágonos aparecem fechados
        // mesmo durante a criação.
        boolean formaFechada =
                (!emAndamento && pontos.size() >= 3)
                        || forma instanceof Quadrado
                        || forma instanceof Hexagono;

        if (formaFechada) {

            // Cor de preenchimento.
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

            // Desenha os segmentos já definidos do polígono.
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

        desenharVertices(
                pontos,
                gc
        );

        gc.restore();
    }

    // ==========================================
    // DESENHAR CÍRCULO
    // ==========================================

    private void desenharCirculo(
            Circulo circulo,
            boolean selecionada) {

        GraphicsContext gc =
                areaDeDesenho.getGraphicsContext2D();

        Ponto centro = circulo.getCentro();

        if (centro == null) {
            return;
        }

        double centroX = centro.getX();
        double centroY = centro.getY();
        double raio = circulo.getRaio();

        gc.save();

        // Cor da borda.
        try {

            gc.setStroke(
                    Color.web(circulo.getCorBordaHex())
            );

        } catch (Exception e) {

            gc.setStroke(Color.BLACK);
        }

        gc.setLineWidth(
                Math.max(0.5, circulo.getEspessuraBorda())
        );

        // Destaca o círculo quando selecionado.
        if (selecionada) {

            gc.setStroke(Color.BLUE);

            gc.setLineWidth(
                    Math.max(0.5, circulo.getEspessuraBorda()) + 3
            );
        }

        // Cor de preenchimento.
        try {

            gc.setFill(
                    Color.web(
                            circulo.getCorPreenchimentoHex()
                    )
            );

        } catch (Exception e) {

            gc.setFill(Color.WHITE);
        }

        // Se o círculo sofreu cisalhamento, utiliza os pontos
        // para desenhar o contorno transformado.
        if (circulo.isCisalhado()) {

            List<Ponto> pontos = circulo.getPontos();

            if (pontos != null && pontos.size() >= 3) {

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
            }

        } else {

            // Círculo normal: utiliza centro e raio.
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

        // Marca o centro do círculo.
        gc.setFill(Color.BLACK);

        gc.fillOval(
                centroX - 3,
                centroY - 3,
                6,
                6
        );

        gc.restore();
    }

    // ==========================================
    // VÉRTICES
    // ==========================================

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

    // ==========================================
    // CAIXA DE SELEÇÃO
    // ==========================================

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

        gc.save();

        gc.setStroke(Color.GRAY);
        gc.setLineWidth(1.0);
        gc.setLineDashes(5);

        gc.strokeRect(
                Math.min(startX, mouseX),
                Math.min(startY, mouseY),
                Math.abs(mouseX - startX),
                Math.abs(mouseY - startY)
        );

        // Restaura a linha contínua.
        gc.setLineDashes();

        gc.restore();
    }

    // ==========================================
    // GETTERS
    // ==========================================

    public Canvas getAreaDeDesenho() {
        return areaDeDesenho;
    }

    public EditorContext getContexto() {
        return contexto;
    }
}