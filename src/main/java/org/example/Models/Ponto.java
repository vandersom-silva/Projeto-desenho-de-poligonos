package org.example.Models;

public class Ponto {

    private double x;
    private double y;

    public Ponto(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public void setX(double x) {
        this.x = x;
    }

    public void setY(double y) {
        this.y = y;
    }

    public void mover(double dx, double dy) {
        this.x += dx;
        this.y += dy;
    }

    public double distancia(Ponto outro) {
        double dx = this.x - outro.x;
        double dy = this.y - outro.y;

        return Math.sqrt(dx * dx + dy * dy);
    }

    public Ponto clonar() {
        return new Ponto(this.x, this.y);
    }
}