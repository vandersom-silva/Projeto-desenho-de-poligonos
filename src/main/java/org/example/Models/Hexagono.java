package org.example.Models;

public class Hexagono extends FormaBase {

    private Ponto centro;
    private double raio;

    public Hexagono(Ponto centro, double raio) {

        if (centro == null) {
            throw new IllegalArgumentException(
                    "O centro não pode ser nulo."
            );
        }

        if (raio <= 0) {
            throw new IllegalArgumentException(
                    "O raio deve ser maior que zero."
            );
        }

        this.centro = centro.clonar();
        this.raio = raio;

        calcularVertices();
    }

    public Ponto getCentro() {
        return centro;
    }

    public double getRaio() {
        return raio;
    }

    public void setRaio(double raio) {

        if (raio <= 0) {
            throw new IllegalArgumentException(
                    "O raio deve ser maior que zero."
            );
        }

        this.raio = raio;

        calcularVertices();
    }

    private void calcularVertices() {

        pontos.clear();

        /*
         * Hexágono regular.
         *
         * Os seis vértices ficam separados por
         * 60 graus.
         */
        for (int i = 0; i < 6; i++) {

            double angulo =
                    Math.toRadians(i * 60);

            double x =
                    centro.getX()
                            + raio * Math.cos(angulo);

            double y =
                    centro.getY()
                            + raio * Math.sin(angulo);

            pontos.add(
                    new Ponto(x, y)
            );
        }
    }

    @Override
    public void adicionarPonto(Ponto p) {
        /*
         * Os vértices são calculados automaticamente.
         */
    }

    @Override
    public double calcularArea() {

        /*
         * Área do hexágono regular:
         *
         * A = (3 * raiz(3) / 2) * lado²
         *
         * Como o lado = raio:
         */
        return (3 * Math.sqrt(3) / 2)
                * raio * raio;
    }

    @Override
    public double calcularPerimetro() {

        /*
         * Em um hexágono regular,
         * lado = raio.
         */
        return 6 * raio;
    }

    @Override
    public Ponto getCentroide() {
        return centro.clonar();
    }

    @Override
    public boolean contemPonto(
            double px,
            double py) {

        boolean dentro = false;

        for (int i = 0,
             j = pontos.size() - 1;
             i < pontos.size();
             j = i++) {

            double xi =
                    pontos.get(i).getX();

            double yi =
                    pontos.get(i).getY();

            double xj =
                    pontos.get(j).getX();

            double yj =
                    pontos.get(j).getY();

            boolean intersecao =
                    ((yi > py) != (yj > py))
                            &&
                            (px < (xj - xi)
                                    * (py - yi)
                                    / (yj - yi)
                                    + xi);

            if (intersecao) {
                dentro = !dentro;
            }
        }

        return dentro;
    }

    @Override
    public void transladar(
            double dx,
            double dy) {

        centro.mover(dx, dy);

        calcularVertices();
    }

    @Override
    public void escalar(double fator) {

        if (fator <= 0) {
            throw new IllegalArgumentException(
                    "O fator de escala deve ser maior que zero."
            );
        }

        raio *= fator;

        calcularVertices();
    }

    @Override
    public void rotacionar(
            double anguloRadianos) {

        for (Ponto ponto : pontos) {

            double dx =
                    ponto.getX()
                            - centro.getX();

            double dy =
                    ponto.getY()
                            - centro.getY();

            double novoX =
                    centro.getX()
                            + dx * Math.cos(anguloRadianos)
                            - dy * Math.sin(anguloRadianos);

            double novoY =
                    centro.getY()
                            + dx * Math.sin(anguloRadianos)
                            + dy * Math.cos(anguloRadianos);

            ponto.setX(novoX);
            ponto.setY(novoY);
        }
    }

    @Override
    public void cisalhar(
            double shx,
            double shy) {

        for (Ponto ponto : pontos) {

            double x = ponto.getX();
            double y = ponto.getY();

            double novoX =
                    x + shx * y;

            double novoY =
                    y + shy * x;

            ponto.setX(novoX);
            ponto.setY(novoY);
        }
    }

    @Override
    public FormaGeometrica clonar() {

        Hexagono copia =
                new Hexagono(
                        centro,
                        raio
                );

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

        /*
         * Copia as coordenadas atuais dos vértices.
         */
        for (int i = 0; i < pontos.size(); i++) {

            Ponto original =
                    pontos.get(i);

            Ponto copiaPonto =
                    copia.pontos.get(i);

            copiaPonto.setX(
                    original.getX()
            );

            copiaPonto.setY(
                    original.getY()
            );
        }

        return copia;
    }
}