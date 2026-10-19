
package org.example.Views;

import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Slider;
import javafx.scene.control.TitledPane;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

import org.example.Controller.EditorContext;
import org.example.Controller.Ferramenta;
import org.example.Controller.FerramentaDesenhar;
import org.example.Controller.FerramentaEditarVertice;
import org.example.Controller.FerramentaInserirForma;
import org.example.Controller.FerramentaSelecionar;
import org.example.Controller.FerramentaTransformar;
import org.example.Models.FormaGeometrica;

import java.util.List;

public class BarraLateralView extends ScrollPane {

    private final EditorContext contexto;
    private final Runnable aoAtualizar;

    private final ColorPicker corPreenchimento =
            new ColorPicker(Color.WHITE);

    private final ColorPicker corBorda =
            new ColorPicker(Color.BLACK);

    private final Slider espessuraBorda =
            new Slider(1, 10, 1);

    private final Label textoEspessura =
            new Label("Espessura: 1.0");

    private final Label mensagemEstilo =
            new Label("Selecione uma forma para editar o estilo.");

    private final ToggleButton botaoGrade =
            new ToggleButton();

    private boolean atualizandoControles = false;

    public BarraLateralView(
            EditorContext contexto,
            Runnable aoAtualizar) {

        this.contexto = contexto;
        this.aoAtualizar = aoAtualizar;

        setPrefWidth(245);
        setMinWidth(225);
        setFitToWidth(true);
        setHbarPolicy(ScrollBarPolicy.NEVER);

        VBox painel = new VBox(10);
        painel.setPadding(new Insets(12));
        painel.setStyle(
                "-fx-background-color: #f3f4f6;"
        );

        Label titulo = new Label("Editor Vetorial");
        titulo.setStyle(
                "-fx-font-size: 18px; -fx-font-weight: bold;"
        );

        painel.getChildren().addAll(
                titulo,
                construirSecaoFerramentas(),
                construirSecaoEstilos(),
                construirSecaoOrganizacao(),
                construirSecaoHistorico()
        );

        setContent(painel);

        configurarControlesDeEstilo();
        atualizarEstadoGrade();
    }

    // ==========================================
    // FERRAMENTAS
    // ==========================================

    private TitledPane construirSecaoFerramentas() {

        VBox conteudo = new VBox(6);

        conteudo.getChildren().addAll(
                criarBotao("Desenhar polígono (D)",
                        () -> ativarFerramenta(
                                new FerramentaDesenhar(), true)),

                criarBotao("Selecionar / mover (S)",
                        () -> ativarFerramenta(
                                new FerramentaSelecionar(), false)),

                criarBotao("Círculo (C)",
                        () -> ativarFerramenta(
                                new FerramentaInserirForma(
                                        FerramentaInserirForma.TipoForma.CIRCULO
                                ), true)),

                criarBotao("Quadrado (Q)",
                        () -> ativarFerramenta(
                                new FerramentaInserirForma(
                                        FerramentaInserirForma.TipoForma.QUADRADO
                                ), true)),

                criarBotao("Hexágono (H)",
                        () -> ativarFerramenta(
                                new FerramentaInserirForma(
                                        FerramentaInserirForma.TipoForma.HEXAGONO
                                ), true)),

                criarBotao("Escala (E)",
                        () -> ativarFerramenta(
                                new FerramentaTransformar(
                                        FerramentaTransformar.Modo.ESCALA
                                ), false)),

                criarBotao("Rotação (R)",
                        () -> ativarFerramenta(
                                new FerramentaTransformar(
                                        FerramentaTransformar.Modo.ROTACAO
                                ), false)),

                criarBotao("Cisalhamento (T)",
                        () -> ativarFerramenta(
                                new FerramentaTransformar(
                                        FerramentaTransformar.Modo.CISALHAMENTO
                                ), false)),

                criarBotao("Editar vértices (V)",
                        () -> ativarFerramenta(
                                new FerramentaEditarVertice(), false))
        );

        return criarSecao("Ferramentas", conteudo);
    }

