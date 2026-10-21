package org.example.Views;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.Window;

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

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

public class ContainerApp extends BorderPane {

    private final Canvas areaDeDesenho;
    private final EditorContext contexto;
    private final BarraLateralView barraLateral;
    private final PainelStatusView painelStatus;

    // Coordenadas reais do cursor dentro do Canvas.
    // O Snap to Grid é aplicado separadamente quando necessário.
    private double mouseX;
    private double mouseY;

    private boolean mouseDentroCanvas;

    // Caminho do último arquivo salvo/aberto e fotografia do último estado salvo.
    private Path arquivoAtual;
    private String estadoSalvo;

    public ContainerApp() {

        contexto = new EditorContext();

        // Ferramenta inicial: desenho de polígonos por cliques.
        contexto.setFerramentaAtual(
                new FerramentaDesenhar()
        );

        areaDeDesenho = new Canvas(1000, 600);
        areaDeDesenho.setFocusTraversable(true);

        barraLateral = new BarraLateralView(
                contexto,
                this::redesenharCanvas
        );

        painelStatus = new PainelStatusView(contexto);

        setLeft(barraLateral);
        setCenter(areaDeDesenho);
        setBottom(painelStatus);
        setTop(criarMenuArquivo());

        inicializarEventos();
        redesenharCanvas();
        painelStatus.atualizar();

        // A tela vazia inicial não possui alterações pendentes.
        estadoSalvo = estadoAtualJson();
        configurarAvisoAoFechar();
    }

    // ==========================================
    // MENU ARQUIVO: SALVAR / ABRIR JSON
    // ==========================================

    private MenuBar criarMenuArquivo() {
        Menu menuArquivo = new Menu("Arquivo");

        MenuItem itemSalvar = new MenuItem("Salvar");
        itemSalvar.setOnAction(evento -> salvarArquivo(false));

        MenuItem itemSalvarComo = new MenuItem("Salvar como...");
        itemSalvarComo.setOnAction(evento -> salvarArquivo(true));

        MenuItem itemAbrir = new MenuItem("Abrir JSON...");
        itemAbrir.setOnAction(evento -> carregarArquivo());

        menuArquivo.getItems().addAll(
                itemAbrir,
                new javafx.scene.control.SeparatorMenuItem(),
                itemSalvar,
                itemSalvarComo
        );

        return new MenuBar(menuArquivo);
    }

    private void salvarArquivo(boolean salvarComo) {
        Path destino = arquivoAtual;

        if (salvarComo || destino == null) {
            FileChooser seletor = criarSeletorArquivo("Salvar projeto JSON");
            if (arquivoAtual != null && arquivoAtual.getParent() != null
                    && java.nio.file.Files.isDirectory(arquivoAtual.getParent())) {
                seletor.setInitialDirectory(arquivoAtual.getParent().toFile());
                seletor.setInitialFileName(arquivoAtual.getFileName().toString());
            } else {
                seletor.setInitialFileName("meu-desenho.json");
            }

            File escolhido = seletor.showSaveDialog(janelaAtual());
            if (escolhido == null) {
                return;
            }
            destino = adicionarExtensaoJson(escolhido.toPath());
        }

        try {
            PersistenciaJson.salvar(
                    destino,
                    contexto.getFormas(),
                    areaDeDesenho.getWidth(),
                    areaDeDesenho.getHeight(),
                    contexto.isGridSnappingHabilitado()
            );

            arquivoAtual = destino.toAbsolutePath().normalize();
            estadoSalvo = estadoAtualJson();
            mostrarAlerta(
                    Alert.AlertType.INFORMATION,
                    "Projeto salvo",
                    "O projeto foi salvo com sucesso.",
                    "Arquivo: " + arquivoAtual
            );

        } catch (IOException | RuntimeException ex) {
            mostrarAlerta(
                    Alert.AlertType.ERROR,
                    "Erro ao salvar",
                    "Não foi possível salvar o projeto.",
                    mensagemErro(ex)
            );
        }
    }

