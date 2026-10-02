package org.example.Models;

public class Poligono extends FormaBase {

    public Poligono() {
        super();
    }

    @Override
    public double calcularArea() {

        if (pontos.size() < 3) {
            return 0;
        }

        double soma = 0;

        for (int i = 0; i < pontos.size(); i++) {

            Ponto atual = pontos.get(i);

            Ponto proximo = pontos.get(
                    (i + 1) % pontos.size()
            );

            soma += atual.getX() * proximo.getY()
                    - proximo.getX() * atual.getY();
        }

        return Math.abs(soma) / 2.0;
    }

    @Override
    public double calcularPerimetro() {

        if (pontos.size() < 2) {
            return 0;
        }

        double perimetro = 0;

        for (int i = 0; i < pontos.size(); i++) {

            Ponto atual = pontos.get(i);

            Ponto proximo = pontos.get(
                    (i + 1) % pontos.size()
            );

            perimetro += atual.distancia(proximo);
        }

        return perimetro;
    }

    @Override
    public Ponto getCentroide() {

        if (pontos.isEmpty()) {
            return new Ponto(0, 0);
        }

        double somaX = 0;
        double somaY = 0;

        for (Ponto ponto : pontos) {
            somaX += ponto.getX();
            somaY += ponto.getY();
        }

        return new Ponto(
                somaX / pontos.size(),
                somaY / pontos.size()
        );
    }

    @Override
    public boolean contemPonto(double px, double py) {

        boolean dentro = false;

        for (int i = 0, j = pontos.size() - 1;
             i < pontos.size();
             j = i++) {

            double xi = pontos.get(i).getX();
            double yi = pontos.get(i).getY();

            double xj = pontos.get(j).getX();
            double yj = pontos.get(j).getY();

            boolean intersecao =
                    ((yi > py) != (yj > py))
                            &&
                            (px < (xj - xi) * (py - yi)
                                    / (yj - yi) + xi);

            if (intersecao) {
                dentro = !dentro;
            }
        }

        return dentro;
    }

    @Override
    public void escalar(double fator) {
        Ponto centro = getCentroide();

        for (Ponto ponto : pontos) {

            double novoX =
                    centro.getX()
                            + (ponto.getX() - centro.getX()) * fator;

            double novoY =
                    centro.getY()
                            + (ponto.getY() - centro.getY()) * fator;

            ponto.setX(novoX);
            ponto.setY(novoY);
        }
    }

    @Override
    public void rotacionar(double anguloRadianos) {

        Ponto centro = getCentroide();

        double cos = Math.cos(anguloRadianos);
        double sin = Math.sin(anguloRadianos);

        for (Ponto ponto : pontos) {

            double dx = ponto.getX() - centro.getX();
            double dy = ponto.getY() - centro.getY();

            double novoX =
                    centro.getX()
                            + dx * cos
                            - dy * sin;

            double novoY =
                    centro.getY()
                            + dx * sin
                            + dy * cos;

            ponto.setX(novoX);
            ponto.setY(novoY);
        }
    }

    @Override
    public void cisalhar(double shx, double shy) {

        for (Ponto ponto : pontos) {

            double novoX =
                    ponto.getX() + shx * ponto.getY();

            double novoY =
                    ponto.getY() + shy * ponto.getX();

            ponto.setX(novoX);
            ponto.setY(novoY);
        }
    }

    @Override
    public FormaGeometrica clonar() {

        Poligono copia = new Poligono();

        for (Ponto ponto : pontos) {
            copia.adicionarPonto(ponto.clonar());
        }

        copia.setCorPreenchimentoHex(
                this.corPreenchimentoHex
        );

        copia.setCorBordaHex(
                this.corBordaHex
        );

        copia.setEspessuraBorda(
                this.espessuraBorda
        );

        copia.setGroupId(
                this.groupId
        );

        return copia;
    }
}