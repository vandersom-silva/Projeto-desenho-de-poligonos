package org.example.Models;

import java.util.ArrayList;
import java.util.List;

public abstract class FormaBase implements FormaGeometrica {

    protected List<Ponto> pontos;

    protected String corPreenchimentoHex = "#FFFFFF";

    protected String corBordaHex = "#000000";

    protected double espessuraBorda = 1.0;

    protected String groupId;

    public FormaBase() {
        this.pontos = new ArrayList<>();
    }

    @Override
    public void adicionarPonto(Ponto p) {
        if (p != null) {
            pontos.add(p);
        }
    }

    @Override
    public List<Ponto> getPontos() {
        return pontos;
    }

    @Override
    public int getQuantidadePontos() {
        return pontos.size();
    }

    @Override
    public String getCorPreenchimentoHex() {
        return corPreenchimentoHex;
    }

    @Override
    public void setCorPreenchimentoHex(String hex) {
        this.corPreenchimentoHex = hex;
    }

    @Override
    public String getCorBordaHex() {
        return corBordaHex;
    }

    @Override
    public void setCorBordaHex(String hex) {
        this.corBordaHex = hex;
    }

    @Override
    public double getEspessuraBorda() {
        return espessuraBorda;
    }

    @Override
    public void setEspessuraBorda(double espessura) {
        this.espessuraBorda = espessura;
    }

    @Override
    public String getGroupId() {
        return groupId;
    }

    @Override
    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    @Override
    public void transladar(double dx, double dy) {
        for (Ponto ponto : pontos) {
            ponto.mover(dx, dy);
        }
    }
}