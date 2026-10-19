package org.example.Controller;

import org.example.Models.FormaGeometrica;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.HashMap;
import java.util.Map;

public class EditorContext {

    private List<FormaGeometrica> formas;
    private List<FormaGeometrica> selecionados;

    private FormaGeometrica formaEmAndamento;
    private FormaGeometrica formaPrincipalSelecionada;

    private Ferramenta ferramentaAtual;

    private boolean gridSnappingHabilitado;

    private static final double TAMANHO_GRADE = 20.0;

    private final HistoricoController historicoController =
            new HistoricoController();



    public EditorContext() {

        formas = new ArrayList<>();
        selecionados = new ArrayList<>();

        formaEmAndamento = null;
        formaPrincipalSelecionada = null;
        ferramentaAtual = null;

        gridSnappingHabilitado = false;
    }

    // =========================
    // GETTERS E SETTERS
    // =========================

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

    public FormaGeometrica getFormaPrincipalSelecionada() {
        return formaPrincipalSelecionada;
    }

    public void setFormaPrincipalSelecionada(
            FormaGeometrica forma) {

        if (forma != null && selecionados.contains(forma)) {
            formaPrincipalSelecionada = forma;
        }
    }

    public Ferramenta getFerramentaAtual() {
        return ferramentaAtual;
    }

    public void setFerramentaAtual(Ferramenta ferramentaAtual) {
        this.ferramentaAtual = ferramentaAtual;
    }

    public boolean isGridSnappingHabilitado() {
        return gridSnappingHabilitado;
    }

    public void setGridSnappingHabilitado(boolean habilitado) {
        this.gridSnappingHabilitado = habilitado;
    }

    public double getTamanhoGrade() {
        return TAMANHO_GRADE;
    }

    // =========================
    // SNAP TO GRID
    // =========================

    public double aplicarSnap(double valor) {

        if (!gridSnappingHabilitado) {
            return valor;
        }

        return Math.round(valor / TAMANHO_GRADE)
                * TAMANHO_GRADE;
    }

    // =========================
    // FINALIZAÇÃO DE FORMAS
    // =========================

    public void finalizarFormaEmAndamento() {

        if (formaEmAndamento != null) {

            registrarEstado();

            formas.add(formaEmAndamento);
            formaEmAndamento = null;

            requestRedraw();
            requestStatsUpdate();
        }
    }

    // =========================
    // SELEÇÃO
    // =========================

    public void limparSelecao() {

        selecionados.clear();
        formaPrincipalSelecionada = null;
    }

    public void adicionarSelecionado(
            FormaGeometrica forma) {

        if (forma == null) {
            return;
        }

        if (!selecionados.contains(forma)) {
            selecionados.add(forma);
        }

        if (formaPrincipalSelecionada == null) {
            formaPrincipalSelecionada = forma;
        }
    }

    public boolean estaSelecionada(
            FormaGeometrica forma) {

        return selecionados.contains(forma);
    }

    public void selecionarFormaOuGrupo(
            FormaGeometrica forma) {

        if (forma == null) {
            return;
        }

        limparSelecao();

        formaPrincipalSelecionada = forma;

        String groupId = forma.getGroupId();

        if (groupId == null || groupId.isBlank()) {

            adicionarSelecionado(forma);
            return;
        }

        for (FormaGeometrica outra : formas) {

            if (groupId.equals(outra.getGroupId())) {
                adicionarSelecionado(outra);
            }
        }
    }

    // =========================
    // AGRUPAMENTO
    // =========================

    public void agruparSelecionados() {

        List<FormaGeometrica> formasGrupo =
                getFormasAfetadasPorSelecao();

        if (formasGrupo.size() < 2) {
            return;
        }

        String novoGroupId =
                UUID.randomUUID().toString();

        for (FormaGeometrica forma : formasGrupo) {
            forma.setGroupId(novoGroupId);
        }

        selecionados.clear();
        selecionados.addAll(formasGrupo);

        if (formaPrincipalSelecionada == null
                || !selecionados.contains(
                formaPrincipalSelecionada)) {

            formaPrincipalSelecionada = formasGrupo.get(0);
        }

        requestRedraw();
        requestStatsUpdate();
    }

    // =========================
    // DESAGRUPAMENTO
    // =========================

    public void desagruparSelecionados() {

        FormaGeometrica formaPrincipal =
                formaPrincipalSelecionada;

        if (formaPrincipal == null
                || !selecionados.contains(formaPrincipal)) {

            formaPrincipal = selecionados.isEmpty()
                    ? null
                    : selecionados.get(0);
        }

        Set<String> idsDosGrupos = new HashSet<>();

        for (FormaGeometrica forma : selecionados) {

            String id = forma.getGroupId();

            if (id != null && !id.isBlank()) {
                idsDosGrupos.add(id);
            }
        }

        for (FormaGeometrica forma : formas) {

            String id = forma.getGroupId();

            if (id != null && idsDosGrupos.contains(id)) {
                forma.setGroupId(null);
            }
        }

        selecionados.clear();

        // Mantém apenas a forma principal selecionada.
        if (formaPrincipal != null
                && formas.contains(formaPrincipal)) {

            selecionados.add(formaPrincipal);
            formaPrincipalSelecionada = formaPrincipal;

        } else {

            formaPrincipalSelecionada = null;
        }

        requestRedraw();
        requestStatsUpdate();
    }

    // =========================
    // FORMAS AFETADAS
    // =========================

