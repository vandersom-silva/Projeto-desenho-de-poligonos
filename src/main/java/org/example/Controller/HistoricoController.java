package org.example.Controller;

import org.example.Models.FormaGeometrica;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

public class HistoricoController {

    private static final int LIMITE_HISTORICO = 100;

    private final Deque<List<FormaGeometrica>> undoStack =
            new ArrayDeque<>();

    private final Deque<List<FormaGeometrica>> redoStack =
            new ArrayDeque<>();

    /**
     * Salva uma cópia profunda do estado atual.
     * Uma nova operação invalida o histórico de refazer.
     */
    public void registrarEstado(
            List<FormaGeometrica> formas) {

        undoStack.push(clonarFormas(formas));

        redoStack.clear();

        // Limita a memória usada pelo histórico.
        while (undoStack.size() > LIMITE_HISTORICO) {
            undoStack.removeLast();
        }
    }

    /**
     * Retorna o estado anterior e guarda o estado
     * atual na pilha de refazer.
     */
    public List<FormaGeometrica> desfazer(
            List<FormaGeometrica> formasAtuais) {

        if (undoStack.isEmpty()) {
            return null;
        }

        redoStack.push(clonarFormas(formasAtuais));

        return undoStack.pop();
    }

    /**
     * Retorna o estado que foi desfeito e guarda
     * o estado atual na pilha de desfazer.
     */
    public List<FormaGeometrica> refazer(
            List<FormaGeometrica> formasAtuais) {

        if (redoStack.isEmpty()) {
            return null;
        }

        undoStack.push(clonarFormas(formasAtuais));

        List<FormaGeometrica> estado = redoStack.pop();

        while (undoStack.size() > LIMITE_HISTORICO) {
            undoStack.removeLast();
        }

        return estado;
    }

    public boolean podeDesfazer() {
        return !undoStack.isEmpty();
    }

    public boolean podeRefazer() {
        return !redoStack.isEmpty();
    }

    /**
     * Clona cada forma individualmente para que
     * alterações futuras não modifiquem o histórico.
     */
    private List<FormaGeometrica> clonarFormas(
            List<FormaGeometrica> formas) {

        List<FormaGeometrica> copia = new ArrayList<>();

        for (FormaGeometrica forma : formas) {
            copia.add(forma.clonar());
        }

        return copia;
    }
}