    private void carregarArquivo() {
        FileChooser seletor = criarSeletorArquivo("Abrir projeto JSON");
        File escolhido = seletor.showOpenDialog(janelaAtual());

        if (escolhido == null) {
            return;
        }

        Path origem = escolhido.toPath();
        final PersistenciaJson.DocumentoCarregado documento;

        // Carrega e valida tudo antes de mexer no desenho atual.
        try {
            documento = PersistenciaJson.carregar(origem);
        } catch (IOException | RuntimeException ex) {
            mostrarAlerta(
                    Alert.AlertType.ERROR,
                    "Erro ao abrir arquivo",
                    "O arquivo não pôde ser aberto. O desenho atual foi preservado.",
                    mensagemErro(ex)
            );
            return;
        }

        if (!confirmarAntesDeSubstituir()) {
            return;
        }

        try {
            // Permite desfazer a substituição, conforme o histórico existente.
            contexto.registrarEstado();

            contexto.getFormas().clear();
            contexto.getFormas().addAll(documento.formas);
            contexto.limparSelecao();
            contexto.setFormaEmAndamento(null);
            contexto.setGridSnappingHabilitado(documento.snapHabilitado);

            areaDeDesenho.setWidth(documento.larguraCanvas);
            areaDeDesenho.setHeight(documento.alturaCanvas);

            arquivoAtual = origem.toAbsolutePath().normalize();
            atualizarInterface();
            estadoSalvo = estadoAtualJson();

            mostrarAlerta(
                    Alert.AlertType.INFORMATION,
                    "Projeto aberto",
                    "O arquivo foi carregado com sucesso.",
                    "Arquivo: " + arquivoAtual
            );

        } catch (RuntimeException ex) {
            mostrarAlerta(
                    Alert.AlertType.ERROR,
                    "Erro ao carregar projeto",
                    "Não foi possível aplicar o conteúdo do arquivo.",
                    mensagemErro(ex)
            );
        }
    }