    public List<FormaGeometrica>
    getFormasAfetadasPorSelecao() {

        List<FormaGeometrica> resultado =
                new ArrayList<>();

        Set<String> gruposIncluidos =
                new HashSet<>();

        for (FormaGeometrica selecionada : selecionados) {

            String groupId = selecionada.getGroupId();

            if (groupId == null || groupId.isBlank()) {

                if (!resultado.contains(selecionada)) {
                    resultado.add(selecionada);
                }

            } else if (gruposIncluidos.add(groupId)) {

                for (FormaGeometrica forma : formas) {

                    if (groupId.equals(forma.getGroupId())
                            && !resultado.contains(forma)) {

                        resultado.add(forma);
                    }
                }
            }
        }

        return resultado;
    }

    // =========================
    // CAMADAS
    // =========================

    private List<FormaGeometrica>
    obterFormasSelecionadasNaOrdem() {

        List<FormaGeometrica> afetadas =
                getFormasAfetadasPorSelecao();

        List<FormaGeometrica> resultado =
                new ArrayList<>();

        for (FormaGeometrica forma : formas) {

            if (afetadas.contains(forma)) {
                resultado.add(forma);
            }
        }

        return resultado;
    }

    public void trazerParaFrente() {

        List<FormaGeometrica> alvos =
                obterFormasSelecionadasNaOrdem();

        if (alvos.isEmpty()) {
            return;
        }

        formas.removeAll(alvos);
        formas.addAll(alvos);

        requestRedraw();
        requestStatsUpdate();
    }

    public void enviarParaTras() {

        List<FormaGeometrica> alvos =
                obterFormasSelecionadasNaOrdem();

        if (alvos.isEmpty()) {
            return;
        }

        formas.removeAll(alvos);
        formas.addAll(0, alvos);

        requestRedraw();
        requestStatsUpdate();
    }

    // =========================
    // ATUALIZAÇÃO DA INTERFACE
    // =========================

    public void requestRedraw() {
        // A renderização é feita pelo ContainerApp.
    }

    public void requestStatsUpdate() {
        // O painel de status será conectado posteriormente.
    }

    // =========================
// HISTÓRICO: UNDO / REDO
// =========================

    /**
     * Registra o estado das formas antes de uma operação.
     */
    public void registrarEstado() {
        historicoController.registrarEstado(formas);
    }

    /**
     * Desfaz a última operação que modificou as formas.
     */
    public boolean desfazer() {

        List<FormaGeometrica> estadoAnterior =
                historicoController.desfazer(formas);

        if (estadoAnterior == null) {
            return false;
        }

        formas.clear();
        formas.addAll(estadoAnterior);

        // Limpa operações que estavam em andamento
        // e referências às formas antigas.
        formaEmAndamento = null;
        limparSelecao();

        requestRedraw();
        requestStatsUpdate();

        return true;
    }

    /**
     * Refaz a última operação desfeita.
     */
    public boolean refazer() {

        List<FormaGeometrica> estadoSeguinte =
                historicoController.refazer(formas);

        if (estadoSeguinte == null) {
            return false;
        }

        formas.clear();
        formas.addAll(estadoSeguinte);

        formaEmAndamento = null;
        limparSelecao();

        requestRedraw();
        requestStatsUpdate();

        return true;
    }

    public boolean podeDesfazer() {
        return historicoController.podeDesfazer();
    }

    public boolean podeRefazer() {
        return historicoController.podeRefazer();
    }

    // =========================
// DUPLICAÇÃO DE FORMAS
// =========================

    public void duplicarSelecionados() {

        // Obtém as formas selecionadas e os membros
        // dos grupos selecionados.
        List<FormaGeometrica> originais =
                getFormasAfetadasPorSelecao();

        if (originais.isEmpty()) {
            return;
        }

        // Guarda a posição da forma principal selecionada.
        FormaGeometrica principalOriginal =
                formaPrincipalSelecionada;

        int indicePrincipal =
                originais.indexOf(principalOriginal);

        // Registra o estado anterior para permitir Ctrl + Z.
        registrarEstado();

        List<FormaGeometrica> copias =
                new ArrayList<>();

        // Cada grupo duplicado recebe um novo identificador.
        Map<String, String> novosIdsDosGrupos =
                new HashMap<>();

        // Distância usada para posicionar as cópias.
        double deslocamento = 20.0;

        for (FormaGeometrica original : originais) {

            // Cria uma cópia independente.
            FormaGeometrica copia = original.clonar();

            // Preserva a estrutura dos grupos sem misturar
            // as cópias com as formas originais.
            String idOriginal = original.getGroupId();

            if (idOriginal != null && !idOriginal.isBlank()) {

                String novoId = novosIdsDosGrupos.computeIfAbsent(
                        idOriginal,
                        id -> java.util.UUID.randomUUID().toString()
                );

                copia.setGroupId(novoId);

            } else {

                copia.setGroupId(null);
            }

            // Desloca a cópia para facilitar sua identificação.
            copia.transladar(
                    deslocamento,
                    deslocamento
            );

            copias.add(copia);
        }

        // Adiciona as cópias ao editor.
        formas.addAll(copias);

        // Seleciona as cópias, deixando as originais
        // desmarcadas.
        selecionados.clear();
        selecionados.addAll(copias);

        // Mantém como principal a cópia correspondente
        // à forma principal da seleção original.
        if (indicePrincipal >= 0
                && indicePrincipal < copias.size()) {

            formaPrincipalSelecionada =
                    copias.get(indicePrincipal);

        } else {

            formaPrincipalSelecionada =
                    copias.get(0);
        }

        requestRedraw();
        requestStatsUpdate();
    }
}