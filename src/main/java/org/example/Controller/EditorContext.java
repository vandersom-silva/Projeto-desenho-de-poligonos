package org.example.Controller;

import org.example.Models.FormaGeometrica;

import java.util.ArrayList;
import java.util.List;

public class EditorContext {

    private List<FormaGeometrica> formas;

    private List<FormaGeometrica> selecionados;

    private FormaGeometrica formaEmAndamento;

    private Ferramenta ferramentaAtual;

    private boolean gridSnappingHabilitado;

    public EditorContext() {

        formas = new ArrayList<>();
        selecionados = new ArrayList<>();

        gridSnappingHabilitado = false;
    }

    public List<FormaGeometrica> getFormas() {
        return formas;
    }

    public List<FormaGeometrica> getSelecionados() {
        return selecionados;
    }

    public FormaGeometrica getFormaEmAndamento() {
        return formaEmAndamento;
    }

    public void setFormaEmAndamento(
            FormaGeometrica formaEmAndamento) {

        this.formaEmAndamento = formaEmAndamento;
    }

    public Ferramenta getFerramentaAtual() {
        return ferramentaAtual;
    }

    public void setFerramentaAtual(
            Ferramenta ferramentaAtual) {

        this.ferramentaAtual = ferramentaAtual;
    }

    public boolean isGridSnappingHabilitado() {
        return gridSnappingHabilitado;
    }

    public void setGridSnappingHabilitado(
            boolean habilitado) {

        this.gridSnappingHabilitado = habilitado;
    }

    public void finalizarFormaEmAndamento() {

        if (formaEmAndamento != null) {

            formas.add(formaEmAndamento);

            formaEmAndamento = null;

            requestRedraw();
        }
    }

    public void limparSelecao() {
        selecionados.clear();
    }

    public void adicionarSelecionado(
            FormaGeometrica forma) {

        if (forma != null
                && !selecionados.contains(forma)) {

            selecionados.add(forma);
        }
    }

    public boolean estaSelecionada(
            FormaGeometrica forma) {

        return selecionados.contains(forma);
    }

    public void requestRedraw() {
        // Será usado pela interface gráfica.
    }

    public void requestStatsUpdate() {
        // Implementaremos posteriormente.
    }
}