    private FileChooser criarSeletorArquivo(String titulo) {
        FileChooser seletor = new FileChooser();
        seletor.setTitle(titulo);
        seletor.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Projeto JSON (*.json)", "*.json")
        );
        return seletor;
    }

    private Path adicionarExtensaoJson(Path caminho) {
        String nome = caminho.getFileName().toString();
        if (!nome.toLowerCase(java.util.Locale.ROOT).endsWith(".json")) {
            return caminho.resolveSibling(nome + ".json");
        }
        return caminho;
    }

    private Window janelaAtual() {
        return getScene() == null ? null : getScene().getWindow();
    }

    private void mostrarAlerta(
            Alert.AlertType tipo,
            String titulo,
            String cabecalho,
            String conteudo
    ) {
        Alert alerta = new Alert(tipo);
        alerta.setTitle(titulo);
        alerta.setHeaderText(cabecalho);
        alerta.setContentText(conteudo == null ? "Ocorreu um erro sem detalhes adicionais." : conteudo);
        if (janelaAtual() != null) {
            alerta.initOwner(janelaAtual());
        }
        alerta.showAndWait();
    }

    private String mensagemErro(Throwable erro) {
        String mensagem = erro.getMessage();
        if (mensagem == null || mensagem.isBlank()) {
            return erro.getClass().getSimpleName();
        }
        return mensagem;
    }

    private String estadoAtualJson() {
        return PersistenciaJson.gerarJson(
                contexto.getFormas(),
                areaDeDesenho.getWidth(),
                areaDeDesenho.getHeight(),
                contexto.isGridSnappingHabilitado()
        );
    }

    private boolean possuiAlteracoesNaoSalvas() {
        try {
            return estadoSalvo == null || !estadoSalvo.equals(estadoAtualJson());
        } catch (RuntimeException erro) {
            // Na dúvida, não descartamos silenciosamente o trabalho do usuário.
            return true;
        }
    }

    private boolean confirmarAntesDeSubstituir() {
        if (!possuiAlteracoesNaoSalvas()) {
            return true;
        }

        ButtonType botaoSalvar = new ButtonType("Salvar");
        ButtonType botaoDescartar = new ButtonType("Descartar");
        ButtonType botaoCancelar = new ButtonType(
                "Cancelar",
                ButtonBar.ButtonData.CANCEL_CLOSE
        );

        Alert alerta = new Alert(Alert.AlertType.CONFIRMATION);
        alerta.setTitle("Alterações não salvas");
        alerta.setHeaderText("Há alterações que ainda não foram salvas.");
        alerta.setContentText("Deseja salvar antes de continuar?");
        alerta.getButtonTypes().setAll(
                botaoSalvar,
                botaoDescartar,
                botaoCancelar
        );
        if (janelaAtual() != null) {
            alerta.initOwner(janelaAtual());
        }

        Optional<ButtonType> resposta = alerta.showAndWait();
        if (resposta.isEmpty() || resposta.get() == botaoCancelar) {
            return false;
        }
        if (resposta.get() == botaoDescartar) {
            return true;
        }

        salvarArquivo(false);
        return !possuiAlteracoesNaoSalvas();
    }

    private void configurarAvisoAoFechar() {
        sceneProperty().addListener((observavel, cenaAntiga, cenaNova) -> {
            if (cenaNova == null) {
                return;
            }
            cenaNova.windowProperty().addListener((obsJanela, janelaAntiga, janelaNova) -> {
                if (janelaNova instanceof Stage stage) {
                    stage.setOnCloseRequest(evento -> {
                        if (!confirmarAntesDeFechar()) {
                            evento.consume();
                        }
                    });
                }
            });
        });
    }

    private boolean confirmarAntesDeFechar() {
        if (!possuiAlteracoesNaoSalvas()) {
            return true;
        }
        return confirmarAntesDeSubstituir();
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

        // Permite usar os atalhos mesmo quando
        // um controle da barra lateral está em foco.
        addEventFilter(
                KeyEvent.KEY_PRESSED,
                this::aoPressionarTecla
        );
    }

    // ==========================================
    // ATALHOS DO TECLADO
    // ==========================================

    private void aoPressionarTecla(KeyEvent evento) {

        // Ctrl + Z: desfazer
        if (evento.isControlDown()
                && evento.getCode() == KeyCode.Z) {

            contexto.desfazer();
            atualizarInterface();
            evento.consume();
            return;
        }

        // Ctrl + Y: refazer
        if (evento.isControlDown()
                && evento.getCode() == KeyCode.Y) {

            contexto.refazer();
            atualizarInterface();
            evento.consume();
            return;
        }

        // Ctrl + D: duplicar as formas selecionadas
        if (evento.isControlDown()
                && evento.getCode() == KeyCode.D) {

            contexto.duplicarSelecionados();
            atualizarInterface();
            evento.consume();
            return;
        }

        // Demais atalhos
        switch (evento.getCode()) {

            // D - Desenhar polígono
            case D -> {
                contexto.limparSelecao();
                contexto.setFormaEmAndamento(null);

                contexto.setFerramentaAtual(
                        new FerramentaDesenhar()
                );
            }

            // S - Selecionar e mover
            case S -> {
                contexto.setFormaEmAndamento(null);

                contexto.setFerramentaAtual(
                        new FerramentaSelecionar()
                );
            }

            // C - Criar círculo
            case C -> {
                contexto.limparSelecao();
                contexto.setFormaEmAndamento(null);

                contexto.setFerramentaAtual(
                        new FerramentaInserirForma(
                                FerramentaInserirForma.TipoForma.CIRCULO
                        )
                );
            }

            // Q - Criar quadrado
            case Q -> {
                contexto.limparSelecao();
                contexto.setFormaEmAndamento(null);

                contexto.setFerramentaAtual(
                        new FerramentaInserirForma(
                                FerramentaInserirForma.TipoForma.QUADRADO
                        )
                );
            }

            // H - Criar hexágono
            case H -> {
                contexto.limparSelecao();
                contexto.setFormaEmAndamento(null);

                contexto.setFerramentaAtual(
                        new FerramentaInserirForma(
                                FerramentaInserirForma.TipoForma.HEXAGONO
                        )
                );
            }

            // E - Escala
            case E -> {
                contexto.setFormaEmAndamento(null);

                contexto.setFerramentaAtual(
                        new FerramentaTransformar(
                                FerramentaTransformar.Modo.ESCALA
                        )
                );
            }

            // R - Rotação
            case R -> {
                contexto.setFormaEmAndamento(null);

                contexto.setFerramentaAtual(
                        new FerramentaTransformar(
                                FerramentaTransformar.Modo.ROTACAO
                        )
                );
            }

            // T - Cisalhamento
            case T -> {
                contexto.setFormaEmAndamento(null);

                contexto.setFerramentaAtual(
                        new FerramentaTransformar(
                                FerramentaTransformar.Modo.CISALHAMENTO
                        )
                );
            }

            // V - Editar vértices
            case V -> {
                contexto.setFormaEmAndamento(null);

                contexto.setFerramentaAtual(
                        new FerramentaEditarVertice()
                );
            }

            // G - Agrupar formas selecionadas
            case G -> contexto.agruparSelecionados();

            // U - Desagrupar formas selecionadas
            case U -> contexto.desagruparSelecionados();

            // F - Trazer para frente
            case F -> contexto.trazerParaFrente();

            // B - Enviar para trás
            case B -> contexto.enviarParaTras();

            // N - Ativar/desativar Snap to Grid
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

        // Ao clicar no Canvas, permite voltar a usar
        // os atalhos do teclado.
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

        // Só redesenha o Canvas quando há uma prévia para atualizar.
        if (contexto.getFormaEmAndamento() != null) {
            redesenharCanvas();

        } else if (contexto.getFerramentaAtual()
                instanceof FerramentaSelecionar ferramenta) {

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
        // Reaplica o estado do cursor após a atualização das estatísticas.
        painelStatus.marcarCursorFora();
    }

    /**
     * Atualiza as coordenadas reais usadas no desenho e as coordenadas
     * exibidas na barra de status. Quando o Snap to Grid está ativo,
     * o painel apresenta as coordenadas ajustadas à grade.
     */
    private void atualizarPosicaoCursor(MouseEvent evento) {

        mouseX = evento.getX();
        mouseY = evento.getY();

        double xStatus = mouseX;
        double yStatus = mouseY;

        if (contexto.isGridSnappingHabilitado()) {
            xStatus = contexto.aplicarSnap(mouseX);
            yStatus = contexto.aplicarSnap(mouseY);
        }

        painelStatus.definirCoordenadas(xStatus, yStatus);
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

        // Protege o desenho da grade contra tamanho inválido.
        if (tamanho <= 0) {
            return;
        }

        double largura = areaDeDesenho.getWidth();
        double altura = areaDeDesenho.getHeight();

        gc.save();
        gc.setStroke(Color.rgb(230, 230, 230));
        gc.setLineWidth(0.5);

        for (double x = 0; x <= largura; x += tamanho) {
            gc.strokeLine(x, 0, x, altura);
        }

        for (double y = 0; y <= altura; y += tamanho) {
            gc.strokeLine(0, y, largura, y);
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

        // O círculo possui desenho próprio para preservar o centro,
        // o raio e a exibição após cisalhamento.
        if (forma instanceof Circulo circulo) {
            desenharCirculo(circulo, selecionada);
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

        try {
            gc.setStroke(Color.web(forma.getCorBordaHex()));
        } catch (Exception e) {
            gc.setStroke(Color.BLACK);
        }

        gc.setLineWidth(Math.max(0.5, forma.getEspessuraBorda()));

        if (selecionada) {
            gc.setStroke(Color.BLUE);
            gc.setLineWidth(
                    Math.max(0.5, forma.getEspessuraBorda()) + 3
            );
        }

        // Quadrados e hexágonos aparecem fechados mesmo durante a criação.
        boolean formaFechada =
                (!emAndamento && pontos.size() >= 3)
                        || forma instanceof Quadrado
                        || forma instanceof Hexagono;

        if (formaFechada) {

            try {
                gc.setFill(Color.web(forma.getCorPreenchimentoHex()));
            } catch (Exception e) {
                gc.setFill(Color.WHITE);
            }

            gc.fillPolygon(x, y, pontos.size());
            gc.strokePolygon(x, y, pontos.size());

        } else {

            if (pontos.size() >= 2) {
                gc.strokePolyline(x, y, pontos.size());
            }

            // Pré-visualização do próximo segmento do polígono.
            if (emAndamento && mouseDentroCanvas && pontos.size() >= 1) {

                Ponto ultimoPonto = pontos.get(pontos.size() - 1);

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

        try {
            gc.setStroke(Color.web(circulo.getCorBordaHex()));
        } catch (Exception e) {
            gc.setStroke(Color.BLACK);
        }

        gc.setLineWidth(Math.max(0.5, circulo.getEspessuraBorda()));

        if (selecionada) {
            gc.setStroke(Color.BLUE);
            gc.setLineWidth(
                    Math.max(0.5, circulo.getEspessuraBorda()) + 3
            );
        }

        try {
            gc.setFill(Color.web(circulo.getCorPreenchimentoHex()));
        } catch (Exception e) {
            gc.setFill(Color.WHITE);
        }

        if (circulo.isCisalhado()) {

            List<Ponto> pontos = circulo.getPontos();

            if (pontos != null && pontos.size() >= 3) {
                double[] x = new double[pontos.size()];
                double[] y = new double[pontos.size()];

                for (int i = 0; i < pontos.size(); i++) {
                    x[i] = pontos.get(i).getX();
                    y[i] = pontos.get(i).getY();
                }

                gc.fillPolygon(x, y, pontos.size());
                gc.strokePolygon(x, y, pontos.size());
            }

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
