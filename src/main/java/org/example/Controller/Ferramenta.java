package org.example.Controller;

import javafx.scene.input.MouseEvent;

public interface Ferramenta {

    void aoPressionarMouse(
            MouseEvent evento,
            EditorContext contexto
    );

    void aoArrastarMouse(
            MouseEvent evento,
            EditorContext contexto
    );

    void aoSoltarMouse(
            MouseEvent evento,
            EditorContext contexto
    );

    void aoClicar(
            MouseEvent evento,
            EditorContext contexto
    );
}