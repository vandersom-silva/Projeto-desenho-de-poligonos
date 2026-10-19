package org.example.Views;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.layout.HBox;
import javafx.geometry.Orientation;

import org.example.Controller.EditorContext;
import org.example.Models.FormaGeometrica;

import java.util.List;
import java.util.Locale;

public class PainelStatusView extends HBox {

    private final EditorContext contexto;

    private final Label coordenadasLabel =
            new Label("Cursor: (0, 0)");

    private final Label totalFormasLabel =
            new Label("Formas: 0");

    private final Label selecionadasLabel =
            new Label("Selecionadas: 0");

    private final Label areaLabel =
            new Label("Área: —");

    private final Label perimetroLabel =
            new Label("Perímetro: —");

    private double mouseX;
    private double mouseY;

    private boolean cursorDentro = true;

    public PainelStatusView(EditorContext contexto) {

        this.contexto = contexto;

        setAlignment(Pos.CENTER_LEFT);
        setSpacing(16);
        setPadding(new Insets(10, 14, 10, 14));

        setMinHeight(42);
        setStyle(
                "-fx-background-color: #f3f4f6;"
                        + "-fx-border-color: #d1d5db;"
                        + "-fx-border-width: 1 0 0 0;"
                        + "-fx-font-size: 12px;"
        );

        coordenadasLabel.setMinWidth(135);
        totalFormasLabel.setMinWidth(85);
        selecionadasLabel.setMinWidth(115);
        areaLabel.setMinWidth(105);
        perimetroLabel.setMinWidth(125);

        getChildren().addAll(
                coordenadasLabel,
                new Separator(Orientation.VERTICAL),
                totalFormasLabel,
                new Separator(Orientation.VERTICAL),
                selecionadasLabel,
                new Separator(Orientation.VERTICAL),
                areaLabel,
                new Separator(Orientation.VERTICAL),
                perimetroLabel
        );

        atualizar();
    }

    /**
     * Atualiza a posição do cursor exibida no painel.
     */
    public void definirCoordenadas(double x, double y) {

        mouseX = x;
        mouseY = y;
        cursorDentro = true;

        atualizar();
    }

    /**
     * Informa que o cursor saiu da área de desenho.
     */
    public void marcarCursorFora() {

        cursorDentro = false;

        atualizar();
    }

    /**
     * Atualiza as coordenadas, quantidade de formas,
     * seleção, área e perímetro.
     */
    public void atualizar() {

        if (cursorDentro) {

            coordenadasLabel.setText(
                    String.format(
                            Locale.getDefault(),
                            "Cursor: (%.1f, %.1f)",
                            mouseX,
                            mouseY
                    )
            );

        } else {

            coordenadasLabel.setText(
                    "Cursor: fora da área"
            );
        }

        totalFormasLabel.setText(
                "Formas: " + contexto.getFormas().size()
        );

        List<FormaGeometrica> selecionados =
                contexto.getSelecionados();

        selecionadasLabel.setText(
                "Selecionadas: " + selecionados.size()
        );

        /*
         * Se houver várias formas selecionadas,
         * mostramos a área e o perímetro da forma
         * principal selecionada.
         */
        FormaGeometrica forma =
                contexto.getFormaPrincipalSelecionada();

        if (forma == null
                || !selecionados.contains(forma)
                || !contexto.getFormas().contains(forma)) {

            areaLabel.setText("Área: —");
            perimetroLabel.setText("Perímetro: —");

            return;
        }

        areaLabel.setText(
                String.format(
                        Locale.getDefault(),
                        "Área: %.2f",
                        forma.calcularArea()
                )
        );

        perimetroLabel.setText(
                String.format(
                        Locale.getDefault(),
                        "Perímetro: %.2f",
                        forma.calcularPerimetro()
                )
        );
    }
}