    private void ativarFerramenta(
            Ferramenta ferramenta,
            boolean limparSelecao) {

        if (limparSelecao) {
            contexto.limparSelecao();
        }

        contexto.setFormaEmAndamento(null);
        contexto.setFerramentaAtual(ferramenta);

        atualizarInterface();
    }

    // ==========================================
    // ESTILOS
    // ==========================================

    private TitledPane construirSecaoEstilos() {

        VBox conteudo = new VBox(8);

        corPreenchimento.setMaxWidth(Double.MAX_VALUE);
        corBorda.setMaxWidth(Double.MAX_VALUE);

        espessuraBorda.setMin(1);
        espessuraBorda.setMax(10);
        espessuraBorda.setMajorTickUnit(1);
        espessuraBorda.setMinorTickCount(0);
        espessuraBorda.setSnapToTicks(true);
        espessuraBorda.setShowTickMarks(true);

        conteudo.getChildren().addAll(
                new Label("Cor de preenchimento"),
                corPreenchimento,

                new Label("Cor da borda"),
                corBorda,

                textoEspessura,
                espessuraBorda,

                mensagemEstilo
        );

        mensagemEstilo.setWrapText(true);
        mensagemEstilo.setStyle(
                "-fx-font-size: 11px; -fx-text-fill: #555555;"
        );

        return criarSecao("Estilos", conteudo);
    }

    private void configurarControlesDeEstilo() {

        corPreenchimento.setOnAction(event -> {

            if (!atualizandoControles) {
                aplicarCorPreenchimento();
            }
        });

        corBorda.setOnAction(event -> {

            if (!atualizandoControles) {
                aplicarCorBorda();
            }
        });

        espessuraBorda.valueProperty().addListener(
                (obs, valorAntigo, valorNovo) -> {

                    textoEspessura.setText(
                            String.format(
                                    "Espessura: %.1f",
                                    valorNovo.doubleValue()
                            )
                    );
                }
        );

        // Aplica a espessura quando o usuário termina
        // de arrastar o controle deslizante.
        espessuraBorda.setOnMouseReleased(event ->
                aplicarEspessuraBorda()
        );

        espessuraBorda.setOnKeyReleased(event ->
                aplicarEspessuraBorda()
        );
    }

    private void aplicarCorPreenchimento() {

        List<FormaGeometrica> alvos =
                contexto.getFormasAfetadasPorSelecao();

        if (alvos.isEmpty()) {
            mensagemEstilo.setText(
                    "Selecione uma forma primeiro."
            );
            return;
        }

        contexto.registrarEstado();

        String hex = converterCorParaHex(
                corPreenchimento.getValue()
        );

        for (FormaGeometrica forma : alvos) {
            forma.setCorPreenchimentoHex(hex);
        }

        mensagemEstilo.setText(
                "Preenchimento atualizado."
        );

        atualizarInterface();
    }

    private void aplicarCorBorda() {

        List<FormaGeometrica> alvos =
                contexto.getFormasAfetadasPorSelecao();

        if (alvos.isEmpty()) {
            mensagemEstilo.setText(
                    "Selecione uma forma primeiro."
            );
            return;
        }

        contexto.registrarEstado();

        String hex = converterCorParaHex(
                corBorda.getValue()
        );

        for (FormaGeometrica forma : alvos) {
            forma.setCorBordaHex(hex);
        }

        mensagemEstilo.setText(
                "Cor da borda atualizada."
        );

        atualizarInterface();
    }

    private void aplicarEspessuraBorda() {

        if (atualizandoControles) {
            return;
        }

        List<FormaGeometrica> alvos =
                contexto.getFormasAfetadasPorSelecao();

        if (alvos.isEmpty()) {
            return;
        }

        contexto.registrarEstado();

        double espessura = espessuraBorda.getValue();

        for (FormaGeometrica forma : alvos) {
            forma.setEspessuraBorda(espessura);
        }

        mensagemEstilo.setText(
                "Espessura da borda atualizada."
        );

        atualizarInterface();
    }

    private String converterCorParaHex(Color cor) {

        int vermelho = (int) Math.round(cor.getRed() * 255);
        int verde = (int) Math.round(cor.getGreen() * 255);
        int azul = (int) Math.round(cor.getBlue() * 255);

        return String.format(
                "#%02X%02X%02X",
                vermelho,
                verde,
                azul
        );
    }

    public void atualizarCoresInterface() {

        List<FormaGeometrica> selecionados =
                contexto.getSelecionados();

        atualizandoControles = true;

        try {

            if (selecionados.isEmpty()) {

                mensagemEstilo.setText(
                        "Selecione uma forma para editar o estilo."
                );

                return;
            }

            FormaGeometrica forma = selecionados.get(0);

            try {
                corPreenchimento.setValue(
                        Color.web(
                                forma.getCorPreenchimentoHex()
                        )
                );
            } catch (Exception ignored) {
                corPreenchimento.setValue(Color.WHITE);
            }

            try {
                corBorda.setValue(
                        Color.web(
                                forma.getCorBordaHex()
                        )
                );
            } catch (Exception ignored) {
                corBorda.setValue(Color.BLACK);
            }

            espessuraBorda.setValue(
                    Math.max(
                            espessuraBorda.getMin(),
                            Math.min(
                                    espessuraBorda.getMax(),
                                    forma.getEspessuraBorda()
                            )
                    )
            );

            mensagemEstilo.setText(
                    selecionados.size()
                            + " forma(s) selecionada(s)."
            );

        } finally {

            atualizandoControles = false;
        }
    }

    // ==========================================
    // AGRUPAMENTO, CAMADAS E SNAP
    // ==========================================

    private TitledPane construirSecaoOrganizacao() {

        VBox conteudo = new VBox(6);

        botaoGrade.setMaxWidth(Double.MAX_VALUE);

        botaoGrade.setOnAction(event -> {

            contexto.setGridSnappingHabilitado(
                    botaoGrade.isSelected()
            );

            atualizarEstadoGrade();
            atualizarInterface();
        });

        conteudo.getChildren().addAll(

                criarBotao("Agrupar selecionados (G)", () -> {
                    contexto.agruparSelecionados();
                }),

                criarBotao("Desagrupar (U)", () -> {
                    contexto.desagruparSelecionados();
                }),

                criarBotao("Duplicar (Ctrl+D)", () -> {
                    contexto.duplicarSelecionados();
                }),

                criarBotao("Trazer para frente (F)", () -> {
                    contexto.trazerParaFrente();
                }),

                criarBotao("Enviar para trás (B)", () -> {
                    contexto.enviarParaTras();
                }),

                botaoGrade
        );

        return criarSecao("Organização", conteudo);
    }

    public void atualizarEstadoGrade() {

        boolean habilitada =
                contexto.isGridSnappingHabilitado();

        botaoGrade.setSelected(habilitada);

        botaoGrade.setText(
                habilitada
                        ? "Grade: ligada (N)"
                        : "Grade: desligada (N)"
        );
    }

    // ==========================================
    // HISTÓRICO
    // ==========================================

    private TitledPane construirSecaoHistorico() {

        VBox conteudo = new VBox(6);

        conteudo.getChildren().addAll(

                criarBotao("Desfazer (Ctrl+Z)", () -> {
                    contexto.desfazer();
                }),

                criarBotao("Refazer (Ctrl+Y)", () -> {
                    contexto.refazer();
                })
        );

        return criarSecao("Histórico", conteudo);
    }

    // ==========================================
    // COMPONENTES AUXILIARES
    // ==========================================

    private TitledPane criarSecao(
            String titulo,
            Node conteudo) {

        TitledPane secao = new TitledPane(
                titulo,
                conteudo
        );

        secao.setExpanded(true);

        return secao;
    }

    private Button criarBotao(
            String texto,
            Runnable acao) {

        Button botao = new Button(texto);

        botao.setMaxWidth(Double.MAX_VALUE);

        botao.setOnAction(event -> {

            acao.run();

            atualizarInterface();
        });

        return botao;
    }

    private void atualizarInterface() {

        atualizarCoresInterface();
        atualizarEstadoGrade();

        contexto.requestRedraw();
        contexto.requestStatsUpdate();

        aoAtualizar.run();
    }
}
